package org.memoxiiii.portalgeyser.packet;

import java.io.IOException;

/**
 * Sent by the proxy to a server to request that it disconnects any existing session
 * for a specific player. Used before transferring a player to prevent stale sessions.
 */
public class DisconnectPlayerPacket implements Packet {

    private String playerName = "";

    @Override
    public int getId() {
        return ProtocolInfo.DISCONNECT_PLAYER;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeString(playerName);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        playerName = buf.readString();
    }

    public String getPlayerName() {
        return playerName;
    }
}
