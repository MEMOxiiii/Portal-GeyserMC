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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Portal-GeyserMC Extension — Connects GeyserMC servers to a Portal proxy.
 * Equivalent of PortalPM for PocketMine-MP servers.
 */
public class PortalExtension implements Extension {

    private PortalConfig config;
    private PortalClient client;
    private ScheduledExecutorService tickExecutor;

    @Subscribe
    public void onPreInitialize(GeyserPreInitializeEvent event) {
        // Load config before commands are defined
        config = PortalConfig.load(this.dataFolder());
        logger().info("Portal config loaded: server=" + config.getServerName());

        // Create the client early so commands can reference it.
        // We don't connect yet — that happens in onPostInitialize.
        String serverAddress = config.getServerAddress();
        if (serverAddress == null || serverAddress.isEmpty()) {
            serverAddress = ""; // Will be resolved in onPostInitialize
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

        // Connect to the proxy
        client.connect(config.getProxyAddress(), config.getSocketPort(), config.getSecret());
        logger().info("Connecting to Portal proxy at " + config.getProxyAddress() + ":" + config.getSocketPort());

        // Start a tick loop to process incoming packets
        tickExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Portal-TickThread");
            t.setDaemon(true);
            return t;
        });
        tickExecutor.scheduleAtFixedRate(() -> {
            try {
                client.tick();
            } catch (Exception e) {
                logger().warning("Error in Portal tick: " + e.getMessage());
            }
        }, 50, 50, TimeUnit.MILLISECONDS);
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
        if (tickExecutor != null) {
            tickExecutor.shutdown();
        }
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
