package org.memoxiiii.portalgeyser.packet;

import java.io.IOException;

public class RegisterServerPacket implements Packet {
    private String address;
    private boolean legacyAuth;

    public RegisterServerPacket() {
    }

    public RegisterServerPacket(String address, boolean legacyAuth) {
        this.address = address;
        this.legacyAuth = legacyAuth;
    }

    @Override
    public int getId() {
        return ProtocolInfo.REGISTER_SERVER;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeString(address);
        buf.writeBool(legacyAuth);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        address = buf.readString();
        legacyAuth = buf.readBool();
    }

    public String getAddress() {
        return address;
    }

    public boolean isLegacyAuth() {
        return legacyAuth;
    }
}
