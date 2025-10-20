package walhalla.loader.parser;

import walhalla.loader.parser.data.*;
import walhalla.loader.parser.io.BinaryReader;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AlParser {

    private BinaryReader reader;
    private boolean decompressOnly;

    public AlObject parse(byte[] data) {
        this.reader = new BinaryReader(data);
        return parseObject();
    }

    public byte[] decompress(byte[] data) {
        this.reader = new BinaryReader(data);
        this.decompressOnly = true;
        AlObject obj = parseObject();
        if (obj instanceof Allz) {
            return ((Allz) obj).getLz();
        }
        return data;
    }

    private AlObject parseObject() {
        int startPosition = reader.position();
        String objectType = new String(reader.getBytes(4), StandardCharsets.UTF_8);
        reader.position(startPosition); // Reset position to be read by specific parsers

        switch (objectType) {
            case "ALAR":
                 return parseAlar();
            case "ALTB":
                return parseAltb();
            case "ALRD":
                return parseAlrd();
             case "ALTX":
                 return parseAltx();
             case "ALIG":
                 return parseAlig();
             case "ALMT":
                 return parseAlmt();
             case "ALOD":
                 return parseAlod();
            case "ALLZ":
                 return parseAllz();
            default:
                throw new UnsupportedOperationException("Unknown object type: " + objectType + " at " + startPosition);
        }
    }

    private Alar parseAlar() {
        Alar alar = new Alar();
        alar.type = "ALAR";
        int base = reader.position();

        reader.position(base + 4);
        int version = reader.readUByte();

        int recordCount = reader.readWord();
        reader.position(base + 0x10);

        if (version == 3) {
            reader.readWord(); // data_offset
            for (int i = 0; i < recordCount; i++) {
                reader.readWord(); // toc_offset
            }
            reader.align(4, 1);
        }

        for (int i = 0; i < recordCount; i++) {
            Alar.AlarEntry entry = new Alar.AlarEntry();
            Alar.TocEntry toc = new Alar.TocEntry();
            if (version == 2) {
                toc.index = reader.readWord();
                reader.readWord(); // unused
                toc.address = reader.readDword();
                toc.size = reader.readDword();
                reader.readDword(); // unused
                reader.position(base + toc.address - 0x22);
                toc.name = reader.readString(0x20);
                toc.name = toc.name.trim();
            } else { // version 3
                toc.index = reader.readWord();
                reader.readWord(); // unused
                toc.address = reader.readDword();
                toc.size = reader.readDword();
                reader.position(reader.position() + 6);
                toc.name = reader.readString();
                reader.align(4, 1);
            }
            entry.toc = toc;

            int currentPos = reader.position();
            reader.position(base + toc.address);
            String extension = toc.name.substring(toc.name.lastIndexOf('.') + 1);
            if (extension.equals("txt") || extension.equals("lua")) {
                TextObject text = new TextObject();
                text.type = "TEXT";
                text.text = new String(reader.getBytes(toc.size), StandardCharsets.UTF_8);
                entry.value = text;
            } else {
                entry.value = parseObject();
                if (entry.value instanceof Altx) {
                    alar.textures.add((Altx) entry.value);
                }
            }
            alar.entries.add(entry);
            reader.position(currentPos);
        }
        return alar;
    }

    private Altb parseAltb() {
        Altb altb = new Altb();
        altb.type = "ALTB";
        int startOffset = reader.position();
        reader.position(startOffset + 4);

        int version = reader.readUByte();
        int form = reader.readUByte();
        int count = reader.readWord();
        int unk1 = reader.readWord();
        int entryOffset = startOffset + reader.readWord();
        int size = reader.readDword();

        int stringsStart = 0, stringsSize = 0;
        if (form == 0x14 || form == 0x1e) {
            stringsSize = reader.readDword();
            stringsStart = startOffset + reader.readDword();
        }

        if (form == 0x1e) {
            reader.readDword(); // names_start, not used to set position
        }

        String label = reader.readString(4);

        altb.header = new Altb.Header();
        altb.header.name = label;
        altb.header.object = (Alrd) parseObject();

        reader.align(4, 1);

        reader.position(entryOffset);
        for (int i = 0; i < count; i++) {
            List<Altb.DataEntry> row = new ArrayList<>();
            int rowStartOffset = reader.position();

            for (Alrd.AlrdEntry headerEntry : altb.header.object.entries) {
                Altb.DataEntry dataEntry = new Altb.DataEntry();
                dataEntry.key = headerEntry;

                int valueOffset = rowStartOffset + headerEntry.offset;
                reader.position(valueOffset);

                Object value = null;
                if (headerEntry.type == 1 || headerEntry.type == 0x20) {
                    value = reader.readSDword();
                } else if (headerEntry.type == 4) {
                    value = reader.readFloat();
                } else if (headerEntry.type == 5) {
                    value = reader.readUByte();
                }

                if (headerEntry.type == 0x20 && stringsStart != 0) {
                    int stringOffset = stringsStart + (int) value;
                    reader.position(stringOffset);
                    value = reader.readString();
                }
                dataEntry.value = value;
                row.add(dataEntry);
            }
            altb.rows.add(row);
            reader.position(rowStartOffset + size);
        }

        reader.align(4, 1);

        if (stringsStart != 0) {
            reader.position(stringsStart + stringsSize);
            reader.align(4, 1);
        }

        if (form == 0x1e) {
            reader.readDword(); // unk_names
            int nameLen = reader.readUByte();
            altb.name = reader.readString(nameLen);
            reader.align(4, 1);
        }

        return altb;
    }

    private Alrd parseAlrd() {
        Alrd alrd = new Alrd();
        alrd.type = "ALRD";
        int startOffset = reader.position();

        reader.position(startOffset + 6);
        int count = reader.readUByte();

        reader.position(startOffset + 10);
        for (int i = 0; i < count; i++) {
            Alrd.AlrdEntry entry = new Alrd.AlrdEntry();
            entry.offset = reader.readWord();

            int a = reader.readUByte();
            int b = reader.readUByte();
            entry.type = a;

            int lenEn = reader.readUByte();
            int lenJp = reader.readUByte();

            byte[] enBytes = reader.getBytes(lenEn);
            entry.nameEn = new String(enBytes, StandardCharsets.UTF_8);
            reader.position(reader.position() + 1);

            byte[] jpBytes = reader.getBytes(lenJp);
            entry.nameJp = new String(jpBytes, StandardCharsets.UTF_8);

            reader.align(4, 1);
            reader.position(reader.position() + b);
            reader.align(4, 1);

            alrd.entries.add(entry);
        }
        return alrd;
    }

    private Altx parseAltx() {
        Altx altx = new Altx();
        altx.type = "ALTX";
        int startOffset = reader.position();
        reader.position(startOffset + 4);

        int version = reader.readUByte();
        int form = reader.readUByte();
        int count = reader.readWord();
        int aligOffset = startOffset + reader.readDword();

        if (form == 0) {
            List<Integer> blockStarts = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                blockStarts.add(startOffset + reader.readWord());
            }
            reader.align(4, 1);

            for (int i = 0; i < count; i++) {
                String frameName = null;
                if (reader.position() == blockStarts.get(i) - 0x20) {
                    frameName = reader.readString(0x20);
                }
                reader.position(blockStarts.get(i));

                int index = reader.readWord();
                reader.readWord(); // unknown
                int framesCount = reader.readWord();
                reader.readWord(); // unknown

                Altx.Sprite sprite = new Altx.Sprite();
                for (int j = 0; j < framesCount; j++) {
                    Altx.Frame frame = new Altx.Frame();
                    frame.x = reader.readWord();
                    frame.y = reader.readWord();
                    frame.width = reader.readWord();
                    frame.height = reader.readWord();
                    sprite.frames.put(j, frame);
                }
                for (int j = 0; j < framesCount; j++) {
                    Altx.Frame frame = sprite.frames.get(j);
                    frame.originX = reader.readWord();
                    frame.originY = reader.readWord();
                }
                altx.sprites.put(index, sprite);
            }
        }

        reader.position(aligOffset);
        if (form == 0) {
            altx.rawImage = (Alig) parseObject();
        } else if (form == 0x0e) {
            altx.width = reader.readWord();
            altx.height = reader.readWord();
            altx.rawImageName = reader.readString();
        }
        return altx;
    }

    private Alig parseAlig() {
        Alig alig = new Alig();
        alig.type = "ALIG";
        reader.position(reader.position() + 4);

        int version = reader.readUByte();
        reader.position(reader.position() + 3);

        String format = reader.readString(4);
        String paletteFormat = reader.readString(4);
        alig.width = reader.readDword();
        alig.height = reader.readDword();

        // Skipping image data parsing as per requirements

        return alig;
    }

    private Almt parseAlmt() {
        Almt almt = new Almt();
        almt.type = "ALMT";
        int startOffset = reader.position();
        reader.position(startOffset + 4);

        int version = reader.readUByte();
        reader.readUByte(); // unk1
        int entryCount = reader.readWord();
        int fieldCount = reader.readUByte();
        reader.readUByte(); // unk2
        reader.readWord(); // unk3

        for (int i = 0; i < entryCount; i++) {
            Almt.AlmtEntry entry = new Almt.AlmtEntry();
            entry.name = reader.readString(4);
            almt.entries.add(entry);
        }

        int dataOffset = startOffset + reader.readDword();

        List<java.util.Map<String, Object>> fields = new ArrayList<>();
        if (fieldCount > 0) {
            for (int i = 0; i < fieldCount; i++) {
                java.util.Map<String, Object> field = new java.util.HashMap<>();
                field.put("offset", startOffset + reader.readWord());
                fields.add(field);
            }
            for (java.util.Map<String, Object> field : fields) {
                field.put("id1", reader.readUByte());
                field.put("id2", reader.readUByte());
                reader.position((int) field.get("offset"));
                field.put("name", reader.readString());
            }
            reader.align(4, 1);
        } else {
            reader.position(reader.position() + 4);
        }

        almt.pattern = reader.readDword();
        almt.length = reader.readWord();
        almt.rate = reader.readUByte();
        reader.readUByte(); // flag1
        int unk4 = reader.readWord();

        for (int i = 0; i < (unk4 - 0x002a) / 2; i++) {
            reader.readWord(); // entry_offset
        }

        for (Almt.AlmtEntry entry : almt.entries) {
            if (fieldCount > 0) {
                int fieldOffsetBase = reader.position();
                int fieldCountNonStream = reader.readUByte();
                int fieldCountStream = reader.readUByte();

                List<Integer> fieldDescs = new ArrayList<>();
                for (int i = 0; i < fieldCountNonStream + fieldCountStream; i++) {
                    fieldDescs.add(reader.readUByte());
                }
                reader.align(2, 1);

                List<Integer> fieldOffsets = new ArrayList<>();
                for (int i = 0; i < fieldCountNonStream + fieldCountStream; i++) {
                    fieldOffsets.add(fieldOffsetBase + reader.readWord());
                }

                for (int i = 0; i < fieldDescs.size(); i++) {
                    int fieldDesc = fieldDescs.get(i);
                    int fieldIdx = (fieldDesc & 0x0f);
                    java.util.Map<String, Object> field = fields.get(fieldIdx);
                    String fieldName = (String) field.get("name");

                    List<Almt.StreamFrame> stream = new ArrayList<>();

                    java.util.function.Supplier<Object> parser = getAlmtFieldParser(fieldName);

                    if (i >= fieldCountNonStream) {
                        while (true) {
                            int time = reader.readWord();
                            if (time == 0xffff) break;
                            if (time == 0x494c) continue;

                            Almt.StreamFrame frame = new Almt.StreamFrame();
                            frame.time = time;
                            frame.data = parser.get();
                            stream.add(frame);
                        }
                    } else {
                        Almt.StreamFrame frame = new Almt.StreamFrame();
                        frame.data = parser.get();
                        stream.add(frame);
                    }
                    entry.data.put(fieldName, stream);
                }
            } else {
                reader.readDword(); // Should be 0
                String noname = "";
                List<Almt.StreamFrame> stream = new ArrayList<>();
                Almt.StreamFrame frame = new Almt.StreamFrame();
                frame.data = reader.readString();
                stream.add(frame);
                entry.data.put(noname, stream);
            }
        }

        return almt;
    }

    private java.util.function.Supplier<Object> getAlmtFieldParser(String fieldName) {
        switch (fieldName) {
            case "PatternNo":
            case "BlendMode":
            case "Disp":
            case "HFlip":
            case "VFlip":
                return () -> reader.readWord();
            case "Texture0ID":
                return () -> {
                    java.util.Map<String, Integer> t = new java.util.HashMap<>();
                    t.put("id1", reader.readWord());
                    t.put("id2", reader.readWord());
                    return t;
                };
            case "Alpha":
            case "DrawPrioOffset":
                return () -> reader.readFloat();
            case "Pos":
                return () -> {
                    int[] t = new int[3];
                    for(int i = 0; i < 3; i++) t[i] = reader.readDword();
                    return t;
                };
            case "ParentNodeID":
                return () -> reader.readString(4);
            case "Rot":
                return () -> reader.readDword();
            case "Scale":
            case "Center":
                return () -> {
                    java.util.Map<String, Float> t = new java.util.HashMap<>();
                    t.put("x", reader.readFloat());
                    t.put("y", reader.readFloat());
                    t.put("z", reader.readFloat());
                    return t;
                };
            case "Color3":
                 return () -> {
                    float[] t = new float[3];
                    for(int i = 0; i < 3; i++) t[i] = reader.readFloat();
                    return t;
                };
            default:
                throw new UnsupportedOperationException("Unsupported ALMT field: " + fieldName);
        }
    }

    private Alod parseAlod() {
        Alod alod = new Alod();
        alod.type = "ALOD";
        int startOffset = reader.position();
        reader.position(startOffset + 4);

        int version = reader.readUByte();
        int form = reader.readUByte();
        int countEntries = reader.readUByte();
        int countFields = reader.readUByte();

        reader.readDword(); // unk4
        int mtOffset = startOffset + reader.readDword();

        List<Integer> entryOffsets = new ArrayList<>();
        for (int i = 0; i < countEntries; i++) {
            entryOffsets.add(startOffset + reader.readWord());
        }

        List<Integer> fieldOffsets = new ArrayList<>();
        for (int i = 0; i < countFields; i++) {
            fieldOffsets.add(startOffset + reader.readWord());
        }

        List<String> fields = new ArrayList<>();
        for (int i = 0; i < countFields; i++) {
            reader.position(fieldOffsets.get(i));
            fields.add(reader.readString());
        }
        reader.align(4, 1);

        for (int i = 0; i < countEntries; i++) {
            reader.align(4, 1);
            reader.position(entryOffsets.get(i));

            Alod.AlodEntry entry = new Alod.AlodEntry();
            entry.name = reader.readString(8);

            int countEntryFields = reader.readDword();
            List<Integer> entryFieldOffsets = new ArrayList<>();
            for (int j = 0; j < countEntryFields; j++) {
                entryFieldOffsets.add(entryOffsets.get(i) + reader.readWord());
            }

            List<Integer> entryFieldIndices = new ArrayList<>();
            for (int j = 0; j < countEntryFields; j++) {
                entryFieldIndices.add(reader.readUByte());
            }
            reader.align(2, 1);

            for (int j = 0; j < countEntryFields; j++) {
                String field = fields.get(entryFieldIndices.get(j));
                reader.position(entryFieldOffsets.get(j));

                Object value = null;
                if (field.equals("Texture0ID")) {
                    // Not a real map, just a struct
                    java.util.Map<String, Integer> val = new java.util.HashMap<>();
                    val.put("id1", reader.readWord());
                    val.put("id2", reader.readWord());
                    value = val;
                } else if (field.equals("Color")) {
                    // Not a real map, just a struct
                    java.util.Map<String, Float> val = new java.util.HashMap<>();
                    val.put("r", reader.readFloat());
                    val.put("g", reader.readFloat());
                    val.put("b", reader.readFloat());
                    val.put("a", reader.readFloat());
                    value = val;
                } else if (field.equals("Alpha")) {
                    value = reader.readFloat();
                }
                entry.fields.put(field, value);
            }
            alod.entries.add(entry);
        }

        if (form == 2) {
            reader.position(mtOffset);
            alod.mt = (Almt) parseObject();
        }

        return alod;
    }

    private AlObject parseAllz() {
        Allz allz = new Allz();
        allz.type = "ALLZ";
        reader.position(reader.position() + 4);

        int version = reader.readUByte();
        int minBitsLength = reader.readUByte();
        int minBitsOffset = reader.readUByte();
        int minBitsLiteral = reader.readUByte();
        int dstSize = reader.readDword();

        byte[] dst = new byte[dstSize];
        int dstPtr = 0;

        int bits = 0;
        int bitsCount = 0;

        BitReader bitReader = new BitReader(reader);

        java.util.function.Function<Integer, Integer> readControl = (minBits) -> {
            int u = bitReader.readUnary();
            int n = bitReader.readBits(u + minBits);
            if (u > 0) {
                return n + (((1 << u) - 1) << minBits);
            } else {
                return n;
            }
        };

        java.util.function.Supplier<Integer> readControlLiteral = () -> 1 + readControl.apply(minBitsLiteral);
        java.util.function.Supplier<Integer> readControlOffset = () -> -1 - readControl.apply(minBitsOffset);
        java.util.function.Supplier<Integer> readControlLength = () -> 3 + readControl.apply(minBitsLength);

        java.util.function.Consumer<Integer> copyLiteral = (control) -> {
            for (int i = 0; i < control; i++) {
                dst[dstPtr++] = reader.readByte();
            }
        };

        java.util.function.BiConsumer<Integer, Integer> copyWord = (offset, length) -> {
            for(int i = 0; i < length; i++) {
                dst[dstPtr] = dst[dstPtr + offset];
                dstPtr++;
            }
        };

        copyLiteral.accept(readControlLiteral.get());

        int wordOff = readControlOffset.get();
        int wordLen = readControlLength.get();

        while (dstPtr < dstSize) {
            if (dstPtr + wordLen >= dstSize) {
                copyWord.accept(wordOff, dstSize - dstPtr);
                break;
            }

            if (bitReader.readBit() == 0) {
                int literal = readControlLiteral.get();
                if(dstPtr + wordLen + literal >= dstSize) {
                    copyWord.accept(wordOff, wordLen);
                    copyLiteral.accept(dstSize - dstPtr);
                    break;
                }

                int literalOffset = reader.position();
                reader.position(reader.position() + literal);
                int nextOff = readControlOffset.get();
                int nextLen = readControlLength.get();

                copyWord.accept(wordOff, wordLen);

                int controlOffset = reader.position();
                reader.position(literalOffset);
                copyLiteral.accept(literal);
                reader.position(controlOffset);

                wordOff = nextOff;
                wordLen = nextLen;
            } else {
                int nextOff = readControlOffset.get();
                int nextLen = readControlLength.get();
                copyWord.accept(wordOff, wordLen);
                wordOff = nextOff;
                wordLen = nextLen;
            }
        }

        allz.setLz(dst);

        if (decompressOnly) {
            return allz;
        } else {
            return new AlParser().parse(dst);
        }
    }
}
