package walhalla.loader.fuel;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class BufferReader {
    private ByteBuffer buffer;
    private int position;
    private int length;
    private int bits = 0;
    private int bitsCount = 0;
    
    public BufferReader(byte[] data) {
        this.buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        this.position = 0;
        this.length = data.length;
    }
    
    public int getPosition() {
        return position;
    }
    
    public int getLength() {
        return length;
    }
    
    public boolean overflow() {
        return position > length;
    }
    
    public void seek(int offset, Origin origin) {
        int originPos;
        switch (origin) {
            case BEGIN:
                originPos = 0;
                break;
            case CURRENT:
                originPos = this.position;
                break;
            case END:
                originPos = this.length - 1;
                offset = -offset;
                break;
            default:
                throw new RuntimeException("Unknown origin");
        }
        this.position = originPos + offset;
    }
    
    public void align(int alignment) {
        if (position % alignment != 0) {
            position = position + (alignment - (position % alignment));
        }
    }
    
    public String readString() {
        int start = position;
        for (int i = 0; i < 0xFFFF; i++) {
            byte b = buffer.get(position);
            position++;
            if (b == 0) {
                break;
            }
        }
        return new String(buffer.array(), start, position - start - 1, StandardCharsets.UTF_8);
    }
    
    public String readString(int length) {
        if (position + length > this.length) {
            throw new IndexOutOfBoundsException("Buffer overflow");
        }
        String value = new String(buffer.array(), position, length, StandardCharsets.UTF_8);
        position += length;
        return value;
    }
    
    public int readDword() {
        if (position + 4 > length) {
            throw new IndexOutOfBoundsException("Buffer overflow");
        }
        int value = buffer.getInt(position);
        position += 4;
        return value;
    }
    
    public int readInt() {
        if (position + 4 > length) {
            throw new IndexOutOfBoundsException("Buffer overflow");
        }
        int value = buffer.getInt(position);
        position += 4;
        return value;
    }
    
    public int readByte() {
        if (position >= length) {
            throw new IndexOutOfBoundsException("Buffer overflow");
        }
        int value = buffer.get(position) & 0xFF;
        position++;
        return value;
    }
    
    public int readUByte() {
        return readByte();
    }
    
    public int readWord() {
        if (position + 2 > length) {
            throw new IndexOutOfBoundsException("Buffer overflow");
        }
        int value = buffer.getShort(position) & 0xFFFF;
        position += 2;
        return value;
    }
    
    public int readUWord() {
        return readWord();
    }
    
    public short readShort() {
        if (position + 2 > length) {
            throw new IndexOutOfBoundsException("Buffer overflow");
        }
        short value = buffer.getShort(position);
        position += 2;
        return value;
    }
    
    public byte[] readBytes(int length) {
        if (position + length > this.length) {
            throw new IndexOutOfBoundsException("Buffer overflow");
        }
        byte[] bytes = new byte[length];
        System.arraycopy(buffer.array(), position, bytes, 0, length);
        position += length;
        return bytes;
    }
    
    public float readFloat() {
        if (position + 4 > length) {
            throw new IndexOutOfBoundsException("Buffer overflow");
        }
        float value = buffer.getFloat(position);
        position += 4;
        return value;
    }
    
    public int readBit() {
        ensure(1);
        int result = bits & 1;
        bits = bits >> 1;
        bitsCount -= 1;
        return result;
    }
    
    public int readBits(int count) {
        ensure(count);
        int result = bits & ((1 << count) - 1);
        bits = bits >> count;
        bitsCount -= count;
        return result;
    }
    
    public int readUnary() {
        int n = 0;
        while (readBit() == 1) {
            n++;
        }
        return n;
    }
    
    public void copy(byte[] dest, int destOffset, int length) {
        if (position + length > this.length) {
            throw new IndexOutOfBoundsException("Buffer overflow");
        }
        System.arraycopy(buffer.array(), position, dest, destOffset, length);
        position += length;
    }
    
    private void ensure(int count) {
        while (bitsCount < count) {
            bits = bits | (readByte() << bitsCount);
            bitsCount += 8;
        }
    }
}