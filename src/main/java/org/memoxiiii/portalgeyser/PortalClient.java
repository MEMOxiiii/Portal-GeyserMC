package org.memoxiiii.portalgeyser;

import org.memoxiiii.portalgeyser.packet.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Core Portal proxy client. Manages the socket thread and provides high-level
 * API methods for transferring players, querying server lists, finding players, etc.
 */
public class PortalClient {

    @FunctionalInterface
    public interface TransferCallback {
        void onResponse(UUID playerUUID, int status, String error);
    }

    @FunctionalInterface
    public interface PlayerInfoCallback {
        void onResponse(UUID playerUUID, int status, String xuid, String address);
    }

    @FunctionalInterface
    public interface ServerListCallback {
        void onResponse(List<ServerEntry> servers);
    }

    @FunctionalInterface
    public interface FindPlayerCallback {
        void onResponse(UUID playerUUID, String playerName, boolean online, String server);
    }

    @FunctionalInterface
    public interface LatencyHandler {
        void onLatencyUpdate(UUID playerUUID, long latency);
    }

    private final Logger logger;
    private final String serverName;
    private volatile String serverAddress;

    private SocketThread socketThread;
    private volatile boolean connected = false;

    private final Map<UUID, TransferCallback> transferCallbacks = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerInfoCallback> playerInfoCallbacks = new ConcurrentHashMap<>();
    private final List<ServerListCallback> serverListCallbacks = new CopyOnWriteArrayList<>();
    private final Map<String, FindPlayerCallback> findPlayerCallbacks = new ConcurrentHashMap<>();
    private final Map<UUID, Long> playerLatencies = new ConcurrentHashMap<>();

    private volatile LatencyHandler latencyHandler;

    public PortalClient(Logger logger, String serverName, String serverAddress) {
        this.logger = logger;
        this.serverName = serverName;
        this.serverAddress = serverAddress;
    }

    /**
     * Updates the server address. Used when the address is resolved after initial creation.
     */
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    /**
     * Starts the socket thread and begins connecting to the proxy.
     */
    public void connect(String host, int port, String secret) {
        socketThread = new SocketThread(host, port, secret, serverName, logger);
        socketThread.start();
    }

    /**
     * Must be called periodically (e.g. from a scheduled task) to process
     * incoming packets from the socket thread.
     */
    public void tick() {
        if (socketThread == null) return;

        byte[] data;
        while ((data = socketThread.pollReceived()) != null) {
            try {
                handleRawPacket(data);
            } catch (Exception e) {
                logger.warning("Error processing Portal packet: " + e.getMessage());
            }
        }
    }

    private void handleRawPacket(byte[] data) throws IOException {
        PacketBuffer buf = PacketBuffer.reader(data);
        int packetId = buf.readUint16();

        Packet packet = PacketPool.createPacket(packetId);
        if (packet == null) {
            logger.warning("Received unknown Portal packet ID: 0x" + Integer.toHexString(packetId));
            return;
        }

        packet.decode(buf);
        handlePacket(packet);
    }

    private void handlePacket(Packet packet) {
        if (packet instanceof AuthResponsePacket pk) {
            handleAuthResponse(pk);
        } else if (packet instanceof TransferResponsePacket pk) {
            handleTransferResponse(pk);
        } else if (packet instanceof PlayerInfoResponsePacket pk) {
            handlePlayerInfoResponse(pk);
        } else if (packet instanceof ServerListResponsePacket pk) {
            handleServerListResponse(pk);
        } else if (packet instanceof FindPlayerResponsePacket pk) {
            handleFindPlayerResponse(pk);
        } else if (packet instanceof UpdatePlayerLatencyPacket pk) {
            handleUpdatePlayerLatency(pk);
        }
    }

    private void handleAuthResponse(AuthResponsePacket pk) {
        if (pk.getStatus() != ProtocolInfo.AUTH_SUCCESS) {
            String reason = switch (pk.getStatus()) {
                case ProtocolInfo.AUTH_UNSUPPORTED_PROTOCOL ->
                        "Unsupported protocol version, proxy expects " + pk.getProtocol() + ", we have " + ProtocolInfo.PROTOCOL_VERSION;
                case ProtocolInfo.AUTH_INCORRECT_SECRET -> "Incorrect secret provided";
                case ProtocolInfo.AUTH_ALREADY_CONNECTED -> "Client with this name already connected";
                case ProtocolInfo.AUTH_UNAUTHENTICATED -> "Attempted to send packets whilst not authenticated";
                default -> "Unknown auth error status: " + pk.getStatus();
            };
            logger.severe("Portal authentication failed: " + reason);
            return;
        }

        logger.info("Authenticated with Portal proxy");
        connected = true;

        // Register our server address with the proxy
        socketThread.addPacketToQueue(new RegisterServerPacket(serverAddress));
        logger.info("Registered server '" + serverName + "' with address " + serverAddress);
    }

    private void handleTransferResponse(TransferResponsePacket pk) {
        TransferCallback cb = transferCallbacks.remove(pk.getPlayerUUID());
        if (cb != null) {
            cb.onResponse(pk.getPlayerUUID(), pk.getStatus(), pk.getError());
        }
    }

    private void handlePlayerInfoResponse(PlayerInfoResponsePacket pk) {
        PlayerInfoCallback cb = playerInfoCallbacks.remove(pk.getPlayerUUID());
        if (cb != null) {
            cb.onResponse(pk.getPlayerUUID(), pk.getStatus(), pk.getXuid(), pk.getAddress());
        }
    }

    private void handleServerListResponse(ServerListResponsePacket pk) {
        List<ServerListCallback> cbs = new ArrayList<>(serverListCallbacks);
        serverListCallbacks.clear();
        for (ServerListCallback cb : cbs) {
            cb.onResponse(pk.getServers());
        }
    }

    private void handleFindPlayerResponse(FindPlayerResponsePacket pk) {
        String server = pk.isOnline() ? pk.getServer() : "";

        // Try by UUID first, then by name
        FindPlayerCallback cb = findPlayerCallbacks.remove(pk.getPlayerUUID().toString());
        if (cb == null) {
            cb = findPlayerCallbacks.remove(pk.getPlayerName().toLowerCase());
        }
        if (cb != null) {
            cb.onResponse(pk.getPlayerUUID(), pk.getPlayerName(), pk.isOnline(), server);
        }
    }

    private void handleUpdatePlayerLatency(UpdatePlayerLatencyPacket pk) {
        playerLatencies.put(pk.getPlayerUUID(), pk.getLatency());
        LatencyHandler handler = latencyHandler;
        if (handler != null) {
            handler.onLatencyUpdate(pk.getPlayerUUID(), pk.getLatency());
        }
    }

    // --- Public API ---

    /**
     * Transfers a player to another server via the proxy.
     */
    public void transferPlayer(UUID playerUUID, String server, TransferCallback callback) {
        if (callback != null) {
            transferCallbacks.put(playerUUID, callback);
        }
        socketThread.addPacketToQueue(new TransferRequestPacket(playerUUID, server));
    }

    /**
     * Requests information (XUID, IP address) about a player from the proxy.
     */
    public void requestPlayerInfo(UUID playerUUID, PlayerInfoCallback callback) {
        if (callback != null) {
            playerInfoCallbacks.put(playerUUID, callback);
        }
        socketThread.addPacketToQueue(new PlayerInfoRequestPacket(playerUUID));
    }

    /**
     * Requests the list of all servers connected to the proxy.
     */
    public void requestServerList(ServerListCallback callback) {
        if (callback != null) {
            boolean sendPacket = serverListCallbacks.isEmpty();
            serverListCallbacks.add(callback);
            if (!sendPacket) return;
        }
        socketThread.addPacketToQueue(new ServerListRequestPacket());
    }

    /**
     * Searches for a player across all proxy servers.
     * Pass null UUID to search by name only.
     */
    public void findPlayer(UUID playerUUID, String playerName, FindPlayerCallback callback) {
        if (callback != null) {
            if (playerUUID != null && !playerUUID.equals(new UUID(0, 0))) {
                findPlayerCallbacks.put(playerUUID.toString(), callback);
            } else {
                findPlayerCallbacks.put(playerName.toLowerCase(), callback);
            }
        }
        UUID uuid = playerUUID != null ? playerUUID : new UUID(0, 0);
        socketThread.addPacketToQueue(new FindPlayerRequestPacket(uuid, playerName));
    }

    /**
     * Sets the handler for player latency updates from the proxy.
     */
    public void setLatencyHandler(LatencyHandler handler) {
        this.latencyHandler = handler;
    }

    /**
     * Gets the latest known latency for a player, or -1 if unknown.
     */
    public long getPlayerLatency(UUID playerUUID) {
        Long latency = playerLatencies.get(playerUUID);
        return latency != null ? latency : -1;
    }

    /**
     * Tracks a player (for latency updates).
     */
    public void trackPlayer(UUID playerUUID) {
        playerLatencies.put(playerUUID, 0L);
    }

    /**
     * Untracks a player.
     */
    public void untrackPlayer(UUID playerUUID) {
        playerLatencies.remove(playerUUID);
    }

    public boolean isConnected() {
        return connected;
    }

    public String getServerName() {
        return serverName;
    }

    /**
     * Shuts down the socket thread.
     */
    public void shutdown() {
        connected = false;
        if (socketThread != null) {
            socketThread.shutdown();
        }
    }
}
