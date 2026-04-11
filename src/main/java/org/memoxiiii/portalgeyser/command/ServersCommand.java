package org.memoxiiii.portalgeyser.command;

import org.geysermc.geyser.api.command.Command;
import org.geysermc.geyser.api.command.CommandSource;
import org.geysermc.geyser.api.extension.Extension;
import org.memoxiiii.portalgeyser.PortalClient;
import org.memoxiiii.portalgeyser.packet.ServerEntry;

import java.util.List;
import java.util.stream.Collectors;

/**
 * /servers — List all servers connected to the proxy.
 */
public class ServersCommand {

    public static Command create(Extension extension, PortalClient client) {
        return Command.builder(extension)
                .name("servers")
                .description("List all servers connected to the proxy")
                .permission("portal.command.servers")
                .playerOnly(false)
                .source(CommandSource.class)
                .executor((source, command, args) -> {
                    client.requestServerList(servers -> {
                        if (servers.isEmpty()) {
                            source.sendMessage("§cNo servers connected to the proxy");
                            return;
                        }

                        String serverList = servers.stream()
                                .map(s -> s.getName() + " §a(" + s.getPlayerCount() + " players)§r")
                                .collect(Collectors.joining(", "));

                        source.sendMessage("There are §a" + servers.size() + "§r servers connected to the proxy: " + serverList);
                    });
                })
                .build();
    }
}
