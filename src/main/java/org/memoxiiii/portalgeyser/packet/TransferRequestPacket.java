package org.memoxiiii.portalgeyser.packet;

import java.io.IOException;
import java.util.UUID;

public class TransferRequestPacket implements Packet {
    private UUID playerUUID;
    private String server;

    public TransferRequestPacket() {
    }

    public TransferRequestPacket(UUID playerUUID, String server) {
        this.playerUUID = playerUUID;
        this.server = server;
    }

    @Override
    public int getId() {
        return ProtocolInfo.TRANSFER_REQUEST;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeUUID(playerUUID);
        buf.writeString(server);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        playerUUID = buf.readUUID();
        server = buf.readString();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getServer() {
        return server;
    }
}
