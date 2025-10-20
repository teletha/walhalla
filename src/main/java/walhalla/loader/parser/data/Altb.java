package walhalla.loader.parser.data;

import java.util.ArrayList;
import java.util.List;

public class Altb extends AlObject {
    public Header header;
    public List<List<DataEntry>> rows = new ArrayList<>();
    public String name;

    public static class Header {
        public String name;
        public Alrd object;
    }

    public static class DataEntry {
        public Alrd.AlrdEntry key;
        public Object value;
    }
}
