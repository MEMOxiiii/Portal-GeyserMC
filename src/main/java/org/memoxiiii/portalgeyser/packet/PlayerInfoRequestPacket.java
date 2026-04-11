package org.memoxiiii.portalgeyser.packet;

import java.io.IOException;
import java.util.UUID;

public class PlayerInfoRequestPacket implements Packet {
    private UUID playerUUID;

    public PlayerInfoRequestPacket() {
    }

    public PlayerInfoRequestPacket(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    @Override
    public int getId() {
        return ProtocolInfo.PLAYER_INFO_REQUEST;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeUUID(playerUUID);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        playerUUID = buf.readUUID();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }
}
