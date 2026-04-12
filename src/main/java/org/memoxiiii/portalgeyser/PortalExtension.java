package org.memoxiiii.portalgeyser;

import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.command.Command;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.event.bedrock.SessionDisconnectEvent;
import org.geysermc.geyser.api.event.bedrock.SessionJoinEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCommandsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPreInitializeEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserShutdownEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.memoxiiii.portalgeyser.command.ServerCommand;
import org.memoxiiii.portalgeyser.command.ServersCommand;
import org.memoxiiii.portalgeyser.command.TransferCommand;

import java.util.UUID;

/**
 * Portal-GeyserMC Extension — Connects GeyserMC servers to a Portal proxy.
 * Zero-latency packet dispatch for fast transfers and responsive commands.
 */
public class PortalExtension implements Extension {

    private PortalConfig config;
    private PortalClient client;

    @Subscribe
    public void onPreInitialize(GeyserPreInitializeEvent event) {
        config = PortalConfig.load(this.dataFolder());
        logger().info("Portal config loaded: server=" + config.getServerName());

        String serverAddress = config.getServerAddress();
        if (serverAddress == null || serverAddress.isEmpty()) {
            serverAddress = "";
        }

        client = new PortalClient(
                java.util.logging.Logger.getLogger("Portal-GeyserMC"),
                config.getServerName(),
                serverAddress
        );
    }

    @Subscribe
    public void onPostInitialize(GeyserPostInitializeEvent event) {
        // Resolve server address if not set in config
        String serverAddress = config.getServerAddress();
        if (serverAddress == null || serverAddress.isEmpty()) {
            String ip = config.getProxyAddress().equals("127.0.0.1") ? "127.0.0.1" :
                    GeyserApi.api().bedrockListener().address();
            int port = GeyserApi.api().bedrockListener().port();
            serverAddress = ip + ":" + port;
            client.setServerAddress(serverAddress);
        }

        // Set warmup delay so GeyserMC is fully ready before accepting transfers
        int warmupSeconds = config.getWarmupDelay();
        if (warmupSeconds > 0) {
            client.setWarmupDelay(warmupSeconds * 1000);
        }

        // Connect to the proxy — packets are dispatched immediately via blocking reads
        client.connect(config.getProxyAddress(), config.getSocketPort(), config.getSecret());
        logger().info("Connecting to Portal proxy at " + config.getProxyAddress() + ":" + config.getSocketPort());
    }

    @Subscribe
    public void onDefineCommands(GeyserDefineCommandsEvent event) {
        if (config == null || !config.isCommandsEnabled()) return;

        if (config.isTransferCommandEnabled()) {
            event.register(TransferCommand.create(this, client));
        }
        if (config.isServerCommandEnabled()) {
            event.register(ServerCommand.create(this, client));
        }
        if (config.isServersCommandEnabled()) {
            event.register(ServersCommand.create(this, client));
        }
    }

    @Subscribe
    public void onSessionJoin(SessionJoinEvent event) {
        GeyserConnection conn = event.connection();
        UUID uuid = conn.javaUuid();
        if (uuid != null && client != null) {
            client.trackPlayer(uuid);
        }
    }

    @Subscribe
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        GeyserConnection conn = event.connection();
        UUID uuid = conn.javaUuid();
        if (uuid != null && client != null) {
            client.untrackPlayer(uuid);
        }
    }

    @Subscribe
    public void onShutdown(GeyserShutdownEvent event) {
        if (client != null) {
            client.shutdown();
        }
        logger().info("Portal extension shut down");
    }

    /**
     * Returns the Portal client for API access from other extensions.
     */
    public PortalClient getClient() {
        return client;
    }

    /**
     * Returns the loaded configuration.
     */
    public PortalConfig getConfig() {
        return config;
    }
}
