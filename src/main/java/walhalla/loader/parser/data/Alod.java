package walhalla.loader.parser.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Alod extends AlObject {
    public List<AlodEntry> entries = new ArrayList<>();
    public Almt mt;

    public static class AlodEntry {
        public String name;
        public Map<String, Object> fields = new HashMap<>();
    }
}
