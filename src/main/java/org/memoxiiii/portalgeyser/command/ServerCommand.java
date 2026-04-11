package org.memoxiiii.portalgeyser.command;

import org.geysermc.geyser.api.command.Command;
import org.geysermc.geyser.api.command.CommandSource;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.extension.Extension;
import org.memoxiiii.portalgeyser.PortalClient;

/**
 * /server [player] — Check which server you or another player is on.
 */
public class ServerCommand {

    public static Command create(Extension extension, PortalClient client) {
        return Command.builder(extension)
                .name("server")
                .description("Check which server a player is on")
                .permission("portal.command.server")
                .playerOnly(false)
                .source(CommandSource.class)
                .executor((source, command, args) -> {
                    String targetName;

                    if (args.length >= 1) {
                        targetName = args[0];
                    } else {
                        GeyserConnection conn = source.connection();
                        if (conn == null) {
                            source.sendMessage("§cUsage: /server <player>");
                            return;
                        }
                        targetName = source.name();
                    }

                    client.findPlayer(null, targetName, (uuid, playerName, online, server) -> {
                        if (!online) {
                            source.sendMessage("§cPlayer '" + playerName + "' could not be found");
                            return;
                        }

                        if (source.name().equalsIgnoreCase(playerName)) {
                            source.sendMessage("§aYou are currently on " + server);
                        } else {
                            source.sendMessage("§aPlayer '" + playerName + "' is currently on " + server);
                        }
                    });
                })
                .build();
    }
}
