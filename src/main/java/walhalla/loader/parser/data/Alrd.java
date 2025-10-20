package walhalla.loader.parser.data;

import java.util.ArrayList;
import java.util.List;

public class Alrd extends AlObject {
    public List<AlrdEntry> entries = new ArrayList<>();

    public static class AlrdEntry {
        public int offset;
        public int type;
        public String nameEn;
        public String nameJp;
    }
}
