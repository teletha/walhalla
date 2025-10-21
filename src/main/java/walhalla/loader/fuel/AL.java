package walhalla.loader.fuel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import psychopath.Locator;

/**
 * AL（Archive Loader）データ解析器
 * 様々なフォーマットのアーカイブファイルを解析・操作するためのライブラリ
 */
public class AL {
    protected byte[] buffer;

    protected String head;

    public AL(byte[] buffer) {
        this.buffer = buffer;
        this.head = new String(buffer, 0, Math.min(4, buffer.length), StandardCharsets.UTF_8);
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public String getHead() {
        return head;
    }

    /**
     * 指定されたパスのファイルが存在する場合は読み込み、存在しない場合は元のバッファを返す
     */
    public byte[] packageFile(String path) {
        try {
            if (Files.exists(Paths.get(path))) {
                System.out.println("found " + path);
                return Files.readAllBytes(Paths.get(path));
            } else {
                return this.buffer;
            }
        } catch (IOException e) {
            return this.buffer;
        }
    }

    /**
     * ファイルを保存する（基本実装では何もしない）
     */
    public void save(String path) {
        throw new Error();
    }

    /**
     * オフセットを指定されたアライメントに合わせる
     */
    protected static int align(int offset, int alignment) {
        if (offset % alignment == 0) {
            return offset;
        }
        return offset + (alignment - (offset % alignment));
    }
}

/**
 * デフォルトのALクラス（特別な処理を行わない）
 */
class DefaultAL extends AL {
    public DefaultAL(byte[] buffer) {
        super(buffer);
    }
}

/**
 * テキストファイル用のALクラス
 */
class Text extends AL {
    private String content;

    public Text(byte[] buffer) {
        super(buffer);
        this.content = new String(buffer, StandardCharsets.UTF_8);
    }

    public String getContent() {
        return content;
    }

    @Override
    public void save(String path) {
        try {
            Files.write(Paths.get(path), buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/**
 * LZ4圧縮されたALL4ファイル用のクラス
 * LZ4FrameInputStreamを使用してフレーム形式を正しく処理
 */
class ALL4 extends AL {
    private byte[] dst;

    private int uncompressedSize;

    public ALL4(byte[] buffer) {
        super(buffer);

        try {
            // TypeScript実装: const jump = buffer.slice(12); this.Dst = decompress(jump);
            byte[] compressedData = Arrays.copyOfRange(buffer, 12, buffer.length);

            // ヘッダー情報の取得（参考用）
            BufferReader br = new BufferReader(buffer);
            br.readString(4); // "ALL4"
            br.readByte(); // version
            br.readByte(); // flag2
            br.readByte(); // flag3
            br.readByte(); // flag4
            int declaredSize = br.readDword(); // 宣言されたサイズ

            System.out.println("ALL4 decompression using LZ4FrameInputStream:");
            System.out.println("  Declared size: " + declaredSize);
            System.out.println("  Compressed data size: " + compressedData.length);

            // LZ4FrameInputStreamを使用した解凍
            this.dst = decompressLZ4Frame(compressedData);
            this.uncompressedSize = this.dst.length;

            System.out.println("  Actual decompressed size: " + this.uncompressedSize);

        } catch (Exception e) {
            System.err.println("LZ4 frame decompression failed: " + e.getMessage());
            e.printStackTrace();

            // 解凍に失敗した場合は圧縮データをそのまま返す
            this.dst = Arrays.copyOfRange(buffer, 12, buffer.length);
            this.uncompressedSize = this.dst.length;
            System.out.println("Using raw compressed data as fallback (" + this.dst.length + " bytes)");
        }
    }

    /**
     * LZ4FrameInputStreamを使用したフレーム形式の解凍
     */
    private byte[] decompressLZ4Frame(byte[] compressedData) throws Exception {
        try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(compressedData);
                net.jpountz.lz4.LZ4FrameInputStream lz4is = new net.jpountz.lz4.LZ4FrameInputStream(bais);
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int len;
            while ((len = lz4is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }

            return baos.toByteArray();
        }
    }

    public byte[] getDst() {
        return dst;
    }

    public int getUncompressedSize() {
        return uncompressedSize;
    }

    @Override
    public void save(String path) {
        try {
            // 解凍されたデータを保存
            Files.write(Paths.get(path), this.dst);
            System.out.println("ALL4 decompressed data saved to: " + path);
        } catch (IOException e) {
            System.err.println("Failed to save ALL4 data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public byte[] packageFile(String path) {
        return super.packageFile(path);
    }
}

/**
 * カスタム圧縮されたALLZファイル用のクラス
 */
class ALLZ extends AL {
    private int vers;

    private int minBitsLength;

    private int minBitsOffset;

    private int minBitsLiteral;

    private int dstSize;

    private byte[] dst;

    public ALLZ(byte[] buffer) {
        super(buffer);
        BufferReader br = new BufferReader(buffer);

        this.head = br.readString(4);
        this.vers = br.readByte();
        this.minBitsLength = br.readByte();
        this.minBitsOffset = br.readByte();
        this.minBitsLiteral = br.readByte();
        this.dstSize = br.readDword();
        this.dst = new byte[dstSize];

        int dstOffset = 0;

        // 解凍処理
        int literalLength = readControlLiteral(br);
        dstOffset = copyLiteral(br, dstOffset, literalLength);

        int wordOffset = readControlOffset(br);
        int wordLength = readControlLength(br);

        String finishFlag = "overflow";

        while (!br.overflow()) {
            if (dstOffset + wordLength >= this.dstSize) {
                finishFlag = "word";
                break;
            }

            if (br.readBit() == 0) {
                literalLength = readControlLiteral(br);
                if (dstOffset + wordLength + literalLength >= this.dstSize) {
                    finishFlag = "literal";
                    break;
                }
                dstOffset = copyWord(dstOffset, wordOffset, wordLength);
                dstOffset = copyLiteral(br, dstOffset, literalLength);
                wordOffset = readControlOffset(br);
                wordLength = readControlLength(br);
            } else {
                dstOffset = copyWord(dstOffset, wordOffset, wordLength);
                wordOffset = readControlOffset(br);
                wordLength = readControlLength(br);
            }
        }

        switch (finishFlag) {
        case "word":
            copyWord(dstOffset, wordOffset, wordLength);
            break;
        case "literal":
            dstOffset = copyWord(dstOffset, wordOffset, wordLength);
            copyLiteral(br, dstOffset, literalLength);
            break;
        case "overflow":
            throw new RuntimeException("Overflow in ALLZ");
        }
    }

    private int readControl(BufferReader br, int minBits) {
        int u = br.readUnary();
        int n = br.readBits(u + minBits);
        if (u > 0) {
            return n + (((1 << u) - 1) << minBits);
        } else {
            return n;
        }
    }

    private int readControlLength(BufferReader br) {
        return 3 + readControl(br, this.minBitsLength);
    }

    private int readControlOffset(BufferReader br) {
        return -1 - readControl(br, this.minBitsOffset);
    }

    private int readControlLiteral(BufferReader br) {
        return 1 + readControl(br, this.minBitsLiteral);
    }

    private int copyWord(int dstOffset, int offset, int length) {
        for (int i = 0; i < length; i++) {
            int trueOffset = (offset < 0) ? dstOffset + offset : offset;
            if (trueOffset >= 0 && trueOffset < dstOffset && dstOffset < this.dst.length) {
                this.dst[dstOffset] = this.dst[trueOffset];
            }
            dstOffset++;
        }
        return dstOffset;
    }

    private int copyLiteral(BufferReader br, int dstOffset, int length) {
        br.copy(this.dst, dstOffset, length);
        return dstOffset + length;
    }

    public byte[] getDst() {
        return dst;
    }

    public int getVers() {
        return vers;
    }

    public int getDstSize() {
        return dstSize;
    }

    @Override
    public byte[] packageFile(String path) {
        return super.packageFile(path);
    }
}

/**
 * ALRDヘッダー情報を格納するクラス
 */
class ALRDHeader {
    public int offset = 0;

    public int type = 0;

    public String nameEN = "";

    public String nameJP = "";
}

/**
 * ALRDファイル（Archive Loader Resource Directory）用のクラス
 */
class ALRD extends AL {
    private int vers;

    private int count;

    private int size;

    private List<ALRDHeader> headers;

    public ALRD(byte[] buffer) {
        super(buffer);
        BufferReader br = new BufferReader(buffer);

        this.head = br.readString(4);
        if (!"ALRD".equals(this.head)) {
            throw new RuntimeException("Not a ALRD file");
        }

        this.vers = br.readWord();
        this.count = br.readWord();
        this.size = br.readWord();
        this.headers = new ArrayList<>();

        for (int i = 0; i < this.count; i++) {
            ALRDHeader header = new ALRDHeader();
            header.offset = br.readWord();
            header.type = br.readByte();
            int emptyLength = br.readByte();
            int lengthEN = br.readByte();
            int lengthJP = br.readByte();
            header.nameEN = br.readString();
            header.nameJP = br.readString();
            br.align(4);
            br.seek(emptyLength, Origin.CURRENT);
            br.align(4);
            this.headers.add(header);
        }
    }

    public List<ALRDHeader> getHeaders() {
        return headers;
    }

    public int getVers() {
        return vers;
    }

    public int getCount() {
        return count;
    }

    public int getSize() {
        return size;
    }
}

/**
 * ALTBファイル（Archive Loader Table Binary）用のクラス
 */
class ALTB extends AL {
    private int vers;

    private int form;

    private int count;

    private int unknown1;

    private int tableEntry;

    private Integer nameStartAddressOffset;

    private Integer nameStartAddress;

    private Integer unknownNames;

    private Integer nameLength;

    private String name;

    private int size;

    private int stringFieldSizePosition = 0;

    private int stringFieldSize = 0;

    private int stringFieldEntry = 0;

    private String label;

    private Map<Integer, String> stringField = new HashMap<>();

    private List<Integer> stringOffsetList = new ArrayList<>();

    private List<ALRDHeader> headers = new ArrayList<>();

    private List<Map<String, Object>> contents = new ArrayList<>();

    public ALTB(byte[] buffer) {
        super(buffer);
        BufferReader br = new BufferReader(buffer);

        this.head = br.readString(4);
        this.vers = br.readByte();
        this.form = br.readByte();
        this.count = br.readWord();
        this.unknown1 = br.readWord();
        this.tableEntry = br.readWord();
        this.size = br.readDword();

        if (this.form == 0x14 || this.form == 0x1e || this.form == 0x04) {
            this.stringFieldSizePosition = br.getPosition();
            this.stringFieldSize = br.readDword();
            this.stringFieldEntry = br.readDword();

            int nowPosition = br.getPosition();
            br.seek(this.stringFieldEntry, Origin.BEGIN);

            while (br.getPosition() < this.stringFieldEntry + this.stringFieldSize) {
                int offset = br.getPosition() - this.stringFieldEntry;
                String s = br.readString();
                this.stringField.put(offset, s);
                this.stringOffsetList.add(offset);
            }
            br.seek(nowPosition, Origin.BEGIN);
        }

        if (this.form == 0x1e) {
            this.nameStartAddressOffset = br.getPosition();
            this.nameStartAddress = br.readDword();
        }

        if (this.form != 0x04) {
            this.label = br.readString(4);
        }

        byte[] alrdBuffer = br.readBytes(this.tableEntry - br.getPosition());
        br.seek(this.tableEntry, Origin.BEGIN);

        ALRD alrd = new ALRD(alrdBuffer);
        this.headers = alrd.getHeaders();

        // テーブルデータの読み込み
        for (int i = 0; i < this.count; i++) {
            br.seek(this.tableEntry + this.size * i, Origin.BEGIN);
            Map<String, Object> row = new HashMap<>();

            for (ALRDHeader header : this.headers) {
                int offset = br.getPosition();
                Object v = null;

                switch (header.type) {
                case 1:
                    v = java.nio.ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt(offset + header.offset);
                    break;
                case 4:
                    v = java.nio.ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getFloat(offset + header.offset);
                    break;
                case 5:
                    v = buffer[offset + header.offset] & 0xFF;
                    break;
                case 0x20:
                    int stringOffset = java.nio.ByteBuffer.wrap(buffer)
                            .order(java.nio.ByteOrder.LITTLE_ENDIAN)
                            .getInt(offset + header.offset);
                    v = this.stringField.get(stringOffset);
                    break;
                }
                row.put(header.nameEN, v);
            }
            this.contents.add(row);
        }

        if (this.nameStartAddress != null) {
            br.seek(this.nameStartAddress, Origin.BEGIN);
            this.unknownNames = br.readDword();
            this.nameLength = br.readByte();
            this.name = br.readString(this.nameLength);
        }
    }

    @Override
    public void save(String path) {
        path = path.replace(".atb", ".txt");
        StringBuilder result = new StringBuilder();

        // ファイル情報を出力
        result.append("=== ALTBファイル情報 ===\n");
        result.append("フォーマット: 0x").append(String.format("%02X", this.form)).append("\n");
        result.append("バージョン: ").append(this.vers).append("\n");
        result.append("エントリ数: ").append(this.count).append("\n");
        if (this.name != null) {
            result.append("ファイル名: ").append(this.name).append("\n");
        }
        result.append("\n");

        // カラムヘッダー情報を出力
        result.append("=== カラム定義 ===\n");
        for (int i = 0; i < this.headers.size(); i++) {
            ALRDHeader header = this.headers.get(i);
            result.append(String.format("%d: %s (%s) - Type:%d, Offset:%d\n", i, header.nameEN, header.nameJP, header.type, header.offset));
        }
        result.append("\n");

        // 文字列フィールドを出力（オフセット順）
        if (!this.stringField.isEmpty()) {
            result.append("=== 文字列フィールド ===\n");
            this.stringField.entrySet().stream().sorted(java.util.Map.Entry.comparingByKey()).forEach(entry -> {
                String value = entry.getValue().replace("\n", "\\n").replace("\r", "\\r");
                result.append(String.format("Offset %d: %s\n", entry.getKey(), value));
            });
            result.append("\n");
        }

        // 全データエントリを出力（CSVライク形式）
        result.append("=== データエントリ ===\n");

        // ヘッダー行を出力
        for (int i = 0; i < this.headers.size(); i++) {
            if (i > 0) result.append("\t");
            result.append(this.headers.get(i).nameEN);
        }
        result.append("\n");

        // 日本語ヘッダー行を出力
        for (int i = 0; i < this.headers.size(); i++) {
            if (i > 0) result.append("\t");
            result.append(this.headers.get(i).nameJP);
        }
        result.append("\n");

        // データ行を出力
        for (int i = 0; i < this.contents.size(); i++) {
            java.util.Map<String, Object> row = this.contents.get(i);
            for (int j = 0; j < this.headers.size(); j++) {
                if (j > 0) result.append("\t");
                ALRDHeader header = this.headers.get(j);
                Object value = row.get(header.nameEN);
                if (value != null) {
                    if (value instanceof String) {
                        // 文字列の場合はタブと改行をエスケープ
                        String strValue = ((String) value).replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r");
                        result.append(strValue);
                    } else {
                        result.append(value.toString());
                    }
                }
            }
            result.append("\n");
        }

        try {
            Files.write(Paths.get(path), result.toString().getBytes(StandardCharsets.UTF_8));
            System.out.println("完全なALTBデータを " + path + " に保存しました");
        } catch (IOException e) {
            System.err.println("ALTBファイル保存エラー: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Getter methods
    public Map<Integer, String> getStringField() {
        return stringField;
    }

    public List<Map<String, Object>> getContents() {
        return contents;
    }

    public List<ALRDHeader> getHeaders() {
        return headers;
    }

    public int getVers() {
        return vers;
    }

    public int getForm() {
        return form;
    }

    public int getCount() {
        return count;
    }

    public String getName() {
        return name;
    }
}

/**
 * ALARエントリ情報を格納するクラス
 */
class ALAREntry {
    public int index = 0;

    public int unknown1 = 0;

    public int address = 0;

    public int offset = 0;

    public int size = 0;

    public byte[] unknown2 = new byte[0];

    public String name = "";

    public int unknown3 = 0;

    public AL content = new DefaultAL(new byte[0]);
}

/**
 * ALARファイル（Archive Loader Archive）用のクラス
 * 複数のファイルを含むアーカイブ形式
 */
class ALAR extends AL {
    private List<ALAREntry> files = new ArrayList<>();

    private List<Integer> tocOffsetList = new ArrayList<>();

    private int vers;

    private int unknown;

    private int count;

    private int dataOffsetByData = 0;

    private int unknown1 = 0;

    private int unknown2 = 0;

    private byte[] unknownBytes;

    private int dataOffset = 0;

    public ALAR(byte[] buffer) {
        super(buffer);
        BufferReader br = new BufferReader(buffer);

        this.head = br.readString(4);
        if (!"ALAR".equals(this.head)) {
            throw new RuntimeException("Not an ALAR file");
        }

        this.vers = br.readByte();
        this.unknown = br.readByte();

        if (this.vers != 2 && this.vers != 3) {
            throw new RuntimeException("Unsupported ALAR version: " + this.vers);
        }

        System.out.println("ALAR version: " + this.vers);

        if (this.vers == 2) {
            this.count = br.readWord();
            this.unknownBytes = br.readBytes(8);
        } else { // version 3
            this.count = br.readWord();
            this.unknown1 = br.readWord();
            this.unknown2 = br.readWord();
            this.unknownBytes = br.readBytes(4);
            this.dataOffset = br.readWord();

            for (int i = 0; i < this.count; i++) {
                this.tocOffsetList.add(br.readWord());
            }
        }

        br.align(4);

        System.out.println("ALAR file count: " + this.count);

        // エントリを解析
        for (int i = 0; i < this.count; i++) {
            ALAREntry entry = parseTocEntry(br, buffer);

            // ファイル内容を取得
            byte[] fileData = Arrays.copyOfRange(buffer, entry.address, entry.address + entry.size);

            // ファイル拡張子に応じて適切なクラスで解析
            String extension = getFileExtension(entry.name).toLowerCase();
            if (extension.endsWith("a") && extension.length() > 1) {
                // .aar, .atb などのALファイル
                try {
                    entry.content = parseALFile(fileData);
                } catch (Exception e) {
                    System.err.println("Failed to parse AL file " + entry.name + ": " + e.getMessage());
                    entry.content = new DefaultAL(fileData);
                }
            } else if (".txt".equals(extension)) {
                entry.content = new Text(fileData);
            } else {
                entry.content = new DefaultAL(fileData);
            }

            this.files.add(entry);
            System.out.println("  Entry " + i + ": " + entry.name + " (" + entry.size + " bytes)");
        }

        // データオフセットを計算
        if (this.vers == 2 && !this.files.isEmpty()) {
            this.dataOffsetByData = this.files.get(0).address - 0x22;
        }
        if (this.vers == 3 && !this.files.isEmpty()) {
            this.dataOffsetByData = this.files.get(0).address;
        }
    }

    private ALAREntry parseTocEntry(BufferReader br, byte[] buffer) {
        ALAREntry entry = new ALAREntry();

        if (this.vers == 2) {
            entry.index = br.readWord();
            entry.unknown1 = br.readWord();
            entry.address = br.readDword();
            entry.size = br.readDword();
            entry.unknown2 = br.readBytes(4);

            // ファイル名を取得（アドレス - 0x22の位置から）
            int currentPos = br.getPosition();
            br.seek(entry.address - 0x22, Origin.BEGIN);
            entry.name = br.readString();
            br.seek(entry.address - 0x02, Origin.BEGIN);
            entry.unknown3 = br.readWord();
            br.seek(currentPos, Origin.BEGIN);

        } else { // version 3
            entry.index = br.readWord();
            entry.unknown1 = br.readWord();
            entry.address = br.readDword();
            entry.size = br.readDword();
            entry.unknown2 = br.readBytes(6);
            entry.name = br.readString();
            br.align(4);
        }

        return entry;
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return filename.substring(lastDot);
    }

    private AL parseALFile(byte[] data) {
        if (data.length < 4) {
            return new DefaultAL(data);
        }

        String header = new String(data, 0, 4, StandardCharsets.UTF_8);
        switch (header) {
        case "ALLZ":
            return new ALLZ(data);
        case "ALL4":
            return new ALL4(data);
        case "ALRD":
            return new ALRD(data);
        case "ALTB":
            return new ALTB(data);
        case "ALAR":
            return new ALAR(data);
        default:
            return new DefaultAL(data);
        }
    }

    @Override
    public void save(String path) {
        // .aar拡張子を除去してディレクトリ名にする
        path = path.replace(".aar", "");

        try {
            // ディレクトリを作成
            java.nio.file.Path dirPath = Paths.get(path);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            System.out.println("Extracting ALAR archive to: " + path);

            for (ALAREntry entry : this.files) {
                String filePath = Paths.get(path, entry.name).toString();
                Files.write(Paths.get(filePath), entry.content.getBuffer());
                System.out.println("  Extracted: " + entry.name + " -> " + filePath);
            }

            Locator.directory(dirPath).walkFile("*.atb").to(file -> {
                ALExample.parse(file.path());
            });

            // アーカイブ情報をテキストファイルとして保存
            saveArchiveInfo(path);

        } catch (IOException e) {
            System.err.println("Failed to extract ALAR archive: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveArchiveInfo(String basePath) throws IOException {
        StringBuilder info = new StringBuilder();
        info.append("=== ALAR Archive Information ===\n");
        info.append("Version: ").append(this.vers).append("\n");
        info.append("File Count: ").append(this.count).append("\n");
        info.append("Data Offset: ").append(this.dataOffsetByData).append("\n");
        info.append("\n=== File Entries ===\n");

        for (int i = 0; i < this.files.size(); i++) {
            ALAREntry entry = this.files.get(i);
            info.append(String.format("Entry %d:\n", i));
            info.append(String.format("  Index: %d\n", entry.index));
            info.append(String.format("  Name: %s\n", entry.name));
            info.append(String.format("  Address: 0x%X\n", entry.address));
            info.append(String.format("  Size: %d bytes\n", entry.size));
            info.append(String.format("  Unknown1: %d\n", entry.unknown1));
            if (entry.unknown2.length > 0) {
                info.append("  Unknown2: ");
                for (byte b : entry.unknown2) {
                    info.append(String.format("%02X ", b & 0xFF));
                }
                info.append("\n");
            }
            info.append("\n");
        }

        String infoPath = Paths.get(basePath, "_archive_info.txt").toString();
        Files.write(Paths.get(infoPath), info.toString().getBytes(StandardCharsets.UTF_8));
    }

    // Getter methods
    public List<ALAREntry> getFiles() {
        return files;
    }

    public int getVers() {
        return vers;
    }

    public int getCount() {
        return count;
    }
}

/**
 * ALTXフレーム情報を格納するクラス
 */
class ALTXFrame {
    public int x = 0;
    public int y = 0;
    public int width = 0;
    public int height = 0;
    public int originX = 0;
    public int originY = 0;
}

/**
 * ALTXフレームテーブルを格納するクラス
 */
class ALTXFrameTable {
    public String name = "";
    public List<ALTXFrame> frames = new ArrayList<>();
}

/**
 * ALTXファイル（Archive Loader Texture）用のクラス
 * 画像/スプライト情報を含むファイル形式
 */
class ALTX extends AL {
    private int vers;
    private int form;
    private int count;
    private Map<Integer, ALTXFrameTable> sprites = new HashMap<>();
    private byte[] image = new byte[0];
    private String fakeImage;
    private int width = 0;
    private int height = 0;
    private int unknown1;
    private int unknown2;
    
    public ALTX(byte[] buffer) {
        super(buffer);
        BufferReader br = new BufferReader(buffer);
        
        int startOffset = br.getPosition();
        this.head = br.readString(4);
        if (!"ALTX".equals(this.head)) {
            throw new RuntimeException("Not an ALTX file");
        }
        
        this.vers = br.readByte();
        this.form = br.readByte();
        this.count = br.readWord();
        int alignOffset = startOffset + br.readDword();
        
        System.out.println("ALTX texture file:");
        System.out.println("  Version: " + this.vers);
        System.out.println("  Form: " + this.form);
        System.out.println("  Sprite count: " + this.count);
        
        if (this.form == 0) {
            // スプライトブロックの開始位置を読み取り
            List<Integer> blockStart = new ArrayList<>();
            for (int i = 0; i < this.count; i++) {
                blockStart.add(startOffset + br.readWord());
            }
            br.align(4);
            
            // 各スプライトを解析
            for (int i = 0; i < this.count; i++) {
                String frameName = "";
                
                // フレーム名があるかチェック
                if (br.getPosition() == blockStart.get(i) - 0x20 || 
                    (i > 0 && br.getPosition() == blockStart.get(0) - 0x20 + blockStart.get(i))) {
                    frameName = br.readString(0x20);
                }
                
                int index = br.readWord();
                this.unknown1 = br.readWord();
                int frames = br.readWord();
                this.unknown2 = br.readWord();
                
                ALTXFrameTable frameTable = new ALTXFrameTable();
                frameTable.name = frameName;
                
                // フレーム位置とサイズを読み取り
                for (int j = 0; j < frames; j++) {
                    ALTXFrame frame = new ALTXFrame();
                    frame.x = br.readShort();
                    frame.y = br.readShort();
                    frame.width = br.readShort();
                    frame.height = br.readShort();
                    frame.originX = 0;
                    frame.originY = 0;
                    frameTable.frames.add(frame);
                }
                
                // フレーム原点を読み取り
                for (int j = 0; j < frames; j++) {
                    frameTable.frames.get(j).originX = br.readShort();
                    frameTable.frames.get(j).originY = br.readShort();
                }
                
                this.sprites.put(index, frameTable);
                System.out.println("  Sprite " + index + ": " + frameName + " (" + frames + " frames)");
            }
        }
        
        // 画像データの位置に移動
        br.seek(alignOffset, Origin.BEGIN);
        
        if (this.form == 0) {
            // ALIG形式の画像データ
            byte[] alignBuffer = br.readBytes(br.getLength() - br.getPosition());
            try {
                ALIG alig = new ALIG(alignBuffer);
                this.image = alig.getImage();
                this.width = alig.getWidth();
                this.height = alig.getHeight();
                System.out.println("  Image size: " + this.width + "x" + this.height);
            } catch (Exception e) {
                System.err.println("Failed to parse ALIG image data: " + e.getMessage());
                this.image = alignBuffer;
            }
        } else if (this.form == 0x0e) {
            // 外部画像ファイル参照
            this.width = br.readWord();
            this.height = br.readWord();
            this.fakeImage = br.readString(0x100);
            System.out.println("  External image: " + this.fakeImage);
            System.out.println("  Image size: " + this.width + "x" + this.height);
        }
    }
    
    @Override
    public void save(String path) {
        try {
            // スプライト情報をテキストファイルとして保存
            String txtPath = path.replace(".atx", ".txt");
            StringBuilder result = new StringBuilder();
            
            result.append("=== ALTX Texture Information ===\n");
            result.append("Version: ").append(this.vers).append("\n");
            result.append("Form: ").append(this.form).append("\n");
            result.append("Sprite Count: ").append(this.count).append("\n");
            result.append("Image Size: ").append(this.width).append("x").append(this.height).append("\n");
            if (this.fakeImage != null) {
                result.append("External Image: ").append(this.fakeImage).append("\n");
            }
            result.append("\n");
            
            result.append("=== Sprite Frames ===\n");
            for (Map.Entry<Integer, ALTXFrameTable> entry : this.sprites.entrySet()) {
                int index = entry.getKey();
                ALTXFrameTable frameTable = entry.getValue();
                
                result.append("Sprite ").append(index);
                if (!frameTable.name.isEmpty()) {
                    result.append(" (").append(frameTable.name).append(")");
                }
                result.append(":\n");
                
                for (int i = 0; i < frameTable.frames.size(); i++) {
                    ALTXFrame frame = frameTable.frames.get(i);
                    result.append(String.format("  Frame %d: X=%d, Y=%d, W=%d, H=%d, OriginX=%d, OriginY=%d\n",
                        i, frame.x, frame.y, frame.width, frame.height, frame.originX, frame.originY));
                }
                result.append("\n");
            }
            
            Files.write(Paths.get(txtPath), result.toString().getBytes(StandardCharsets.UTF_8));
            System.out.println("ALTX sprite info saved to: " + txtPath);
            
            // 画像データがある場合は適切な形式で保存
            if (this.image.length > 0) {
                String imageExtension = determineImageFormat(this.image);
                String imagePath = path.replace(".atx", imageExtension);
                Files.write(Paths.get(imagePath), this.image);
                System.out.println("ALTX image data saved to: " + imagePath);
            }
            
        } catch (IOException e) {
            System.err.println("Failed to save ALTX data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 画像データのフォーマットを判別して適切な拡張子を返す
     */
    private String determineImageFormat(byte[] imageData) {
        if (imageData.length < 4) {
            return ".bin"; // 不明な形式
        }
        
        // PNG形式のマジックナンバーをチェック
        if (imageData.length >= 8 && 
            imageData[0] == (byte)0x89 && imageData[1] == 0x50 && 
            imageData[2] == 0x4E && imageData[3] == 0x47 &&
            imageData[4] == 0x0D && imageData[5] == 0x0A && 
            imageData[6] == 0x1A && imageData[7] == 0x0A) {
            return ".png";
        }
        
        // JPEG形式のマジックナンバーをチェック
        if (imageData.length >= 3 && 
            imageData[0] == (byte)0xFF && imageData[1] == (byte)0xD8 && imageData[2] == (byte)0xFF) {
            return ".jpg";
        }
        
        // BMP形式のマジックナンバーをチェック
        if (imageData.length >= 2 && 
            imageData[0] == 0x42 && imageData[1] == 0x4D) {
            return ".bmp";
        }
        
        // GIF形式のマジックナンバーをチェック
        if (imageData.length >= 6 && 
            imageData[0] == 0x47 && imageData[1] == 0x49 && imageData[2] == 0x46 &&
            imageData[3] == 0x38 && (imageData[4] == 0x37 || imageData[4] == 0x39) && imageData[5] == 0x61) {
            return ".gif";
        }
        
        // TGA形式かどうかをチェック（簡易的）
        if (imageData.length >= 18) {
            // TGAヘッダーの基本的なチェック
            int imageType = imageData[2] & 0xFF;
            if (imageType == 1 || imageType == 2 || imageType == 3 || imageType == 9 || imageType == 10 || imageType == 11) {
                return ".tga";
            }
        }
        
        // DDS形式のマジックナンバーをチェック
        if (imageData.length >= 4 && 
            imageData[0] == 0x44 && imageData[1] == 0x44 && imageData[2] == 0x53 && imageData[3] == 0x20) {
            return ".dds";
        }
        
        // ALIGの独自形式の場合、生データとして保存
        if (imageData.length >= 4) {
            String header = new String(imageData, 0, 4, StandardCharsets.UTF_8);
            if ("ALIG".equals(header)) {
                return ".alig"; // ALIG独自形式
            }
        }
        
        // 不明な形式の場合
        System.out.println("Unknown image format, saving as .bin");
        return ".bin";
    }
    
    // Getter methods
    public Map<Integer, ALTXFrameTable> getSprites() { return sprites; }
    public byte[] getImage() { return image; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getVers() { return vers; }
    public int getForm() { return form; }
    public int getCount() { return count; }
    public String getFakeImage() { return fakeImage; }
}

/**
 * ALIG画像ファイル用のクラス（ALTXで使用）
 */
class ALIG extends AL {
    private int vers;
    private String form;
    private String paletteForm;
    private int width;
    private int height;
    private byte[] image;
    
    public ALIG(byte[] buffer) {
        super(buffer);
        BufferReader br = new BufferReader(buffer);
        
        this.head = br.readString(4);
        if (!"ALIG".equals(this.head)) {
            throw new RuntimeException("Not an ALIG file");
        }
        
        this.vers = br.readByte();
        int unknown1 = br.readByte();
        int unknown2 = br.readByte();
        int unknown3 = br.readByte();
        this.form = br.readString(4);
        this.paletteForm = br.readString(4);
        this.width = br.readDword();
        this.height = br.readDword();
        
        int size = this.width * this.height;
        int unknown5 = br.readWord();
        int unknown6 = br.readWord();
        int unknown7 = br.readDword();
        
        // 残りのデータを画像データとして取得
        this.image = br.readBytes(br.getLength() - br.getPosition());
        
        System.out.println("ALIG image: " + this.width + "x" + this.height + " (" + this.form + "/" + this.paletteForm + ")");
    }
    
    public byte[] getImage() { return image; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getForm() { return form; }
}