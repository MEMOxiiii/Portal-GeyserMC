package org.memoxiiii.portalgeyser.packet;

import java.io.IOException;

public class AuthRequestPacket implements Packet {
    private long protocol;
    private String secret;
    private String name;

    public AuthRequestPacket() {
    }

    public AuthRequestPacket(long protocol, String secret, String name) {
        this.protocol = protocol;
        this.secret = secret;
        this.name = name;
    }

    @Override
    public int getId() {
        return ProtocolInfo.AUTH_REQUEST;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeUint32(protocol);
        buf.writeString(secret);
        buf.writeString(name);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        protocol = buf.readUint32();
        secret = buf.readString();
        name = buf.readString();
    }

    public long getProtocol() {
        return protocol;
    }

    public String getSecret() {
        return secret;
    }

    public String getName() {
        return name;
    }
}
