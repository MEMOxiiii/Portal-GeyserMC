package org.memoxiiii.portalgeyser.packet;

import java.io.IOException;

public class ServerListRequestPacket implements Packet {
    @Override
    public int getId() {
        return ProtocolInfo.SERVER_LIST_REQUEST;
    }

    @Override
    public void encode(PacketBuffer buf) {
        // Empty payload
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        // Empty payload
    }
}
