package org.memoxiiii.portalgeyser.packet;

import java.io.IOException;

/**
 * Sent by a server to tell the proxy whether load balancers should stop routing new players to it.
 * Players already connected to the server are unaffected. Typically sent before a planned restart or
 * deployment so the server can finish serving its current players before going down.
 */
public class SetServerDrainingPacket implements Packet {

    private boolean draining;

    public SetServerDrainingPacket() {
    }

    public SetServerDrainingPacket(boolean draining) {
        this.draining = draining;
    }

    @Override
    public int getId() {
        return ProtocolInfo.SET_SERVER_DRAINING;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeBool(draining);
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        draining = buf.readBool();
    }

    public boolean isDraining() {
        return draining;
    }
}
