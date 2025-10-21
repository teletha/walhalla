package walhalla.loader.parser.data;

import java.util.ArrayList;
import java.util.List;

public class Altb extends AlObject {
    public Header header;

    public List<List<DataEntry>> rows = new ArrayList<>();

    public String name;

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Altb [header=" + header + ", rows=" + rows + ", name=" + name + "]";
    }

    public static class Header {
        public String name;

        public Alrd object;

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Header [name=" + name + ", object=" + object + "]";
        }

    }

    public static class DataEntry {
        public Alrd.AlrdEntry key;

        public Object value;

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "DataEntry [key=" + key + ", value=" + value + "]";
        }
    }
}
