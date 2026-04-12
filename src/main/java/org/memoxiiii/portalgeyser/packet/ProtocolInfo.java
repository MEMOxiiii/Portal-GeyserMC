package org.memoxiiii.portalgeyser.packet;

/**
 * Protocol constants for the Portal proxy socket communication.
 */
public final class ProtocolInfo {
    public static final int PROTOCOL_VERSION = 1;

    public static final int AUTH_REQUEST = 0x00;
    public static final int AUTH_RESPONSE = 0x01;
    public static final int REGISTER_SERVER = 0x02;
    public static final int TRANSFER_REQUEST = 0x03;
    public static final int TRANSFER_RESPONSE = 0x04;
    public static final int PLAYER_INFO_REQUEST = 0x05;
    public static final int PLAYER_INFO_RESPONSE = 0x06;
    public static final int SERVER_LIST_REQUEST = 0x07;
    public static final int SERVER_LIST_RESPONSE = 0x08;
    public static final int FIND_PLAYER_REQUEST = 0x09;
    public static final int FIND_PLAYER_RESPONSE = 0x0A;
    public static final int UPDATE_PLAYER_LATENCY = 0x0B;
    public static final int DISCONNECT_PLAYER = 0x0C;

    // Auth response statuses
    public static final byte AUTH_SUCCESS = 0;
    public static final byte AUTH_UNSUPPORTED_PROTOCOL = 1;
    public static final byte AUTH_INCORRECT_SECRET = 2;
    public static final byte AUTH_ALREADY_CONNECTED = 3;
    public static final byte AUTH_UNAUTHENTICATED = 4;

    // Transfer response statuses
    public static final byte TRANSFER_SUCCESS = 0;
    public static final byte TRANSFER_SERVER_NOT_FOUND = 1;
    public static final byte TRANSFER_ALREADY_ON_SERVER = 2;
    public static final byte TRANSFER_PLAYER_NOT_FOUND = 3;
    public static final byte TRANSFER_ERROR = 4;

    // Player info response statuses
    public static final byte PLAYER_INFO_SUCCESS = 0;
    public static final byte PLAYER_INFO_PLAYER_NOT_FOUND = 1;

    private ProtocolInfo() {
    }
}
