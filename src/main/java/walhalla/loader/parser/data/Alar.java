package walhalla.loader.parser.data;

import java.util.ArrayList;
import java.util.List;

public class Alar extends AlObject {
    public List<AlarEntry> entries = new ArrayList<>();

    public List<Altx> textures = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Alar [entries=" + entries + ", textures=" + textures + "]";
    }

    public static class AlarEntry {
        public String name;

        public AlObject value;

        public TocEntry toc;

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "AlarEntry [name=" + name + ", value=" + ", toc=" + toc + "]";
        }
    }

    public static class TocEntry {
        public int index;

        public int address;

        public int size;

        public String name;

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "TocEntry [index=" + index + ", address=" + address + ", size=" + size + ", name=" + name + "]";
        }
    }
}
