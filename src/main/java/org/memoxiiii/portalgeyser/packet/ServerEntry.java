package org.memoxiiii.portalgeyser.packet;

/**
 * Represents a server connected to the Portal proxy.
 */
public class ServerEntry {
    private final String name;
    private final long playerCount;

    public ServerEntry(String name, long playerCount) {
        this.name = name;
        this.playerCount = playerCount;
    }

    public String getName() {
        return name;
    }

    public long getPlayerCount() {
        return playerCount;
    }
}
