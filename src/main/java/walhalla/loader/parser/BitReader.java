package walhalla.loader.parser;

import walhalla.loader.parser.io.BinaryReader;

class BitReader {
    private BinaryReader reader;
    private int bits;
    private int bitsCount;

    BitReader(BinaryReader reader) {
        this.reader = reader;
    }

    int readBit() {
        if (bitsCount == 0) {
            bits = reader.readUByte();
            bitsCount = 8;
        }
        int result = bits & 1;
        bits >>= 1;
        bitsCount--;
        return result;
    }

    int readBits(int n) {
        while (bitsCount < n) {
            bits |= reader.readUByte() << bitsCount;
            bitsCount += 8;
        }
        int result = bits & ((1 << n) - 1);
        bits >>= n;
        bitsCount -= n;
        return result;
    }

    int readUnary() {
        int n = 0;
        while (readBit() == 1) {
            n++;
        }
        return n;
    }
}
