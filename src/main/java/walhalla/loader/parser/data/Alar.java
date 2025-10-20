package walhalla.loader.parser.data;

import java.util.ArrayList;
import java.util.List;

public class Alar extends AlObject {
    public List<AlarEntry> entries = new ArrayList<>();
    public List<Altx> textures = new ArrayList<>();

    public static class AlarEntry {
        public String name;
        public AlObject value;
        public TocEntry toc;
    }

    public static class TocEntry {
        public int index;
        public int address;
        public int size;
        public String name;
    }
}
