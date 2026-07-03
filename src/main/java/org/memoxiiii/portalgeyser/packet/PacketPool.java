package org.memoxiiii.portalgeyser.packet;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Pool of packet constructors indexed by packet ID.
 */
public final class PacketPool {
    private static final Map<Integer, Supplier<Packet>> POOL = new HashMap<>();

    static {
        register(ProtocolInfo.AUTH_REQUEST, AuthRequestPacket::new);
        register(ProtocolInfo.AUTH_RESPONSE, AuthResponsePacket::new);
        register(ProtocolInfo.REGISTER_SERVER, RegisterServerPacket::new);
        register(ProtocolInfo.TRANSFER_REQUEST, TransferRequestPacket::new);
        register(ProtocolInfo.TRANSFER_RESPONSE, TransferResponsePacket::new);
        register(ProtocolInfo.PLAYER_INFO_REQUEST, PlayerInfoRequestPacket::new);
        register(ProtocolInfo.PLAYER_INFO_RESPONSE, PlayerInfoResponsePacket::new);
        register(ProtocolInfo.SERVER_LIST_REQUEST, ServerListRequestPacket::new);
        register(ProtocolInfo.SERVER_LIST_RESPONSE, ServerListResponsePacket::new);
        register(ProtocolInfo.FIND_PLAYER_REQUEST, FindPlayerRequestPacket::new);
        register(ProtocolInfo.FIND_PLAYER_RESPONSE, FindPlayerResponsePacket::new);
        register(ProtocolInfo.UPDATE_PLAYER_LATENCY, UpdatePlayerLatencyPacket::new);
        register(ProtocolInfo.DISCONNECT_PLAYER, DisconnectPlayerPacket::new);
        register(ProtocolInfo.SET_SERVER_DRAINING, SetServerDrainingPacket::new);
    }

    private static void register(int id, Supplier<Packet> supplier) {
        POOL.put(id, supplier);
    }

    /**
     * Creates a new packet instance for the given ID, or null if unknown.
     */
    public static Packet createPacket(int id) {
        Supplier<Packet> supplier = POOL.get(id);
        return supplier != null ? supplier.get() : null;
    }

    private PacketPool() {
    }
}
