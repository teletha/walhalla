package walhalla.loader.parser.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Almt extends AlObject {
    public int pattern;
    public int length;
    public int rate;
    public List<AlmtEntry> entries = new ArrayList<>();

    public static class AlmtEntry {
        public String name;
        public Map<String, List<StreamFrame>> data = new HashMap<>();
    }

    public static class StreamFrame {
        public Integer time;
        public Object data;
    }
}
