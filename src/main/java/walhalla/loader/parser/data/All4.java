package walhalla.loader.parser.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class All4 extends AlObject {
    public int version;

    public int count;

    public List<All4Entry> entries = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "All4 [version=" + version + ", count=" + count + ", entries=" + entries + "]";
    }

    public static class All4Entry {
        public String name;

        public int offset;

        public int size;

        public byte[] data;

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "All4Entry [name=" + name + ", offset=" + offset + ", size=" + size + ", data=" + Arrays.toString(data) + "]";
        }
    }
}
