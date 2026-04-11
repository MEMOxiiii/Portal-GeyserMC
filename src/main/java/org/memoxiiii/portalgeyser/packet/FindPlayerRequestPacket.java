package org.memoxiiii.portalgeyser.packet;

import java.io.IOException;
import java.util.UUID;

public class FindPlayerRequestPacket implements Packet {
    private UUID playerUUID;
    private String playerName;

    public FindPlayerRequestPacket() {
    }

    public FindPlayerRequestPacket(UUID playerUUID, String playerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
    }

    @Override
    public int getId() {
        return ProtocolInfo.FIND_PLAYER_REQUEST;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeUUID(playerUUID);
        buf.writeString(playerName);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        playerUUID = buf.readUUID();
        playerName = buf.readString();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }
}
