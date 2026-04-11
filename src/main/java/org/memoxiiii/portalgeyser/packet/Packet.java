package org.memoxiiii.portalgeyser.packet;

import java.io.IOException;

/**
 * Represents a packet in the Portal socket protocol.
 */
public interface Packet {
    /**
     * Returns the packet ID.
     */
    int getId();

    /**
     * Encodes this packet's payload into the given buffer.
     */
    void encode(PacketBuffer buf);

    /**
     * Decodes this packet's payload from the given buffer.
     */
    void decode(PacketBuffer buf) throws IOException;
}
