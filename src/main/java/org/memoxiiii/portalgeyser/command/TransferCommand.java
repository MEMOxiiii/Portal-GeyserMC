package org.memoxiiii.portalgeyser.command;

import org.geysermc.geyser.api.command.Command;
import org.geysermc.geyser.api.command.CommandSource;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.extension.Extension;
import org.memoxiiii.portalgeyser.PortalClient;
import org.memoxiiii.portalgeyser.packet.ProtocolInfo;

import java.util.UUID;

/**
 * /transfer <server> [player] — Transfer yourself or another player to a server.
 */
public class TransferCommand {

    public static Command create(Extension extension, PortalClient client) {
        return Command.builder(extension)
                .name("transfer")
                .description("Transfer a player to another server")
                .permission("portal.command.transfer")
                .playerOnly(false)
                .source(CommandSource.class)
                .executor((source, command, args) -> {
                    if (args.length < 1) {
                        source.sendMessage("§cUsage: /transfer <server> [player]");
                        return;
                    }

                    String server = args[0];

                    if (args.length >= 2) {
                        // Transfer another player
                        String playerName = args[1];
                        transferByName(source, client, playerName, server);
                    } else {
                        // Transfer self — must be a player
                        GeyserConnection conn = source.connection();
                        if (conn == null) {
                            source.sendMessage("§cUsage: /transfer <server> <player>");
                            return;
                        }

                        UUID uuid = conn.javaUuid();
                        if (uuid == null) {
                            source.sendMessage("§cCould not determine your UUID");
                            return;
                        }

                        client.transferPlayer(uuid, server, (uid, status, error) -> {
                            switch (status) {
                                case ProtocolInfo.TRANSFER_SUCCESS ->
                                        source.sendMessage("§aYou were transferred to " + server);
                                case ProtocolInfo.TRANSFER_SERVER_NOT_FOUND ->
                                        source.sendMessage("§cServer '" + server + "' not found");
                                case ProtocolInfo.TRANSFER_ALREADY_ON_SERVER ->
                                        source.sendMessage("§cYou are already on that server");
                                case ProtocolInfo.TRANSFER_PLAYER_NOT_FOUND ->
                                        source.sendMessage("§cPlayer could not be found");
                                case ProtocolInfo.TRANSFER_ERROR ->
                                        source.sendMessage("§cTransfer error: " + error);
                            }
                        });
                    }
                })
                .build();
    }

    private static void transferByName(CommandSource source, PortalClient client, String playerName, String server) {
        // Search for the player on the proxy network
        client.findPlayer(null, playerName, (uuid, foundName, online, currentServer) -> {
            if (!online) {
                source.sendMessage("§cPlayer '" + playerName + "' could not be found");
                return;
            }

            client.transferPlayer(uuid, server, (uid, status, error) -> {
                switch (status) {
                    case ProtocolInfo.TRANSFER_SUCCESS -> {
                        source.sendMessage("§aPlayer '" + foundName + "' was transferred to " + server);
                    }
                    case ProtocolInfo.TRANSFER_SERVER_NOT_FOUND ->
                            source.sendMessage("§cServer '" + server + "' not found");
                    case ProtocolInfo.TRANSFER_ALREADY_ON_SERVER ->
                            source.sendMessage("§cPlayer is already on that server");
                    case ProtocolInfo.TRANSFER_PLAYER_NOT_FOUND ->
                            source.sendMessage("§cPlayer could not be found");
                    case ProtocolInfo.TRANSFER_ERROR ->
                            source.sendMessage("§cTransfer error: " + error);
                }
            });
        });
    }
}
