package walhalla.loader.parser.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class BinaryReader {

    private final ByteBuffer buffer;

    public BinaryReader(byte[] data) {
        this.buffer = ByteBuffer.wrap(data);
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public int position() {
        return buffer.position();
    }

    public void position(int newPosition) {
        buffer.position(newPosition);
    }

    public short readWord() {
        return buffer.getShort();
    }

    public int readDword() {
        return buffer.getInt();
    }

    public int readUnsignedDword() {
        return (int) (readDword() & 0xFFFFFFFFL);
    }

    public int readSDword() {
        return readDword();
    }

    public float readFloat() {
        return buffer.getFloat();
    }

    public String readString(int maxLength) {
        byte[] bytes = new byte[maxLength];
        buffer.get(bytes);
        int length = 0;
        while (length < maxLength && bytes[length] != 0) {
            length++;
        }
        return new String(bytes, 0, length, StandardCharsets.UTF_8);
    }

    public String readString() {
        int start = buffer.position();
        while (buffer.get() != 0);
        int end = buffer.position();
        byte[] bytes = new byte[end - start - 1];
        buffer.position(start);
        buffer.get(bytes);
        buffer.get(); // Skip null terminator
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public void align(int alignment, int n) {
        while (true) {
            int currentAlign = buffer.position() % alignment;
            if (currentAlign == n) {
                return;
            }
            buffer.position(buffer.position() + 1);
        }
    }

    public byte readByte() {
        return buffer.get();
    }

    public int readUByte() {
        return buffer.get() & 0xFF;
    }

    public byte get(int index) {
        return buffer.get(index);
    }

    public byte[] getBytes(int length) {
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return bytes;
    }

    public byte[] getBytes(int offset, int length) {
        byte[] bytes = new byte[length];
        int currentPos = buffer.position();
        buffer.position(offset);
        buffer.get(bytes);
        buffer.position(currentPos);
        return bytes;
    }
}
