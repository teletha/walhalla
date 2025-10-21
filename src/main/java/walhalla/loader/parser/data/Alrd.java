package walhalla.loader.parser.data;

import java.util.ArrayList;
import java.util.List;

public class Alrd extends AlObject {
    public List<AlrdEntry> entries = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Alrd [entries=" + entries + "]";
    }

    public static class AlrdEntry {
        public int offset;

        public int type;

        public String nameEn;

        public String nameJp;

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "AlrdEntry [offset=" + offset + ", type=" + type + ", nameEn=" + nameEn + ", nameJp=" + nameJp + "]";
        }
    }
}
