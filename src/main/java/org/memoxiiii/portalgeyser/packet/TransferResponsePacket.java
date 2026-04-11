package org.memoxiiii.portalgeyser.packet;

import java.io.IOException;
import java.util.UUID;

public class TransferResponsePacket implements Packet {
    private UUID playerUUID;
    private int status;
    private String error = "";

    @Override
    public int getId() {
        return ProtocolInfo.TRANSFER_RESPONSE;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeUUID(playerUUID);
        buf.writeByte(status);
        if (status == ProtocolInfo.TRANSFER_ERROR) {
            buf.writeString(error);
        }
    }

    @Override
    public void decode(PacketBuffer buf) throws IOException {
        playerUUID = buf.readUUID();
        status = buf.readByte();
        if (status == ProtocolInfo.TRANSFER_ERROR) {
            error = buf.readString();
        }
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }
}
