package walhalla.loader.parser.data;

import java.util.HashMap;
import java.util.Map;

public class Altx extends AlObject {
    public Map<Integer, Sprite> sprites = new HashMap<>();
    public Alig rawImage;
    public String rawImageName;
    public int width;
    public int height;

    public static class Sprite {
        public String name;
        public Map<Integer, Frame> frames = new HashMap<>();
    }

    public static class Frame {
        public int x;
        public int y;
        public int width;
        public int height;
        public int originX;
        public int originY;
    }
}
