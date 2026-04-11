package org.memoxiiii.portalgeyser.packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Utility class for reading/writing Portal protocol binary data.
 * All multi-byte values are little-endian. Strings are length-prefixed with a uint32 LE length.
 * UUIDs are 16 bytes in standard (big-endian) byte order.
 */
public final class PacketBuffer {
    private final ByteArrayOutputStream out;
    private final ByteArrayInputStream in;

    private PacketBuffer(ByteArrayOutputStream out, ByteArrayInputStream in) {
        this.out = out;
        this.in = in;
    }

    /**
     * Creates a writer buffer for encoding packets.
     */
    public static PacketBuffer writer() {
        return new PacketBuffer(new ByteArrayOutputStream(256), null);
    }

    /**
     * Creates a reader buffer for decoding packets from the given data.
     */
    public static PacketBuffer reader(byte[] data) {
        return new PacketBuffer(null, new ByteArrayInputStream(data));
    }

    // --- Writer methods ---

    public void writeUint16(int value) {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
    }

    public void writeUint32(long value) {
        out.write((int) (value & 0xFF));
        out.write((int) ((value >> 8) & 0xFF));
        out.write((int) ((value >> 16) & 0xFF));
        out.write((int) ((value >> 24) & 0xFF));
    }

    public void writeInt64(long value) {
        byte[] buf = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(value).array();
        out.write(buf, 0, 8);
    }

    public void writeByte(int value) {
        out.write(value & 0xFF);
    }

    public void writeBool(boolean value) {
        out.write(value ? 1 : 0);
    }

    public void writeVaruint32(long value) {
        value &= 0xFFFFFFFFL;
        while (value > 0x7F) {
            out.write((int) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        out.write((int) (value & 0x7F));
    }

    public void writeString(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVaruint32(bytes.length);
        out.write(bytes, 0, bytes.length);
    }

    public void writeUUID(UUID uuid) {
        ByteBuffer buf = ByteBuffer.allocate(16);
        buf.putLong(uuid.getMostSignificantBits());
        buf.putLong(uuid.getLeastSignificantBits());
        byte[] bytes = buf.array();
        out.write(bytes, 0, 16);
    }

    public byte[] toByteArray() {
        return out.toByteArray();
    }

    // --- Reader methods ---

    public int readUint16() throws IOException {
        int b1 = in.read();
        int b2 = in.read();
        if (b1 < 0 || b2 < 0) throw new IOException("Unexpected end of data");
        return (b1 & 0xFF) | ((b2 & 0xFF) << 8);
    }

    public long readUint32() throws IOException {
        byte[] buf = readBytes(4);
        return (buf[0] & 0xFFL) | ((buf[1] & 0xFFL) << 8) | ((buf[2] & 0xFFL) << 16) | ((buf[3] & 0xFFL) << 24);
    }

    public long readInt64() throws IOException {
        byte[] buf = readBytes(8);
        return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }

    public int readByte() throws IOException {
        int b = in.read();
        if (b < 0) throw new IOException("Unexpected end of data");
        return b & 0xFF;
    }

    public boolean readBool() throws IOException {
        return readByte() != 0;
    }

    public long readVaruint32() throws IOException {
        long result = 0;
        for (int shift = 0; shift < 35; shift += 7) {
            int b = in.read();
            if (b < 0) throw new IOException("Unexpected end of varuint32");
            result |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) return result & 0xFFFFFFFFL;
        }
        throw new IOException("Varuint32 too large");
    }

    public String readString() throws IOException {
        long length = readVaruint32();
        if (length > 1024 * 1024) throw new IOException("String too large: " + length);
        byte[] bytes = readBytes((int) length);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public UUID readUUID() throws IOException {
        byte[] bytes = readBytes(16);
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        return new UUID(buf.getLong(), buf.getLong());
    }

    private byte[] readBytes(int count) throws IOException {
        byte[] buf = new byte[count];
        int offset = 0;
        while (offset < count) {
            int read = in.read(buf, offset, count - offset);
            if (read < 0) throw new IOException("Unexpected end of data");
            offset += read;
        }
        return buf;
    }
}
