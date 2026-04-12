package org.memoxiiii.portalgeyser;

import org.memoxiiii.portalgeyser.packet.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Background thread managing the TCP socket connection to the Portal proxy.
 * Uses blocking reads for zero-latency packet dispatch and direct synchronized writes.
 */
public class SocketThread extends Thread {
    private final String host;
    private final int port;
    private final String secret;
    private final String serverName;
    private final Logger logger;
    private final Consumer<byte[]> packetHandler;
    private final Runnable disconnectHandler;

    private volatile Socket currentSocket;
    private volatile DataOutputStream dos;
    private final Object writeLock = new Object();
    private volatile boolean running = true;

    public SocketThread(String host, int port, String secret, String serverName,
                        Logger logger, Consumer<byte[]> packetHandler, Runnable disconnectHandler) {
        super("Portal-SocketThread");
        this.host = host;
        this.port = port;
        this.secret = secret;
        this.serverName = serverName;
        this.logger = logger;
        this.packetHandler = packetHandler;
        this.disconnectHandler = disconnectHandler;
        this.setDaemon(true);
    }

    @Override
    public void run() {
        int backoff = 1000;
        while (running) {
            try {
                connectAndProcess();
                backoff = 1000;
            } catch (Exception e) {
                if (running) {
                    logger.log(Level.WARNING, "Portal socket error: " + e.getMessage());
                }
            }

            disconnectHandler.run();
            if (!running) break;

            try {
                Thread.sleep(backoff);
            } catch (InterruptedException e) {
                break;
            }
            backoff = Math.min(backoff * 2, 10000);
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
            socket.setKeepAlive(true);

            this.currentSocket = socket;
            DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream(), 8192));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), 8192));
            this.dos = out;

            logger.info("Connected to Portal proxy");

            // Send auth directly — no queue needed, we're in the socket thread
            AuthRequestPacket auth = new AuthRequestPacket(ProtocolInfo.PROTOCOL_VERSION, secret, serverName);
            sendPacketInternal(out, auth);

            // Blocking read loop — packets dispatched immediately with zero latency
            while (running && !socket.isClosed()) {
                byte[] frame = readFrame(dis);
                if (frame != null) {
                    try {
                        packetHandler.accept(frame);
                    } catch (Exception e) {
                        logger.warning("Error processing Portal packet: " + e.getMessage());
                    }
                }
            }
        } catch (EOFException e) {
            if (running) {
                logger.warning("Portal proxy connection closed");
            }
        } finally {
            this.dos = null;
            this.currentSocket = null;
        }
    }

    /**
     * Sends a packet immediately to the proxy. Thread-safe — can be called from any thread.
     */
    public void sendPacket(Packet packet) {
        PacketBuffer buf = PacketBuffer.writer();
        buf.writeUint16(packet.getId());
        packet.encode(buf);
        byte[] data = buf.toByteArray();

        synchronized (writeLock) {
            DataOutputStream out = this.dos;
            if (out != null) {
                try {
                    writeFrame(out, data);
                    out.flush();
                } catch (IOException e) {
                    logger.warning("Error sending Portal packet: " + e.getMessage());
                    closeSocket();
                }
            }
        }
    }

    private void sendPacketInternal(DataOutputStream out, Packet packet) throws IOException {
        PacketBuffer buf = PacketBuffer.writer();
        buf.writeUint16(packet.getId());
        packet.encode(buf);
        writeFrame(out, buf.toByteArray());
        out.flush();
    }

    private void writeFrame(DataOutputStream dos, byte[] data) throws IOException {
        byte[] lengthBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(data.length).array();
        dos.write(lengthBytes);
        dos.write(data);
    }

    private byte[] readFrame(DataInputStream dis) throws IOException {
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

    private void closeSocket() {
        Socket s = currentSocket;
        if (s != null) {
            try { s.close(); } catch (IOException ignored) {}
        }
    }

    public void shutdown() {
        running = false;
        closeSocket();
        this.interrupt();
    }
}
