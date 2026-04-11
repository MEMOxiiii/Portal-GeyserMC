package org.memoxiiii.portalgeyser.packet;

import java.io.IOException;
import java.util.UUID;

public class UpdatePlayerLatencyPacket implements Packet {
    private UUID playerUUID;
    private long latency;

    @Override
    public int getId() {
        return ProtocolInfo.UPDATE_PLAYER_LATENCY;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeUUID(playerUUID);
        buf.writeInt64(latency);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        playerUUID = buf.readUUID();
        latency = buf.readInt64();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public long getLatency() {
        return latency;
    }
}
