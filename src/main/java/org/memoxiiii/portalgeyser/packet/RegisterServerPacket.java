package org.memoxiiii.portalgeyser.packet;

import java.io.IOException;

public class RegisterServerPacket implements Packet {
    private String address;

    public RegisterServerPacket() {
    }

    public RegisterServerPacket(String address) {
        this.address = address;
    }

    @Override
    public int getId() {
        return ProtocolInfo.REGISTER_SERVER;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeString(address);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        address = buf.readString();
    }

    public String getAddress() {
        return address;
    }
}
