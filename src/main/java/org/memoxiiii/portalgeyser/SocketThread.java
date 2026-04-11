package org.memoxiiii.portalgeyser;

import org.memoxiiii.portalgeyser.packet.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Background thread managing the TCP socket connection to the Portal proxy.
 * Handles connecting, reconnecting, sending packets, and reading responses.
 */
public class SocketThread extends Thread {
    private final String host;
    private final int port;
    private final String secret;
    private final String serverName;
    private final Logger logger;

    private final BlockingQueue<byte[]> sendQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<byte[]> receiveQueue = new LinkedBlockingQueue<>();

    private volatile boolean running = true;

    public SocketThread(String host, int port, String secret, String serverName, Logger logger) {
        super("Portal-SocketThread");
        this.host = host;
        this.port = port;
        this.secret = secret;
        this.serverName = serverName;
        this.logger = logger;
        this.setDaemon(true);
    }

    @Override
    public void run() {
        while (running) {
            try {
                connectAndProcess();
            } catch (Exception e) {
                if (running) {
                    logger.log(Level.WARNING, "Portal socket connection error: " + e.getMessage());
                }
            }

            if (!running) break;

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                break;
            }
            if (running) {
                logger.info("Reconnecting to Portal proxy...");
            }
        }
    }

    private void connectAndProcess() throws Exception {
        logger.info("Connecting to Portal proxy at " + host + ":" + port);

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 10000);
            socket.setTcpNoDelay(true);

            DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            // Send authentication request
            AuthRequestPacket auth = new AuthRequestPacket(ProtocolInfo.PROTOCOL_VERSION, secret, serverName);
            sendPacketDirect(dos, auth);

            // Set socket read timeout for non-blocking behavior
            socket.setSoTimeout(50);

            while (running && !socket.isClosed()) {
                // Send queued packets
                byte[] data;
                while ((data = sendQueue.poll()) != null) {
                    writeFrame(dos, data);
                }
                dos.flush();

                // Try reading packets
                try {
                    byte[] received = readFrame(dis);
                    if (received != null) {
                        receiveQueue.offer(received);
                    }
                } catch (java.net.SocketTimeoutException ignored) {
                    // No data available - continue loop
                } catch (EOFException e) {
                    logger.warning("Portal proxy connection closed");
                    return;
                }
            }
        }
    }

    private void sendPacketDirect(DataOutputStream dos, Packet packet) throws IOException {
        PacketBuffer buf = PacketBuffer.writer();
        // Write 2-byte LE packet ID header
        buf.writeUint16(packet.getId());
        packet.encode(buf);
        byte[] payload = buf.toByteArray();
        writeFrame(dos, payload);
        dos.flush();
    }

    private void writeFrame(DataOutputStream dos, byte[] data) throws IOException {
        // 4-byte LE length prefix
        byte[] lengthBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(data.length).array();
        dos.write(lengthBytes);
        dos.write(data);
    }

    private byte[] readFrame(DataInputStream dis) throws IOException {
        // Read 4-byte LE length
        byte[] lengthBytes = new byte[4];
        dis.readFully(lengthBytes);
        int length = ByteBuffer.wrap(lengthBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();

        if (length <= 0 || length > 1024 * 1024) {
            throw new IOException("Invalid packet length: " + length);
        }

        byte[] data = new byte[length];
        dis.readFully(data);
        return data;
    }

    /**
     * Queues an encoded packet for sending to the proxy.
     */
    public void addPacketToQueue(Packet packet) {
        PacketBuffer buf = PacketBuffer.writer();
        buf.writeUint16(packet.getId());
        packet.encode(buf);
        sendQueue.offer(buf.toByteArray());
    }

    /**
     * Polls the next received packet data, or null if none available.
     */
    public byte[] pollReceived() {
        return receiveQueue.poll();
    }

    public void shutdown() {
        running = false;
        this.interrupt();
    }
}
