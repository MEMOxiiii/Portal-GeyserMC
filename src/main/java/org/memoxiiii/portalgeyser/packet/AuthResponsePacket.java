package org.memoxiiii.portalgeyser.packet;

import java.io.IOException;

public class AuthResponsePacket implements Packet {
    private long protocol;
    private int status;

    @Override
    public int getId() {
        return ProtocolInfo.AUTH_RESPONSE;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeUint32(protocol);
        buf.writeByte(status);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        protocol = buf.readUint32();
        status = buf.readByte();
    }

    public long getProtocol() {
        return protocol;
    }

    public int getStatus() {
        return status;
    }
}
