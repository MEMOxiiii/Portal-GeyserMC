package org.memoxiiii.portalgeyser.packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerListResponsePacket implements Packet {
    private final List<ServerEntry> servers = new ArrayList<>();

    @Override
    public int getId() {
        return ProtocolInfo.SERVER_LIST_RESPONSE;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeUint32(servers.size());
        for (ServerEntry entry : servers) {
            buf.writeString(entry.getName());
            buf.writeInt64(entry.getPlayerCount());
        }
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        long count = buf.readUint32();
        servers.clear();
        for (long i = 0; i < count; i++) {
            String name = buf.readString();
            long playerCount = buf.readInt64();
            servers.add(new ServerEntry(name, playerCount));
        }
    }

    public List<ServerEntry> getServers() {
        return servers;
    }
}
