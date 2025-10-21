package walhalla.loader.writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import walhalla.loader.parser.data.AlObject;
import walhalla.loader.parser.data.Alar;
import walhalla.loader.parser.data.Almt;
import walhalla.loader.parser.data.Alrd;
import walhalla.loader.parser.data.Altb;
import walhalla.loader.parser.data.TextObject;

public class AlWriter {

    private final Path outputDir;

    public AlWriter(String outputDir) {
        this.outputDir = Paths.get(outputDir);
    }

    public void write(AlObject obj) throws IOException {
        Files.createDirectories(outputDir);
        writeObject(obj, outputDir);
    }

    private void writeObject(AlObject obj, Path currentDir) throws IOException {
        switch (obj.type) {
        case "ALAR":
            writeAlar((Alar) obj, currentDir);
            break;
        case "ALTB":
            writeAltb((Altb) obj, currentDir);
            break;
        case "ALRD":
            writeAlrd((Alrd) obj, currentDir);
            break;
        case "ALMT":
            writeAlmt((Almt) obj, currentDir);
            break;
        case "TEXT":
            writeText((TextObject) obj, currentDir);
            break;
        // ALTX, ALIG, ALOD are not written as per requirement (image related)
        default:
            // Create a .nul file for unhandled types
            Files.createFile(currentDir.resolve(obj.type + ".nul"));
            break;
        }
    }

    private void writeAlar(Alar alar, Path currentDir) throws IOException {
        for (int i = 0; i < alar.entries.size(); i++) {
            Alar.AlarEntry entry = alar.entries.get(i);
            String name = String.format("%03d_%s", i + 1, entry.toc.name);
            Path entryPath = currentDir.resolve(name);
            if (entry.value.type.equals("TEXT")) {
                writeObject(entry.value, entryPath);
            } else {
                Files.createDirectories(entryPath);
                writeObject(entry.value, entryPath);
            }
        }
    }

    private void writeAltb(Altb altb, Path currentDir) throws IOException {
        Path filePath = currentDir.resolve("ALTB_" + altb.header.name + ".txt");
        StringBuilder sb = new StringBuilder();

        List<Integer> widths = new ArrayList<>();
        for (int i = 0; i < altb.header.object.entries.size(); i++) {
            widths.add(altb.header.object.entries.get(i).nameEn.length());
        }

        for (List<Altb.DataEntry> row : altb.rows) {
            for (int i = 0; i < row.size(); i++) {
                String valueStr = valueToString(row.get(i).value, row.get(i).key.nameEn);
                widths.set(i, Math.max(widths.get(i), valueStr.length()));
            }
        }

        for (int i = 0; i < altb.header.object.entries.size(); i++) {
            Alrd.AlrdEntry header = altb.header.object.entries.get(i);
            sb.append(pad(header.nameEn, widths.get(i), header.type == 0x20)).append(" ");
        }
        sb.append("\n");

        for (List<Altb.DataEntry> row : altb.rows) {
            for (int i = 0; i < row.size(); i++) {
                Altb.DataEntry entry = row.get(i);
                String valueStr = valueToString(entry.value, entry.key.nameEn);
                sb.append(pad(valueStr, widths.get(i), entry.key.type == 0x20)).append(" ");
            }
            sb.append("\n");
        }
        Files.write(filePath, sb.toString().getBytes());
    }

    private void writeAlrd(Alrd alrd, Path currentDir) throws IOException {
        Path filePath = currentDir.resolve("ALRD.txt");
        StringBuilder sb = new StringBuilder();
        for (Alrd.AlrdEntry entry : alrd.entries) {
            sb.append(entry.nameEn).append(" ");
        }
        sb.append("\n");
        Files.write(filePath, sb.toString().getBytes());
    }

    private void writeAlmt(Almt almt, Path currentDir) throws IOException {
        Path filePath = currentDir.resolve("ALMT.txt");
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("pattern: 0x%08x\n", almt.pattern));
        sb.append("length: ").append(almt.length).append("\n");
        sb.append("rate (?): ").append(almt.rate).append("\n\n");

        for (Almt.AlmtEntry entry : almt.entries) {
            sb.append("entry: ").append(entry.name).append("\n");
            entry.data.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(fieldEntry -> {
                sb.append("  ").append(fieldEntry.getKey()).append("\n");
                List<Almt.StreamFrame> stream = fieldEntry.getValue();
                for (int i = 0; i < stream.size(); i++) {
                    Almt.StreamFrame frame = stream.get(i);
                    String timeString = (frame.time == null) ? "N/A" : String.format("%03d", frame.time);
                    String dataString = streamFrameDataToString(fieldEntry.getKey(), frame.data);
                    sb.append(String.format("    %2d @%s: %s\n", i, timeString, dataString));
                }
                sb.append("\n");
            });
            sb.append("\n");
        }
        Files.write(filePath, sb.toString().getBytes());
    }

    private String pad(String str, int n, boolean left) {
        int padLen = n - str.length();
        if (padLen <= 0) return str;
        StringBuilder sb = new StringBuilder();
        if (left) {
            sb.append(str);
            for (int i = 0; i < padLen; i++)
                sb.append(" ");
        } else {
            for (int i = 0; i < padLen; i++)
                sb.append(" ");
            sb.append(str);
        }
        return sb.toString();
    }

    private String valueToString(Object value, String key) {
        if (value instanceof String) {
            return "\"" + ((String) value).replace("\n", "\\n") + "\"";
        }
        if (value instanceof Boolean) {
            return (Boolean) value ? "true" : "false";
        }
        if (key.equals("PatternID")) {
            return String.format("0x%08x", (int) value);
        }
        return String.valueOf(value);
    }

    private String streamFrameDataToString(String fieldName, Object data) {
        if (fieldName.equals("Scale") || fieldName.equals("Center")) {
            @SuppressWarnings("unchecked")
            Map<String, Float> vec = (Map<String, Float>) data;
            return String.format("x:%g y:%g z:%g", vec.get("x"), vec.get("y"), vec.get("z"));
        }
        return String.valueOf(data);
    }

    private void writeText(TextObject text, Path filePath) throws IOException {
        Files.write(filePath, text.text.getBytes());
    }
}
