package org.memoxiiii.portalgeyser.packet;

import java.io.IOException;

public class RegisterServerPacket implements Packet {
    private String address;
    private boolean legacyAuth;
    private String group;
    private long weight;

    public RegisterServerPacket() {
    }

    public RegisterServerPacket(String address, boolean legacyAuth) {
        this(address, legacyAuth, "", 0);
    }

    public RegisterServerPacket(String address, boolean legacyAuth, String group, long weight) {
        this.address = address;
        this.legacyAuth = legacyAuth;
        this.group = group == null ? "" : group;
        this.weight = weight;
    }

    @Override
    public int getId() {
        return ProtocolInfo.REGISTER_SERVER;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeString(address);
        buf.writeBool(legacyAuth);
        buf.writeString(group == null ? "" : group);
        buf.writeVaruint32(weight);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        address = buf.readString();
        legacyAuth = buf.readBool();
        group = buf.readString();
        weight = buf.readVaruint32();
    }

    public String getAddress() {
        return address;
    }

    public boolean isLegacyAuth() {
        return legacyAuth;
    }

    public String getGroup() {
        return group;
    }

    public long getWeight() {
        return weight;
    }
}
