package org.memoxiiii.portalgeyser.packet;

import java.io.IOException;
import java.util.UUID;

public class FindPlayerResponsePacket implements Packet {
    private UUID playerUUID;
    private String playerName;
    private boolean online;
    private String server = "";

    @Override
    public int getId() {
        return ProtocolInfo.FIND_PLAYER_RESPONSE;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeUUID(playerUUID);
        buf.writeString(playerName);
        buf.writeBool(online);
        if (online) {
            buf.writeString(server);
        }
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        playerUUID = buf.readUUID();
        playerName = buf.readString();
        online = buf.readBool();
        if (online) {
            server = buf.readString();
        }
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isOnline() {
        return online;
    }

    public String getServer() {
        return server;
    }
}
