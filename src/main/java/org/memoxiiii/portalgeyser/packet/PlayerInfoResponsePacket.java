package org.memoxiiii.portalgeyser.packet;

import java.io.IOException;
import java.util.UUID;

public class PlayerInfoResponsePacket implements Packet {
    private UUID playerUUID;
    private int status;
    private String xuid = "";
    private String address = "";

    @Override
    public int getId() {
        return ProtocolInfo.PLAYER_INFO_RESPONSE;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeUUID(playerUUID);
        buf.writeByte(status);
        buf.writeString(xuid);
        buf.writeString(address);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        playerUUID = buf.readUUID();
        status = buf.readByte();
        xuid = buf.readString();
        address = buf.readString();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public int getStatus() {
        return status;
    }

    public String getXuid() {
        return xuid;
    }

    public String getAddress() {
        return address;
    }
}
