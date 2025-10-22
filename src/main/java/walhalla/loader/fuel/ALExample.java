package walhalla.loader.fuel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import psychopath.Locator;

/**
 * ALデータ解析器の使用例とテストクラス
 */
public class ALExample {
    public static void main(String[] args) {
        Locator.directory(".data/raw").walkFile("**.atx").to(file -> {
            parse(file.path());
        });
    }

    public static void parse(String filePath) {
        try {
            // ファイルを読み込み
            byte[] data = Files.readAllBytes(Paths.get(filePath));

            // ヘッダーを確認してファイル形式を判定
            String header = new String(data, 0, Math.min(4, data.length));
            System.out.println("ファイルヘッダー: " + header);

            AL alFile = null;

            switch (header) {
            case "ALLZ":
                System.out.println("ALLZ形式として解析中...");
                alFile = new ALLZ(data);
                ALLZ allz = (ALLZ) alFile;
                System.out.println("解凍後サイズ: " + allz.getDst().length + " bytes");
                break;

            case "ALL4":
                System.out.println("ALL4形式として解析中...");

                // ファイル構造を調べるために先頭64バイトを16進数表示
                System.out.println("ファイル先頭64バイトの16進数表示:");
                printHex(data, 64);

                // ALL4ヘッダー情報を解析
                BufferReader br = new BufferReader(data);
                System.out.println("Header: " + br.readString(4));
                System.out.println("Version/Flag1: 0x" + String.format("%02X", br.readByte()));
                System.out.println("Flag2: 0x" + String.format("%02X", br.readByte()));
                System.out.println("Flag3: 0x" + String.format("%02X", br.readByte()));
                System.out.println("Flag4: 0x" + String.format("%02X", br.readByte()));
                System.out.println("Uncompressed Size: " + br.readDword());
                System.out.println("Current position: " + br.getPosition());

                alFile = new ALL4(data);
                ALL4 all4 = (ALL4) alFile;
                System.out.println("LZ4圧縮ファイルを検出");

                // 解凍されたデータの形式をチェックして適切に処理
                byte[] decompressed = all4.getDst();
                if (decompressed.length >= 4) {
                    String decompressedHeader = new String(decompressed, 0, 4);
                    System.out.println("解凍後のファイル形式: " + decompressedHeader);

                    switch (decompressedHeader) {
                    case "ALTB":
                        System.out.println("解凍されたALTBファイルを解析中...");
                        try {
                            ALTB innerAltb = new ALTB(decompressed);
                            System.out.println("ALTBエントリ数: " + innerAltb.getCount());
                            System.out.println("ALTB文字列フィールド数: " + innerAltb.getStringField().size());

                            // ALTBの内容を表示
                            displayALTBContents(innerAltb);

                            // ALTBファイルとして保存（テキスト形式で完全なデータを保存）
                            String altbOutputPath = filePath + ".altb.txt";
                            innerAltb.save(altbOutputPath);
                            System.out.println("ALTBテキストデータを " + altbOutputPath + " に保存しました");

                        } catch (Exception e) {
                            System.err.println("内部ALTBファイルの解析エラー: " + e.getMessage());
                        }
                        break;

                    case "ALAR":
                        System.out.println("解凍されたALARアーカイブを解析中...");
                        try {
                            ALAR innerAlar = new ALAR(decompressed);
                            System.out.println("ALARアーカイブバージョン: " + innerAlar.getVers());
                            System.out.println("ALAR含まれるファイル数: " + innerAlar.getCount());

                            // ALARの内容を表示
                            displayALARContents(innerAlar);

                            // ALARアーカイブとして展開保存
                            String alarOutputPath = filePath + ".alar_extracted";
                            innerAlar.save(alarOutputPath);
                            System.out.println("ALARアーカイブを " + alarOutputPath + " に展開しました");

                        } catch (Exception e) {
                            System.err.println("内部ALARファイルの解析エラー: " + e.getMessage());
                        }
                        break;

                    case "ALLZ":
                        System.out.println("解凍されたALLZファイルを解析中...");
                        try {
                            ALLZ innerAllz = new ALLZ(decompressed);
                            System.out.println("ALLZ解凍後サイズ: " + innerAllz.getDst().length + " bytes");

                            // ALLZの解凍データを保存
                            String allzOutputPath = filePath + ".allz.extracted.txt";
                            Files.write(Paths.get(allzOutputPath), innerAllz.getDst());
                            System.out.println("ALLZ解凍データを " + allzOutputPath + " に保存しました");

                        } catch (Exception e) {
                            System.err.println("内部ALLZファイルの解析エラー: " + e.getMessage());
                        }
                        break;

                    case "ALRD":
                        System.out.println("解凍されたALRDファイルを解析中...");
                        try {
                            ALRD innerAlrd = new ALRD(decompressed);
                            System.out.println("ALRDヘッダー数: " + innerAlrd.getCount());

                            // ALRDの詳細を表示
                            for (int i = 0; i < innerAlrd.getHeaders().size(); i++) {
                                ALRDHeader h = innerAlrd.getHeaders().get(i);
                                System.out.println("  " + i + ": " + h.nameEN + " (" + h.nameJP + ")");
                            }

                        } catch (Exception e) {
                            System.err.println("内部ALRDファイルの解析エラー: " + e.getMessage());
                        }
                        break;

                    default:
                        System.out.println("解凍されたファイルは未対応の形式です: " + decompressedHeader);
                        // 解凍データをそのまま保存
                        String unknownOutputPath = filePath + ".decompressed";
                        try {
                            Files.write(Paths.get(unknownOutputPath), decompressed);
                            System.out.println("解凍データを " + unknownOutputPath + " に保存しました");
                        } catch (IOException e) {
                            System.err.println("解凍データの保存エラー: " + e.getMessage());
                        }
                        break;
                    }
                }
                break;

            case "ALRD":
                System.out.println("ALRD形式として解析中...");
                alFile = new ALRD(data);
                ALRD alrd = (ALRD) alFile;
                System.out.println("ヘッダー数: " + alrd.getCount());
                for (int i = 0; i < alrd.getHeaders().size(); i++) {
                    ALRDHeader h = alrd.getHeaders().get(i);
                    System.out.println("  " + i + ": " + h.nameEN + " (" + h.nameJP + ")");
                }
                break;

            case "ALTB":
                System.out.println("ALTB形式として解析中...");
                alFile = new ALTB(data);
                ALTB altb = (ALTB) alFile;
                altb.save(filePath + ".txt");
                System.out.println("エントリ数: " + altb.getCount());
                System.out.println("文字列フィールド数: " + altb.getStringField().size());
                break;

            case "ALAR":
                System.out.println("ALAR形式として解析中...");
                alFile = new ALAR(data);
                ALAR alar = (ALAR) alFile;
                System.out.println("アーカイブバージョン: " + alar.getVers());
                System.out.println("含まれるファイル数: " + alar.getCount());

                // アーカイブの内容を表示
                displayALARContents(alar);
                break;

            default:
                System.out.println("不明な形式です。テキストとして処理します。");
                alFile = new Text(data);
                Text text = (Text) alFile;
                System.out.println("コンテンツ長: " + text.getContent().length() + " 文字");
                break;
            }

            // 出力ファイル名を生成
            String outputPath = filePath + ".extracted";
            alFile.save(outputPath);
            System.out.println("結果を " + outputPath + " に保存しました");

        } catch (IOException e) {
            System.err.println("ファイル読み込みエラー: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("解析エラー: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * バイト配列を16進数で表示するユーティリティメソッド
     */
    public static void printHex(byte[] data, int maxLength) {
        int length = Math.min(data.length, maxLength);
        for (int i = 0; i < length; i++) {
            if (i > 0 && i % 16 == 0) {
                System.out.println();
            }
            System.out.printf("%02X ", data[i] & 0xFF);
        }
        System.out.println();
        if (data.length > maxLength) {
            System.out.println("... (残り " + (data.length - maxLength) + " bytes)");
        }
    }

    /**
     * ALTBファイルの内容を人間が読める形式で表示
     */
    private static void displayALTBContents(ALTB altb) {
        System.out.println("\n=== ALTBファイル内容 ===");
        System.out.println("フォーマット: 0x" + String.format("%02X", altb.getForm()));
        System.out.println("バージョン: " + altb.getVers());
        System.out.println("エントリ数: " + altb.getCount());

        // ヘッダー情報を表示
        System.out.println("\n--- カラムヘッダー ---");
        var headers = altb.getHeaders();
        for (int i = 0; i < headers.size(); i++) {
            var header = headers.get(i);
            System.out.printf("%d: %s (%s) - Type:%d, Offset:%d\n", i, header.nameEN, header.nameJP, header.type, header.offset);
        }

        // 文字列フィールドを表示
        var stringField = altb.getStringField();
        if (!stringField.isEmpty()) {
            System.out.println("\n--- 文字列フィールド ---");
            stringField.entrySet()
                    .stream()
                    .sorted(java.util.Map.Entry.comparingByKey())
                    .limit(10) // 最初の10個だけ表示
                    .forEach(entry -> System.out.printf("Offset %d: %s\n", entry.getKey(), entry.getValue()));
            if (stringField.size() > 10) {
                System.out.println("... および " + (stringField.size() - 10) + " 個の追加エントリ");
            }
        }

        // データエントリを表示
        var contents = altb.getContents();
        if (!contents.isEmpty()) {
            System.out.println("\n--- データエントリ (最初の5個) ---");
            for (int i = 0; i < Math.min(5, contents.size()); i++) {
                var row = contents.get(i);
                System.out.printf("エントリ %d:\n", i);
                for (var header : headers) {
                    Object value = row.get(header.nameEN);
                    if (value != null) {
                        System.out.printf("  %s: %s\n", header.nameEN, value);
                    }
                }
                System.out.println();
            }
            if (contents.size() > 5) {
                System.out.println("... および " + (contents.size() - 5) + " 個の追加エントリ");
            }
        }

        System.out.println("=== ALTBファイル内容終了 ===\n");
    }

    /**
     * ALARファイルの内容を人間が読める形式で表示
     */
    private static void displayALARContents(ALAR alar) {
        System.out.println("\n=== ALARアーカイブ内容 ===");
        System.out.println("バージョン: " + alar.getVers());
        System.out.println("ファイル数: " + alar.getCount());

        var files = alar.getFiles();
        System.out.println("\n--- 含まれるファイル ---");
        for (int i = 0; i < files.size(); i++) {
            var entry = files.get(i);
            System.out.printf("%d: %s (%d bytes) - Index:%d\n", i, entry.name, entry.size, entry.index);

            // ファイルの種類を表示
            String fileType = "Unknown";
            if (entry.content instanceof ALTB) {
                fileType = "ALTB (Table Binary)";
            } else if (entry.content instanceof ALLZ) {
                fileType = "ALLZ (Compressed)";
            } else if (entry.content instanceof ALL4) {
                fileType = "ALL4 (LZ4 Compressed)";
            } else if (entry.content instanceof ALRD) {
                fileType = "ALRD (Resource Directory)";
            } else if (entry.content instanceof Text) {
                fileType = "Text File";
            } else if (entry.content instanceof DefaultAL) {
                fileType = "Binary Data";
            }
            System.out.printf("   Type: %s\n", fileType);
        }

        System.out.println("=== ALARアーカイブ内容終了 ===\n");
    }
}
