package org.memoxiiii.portalgeyser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple YAML-like config parser for the Portal extension.
 * Supports nested keys using dot notation internally, and flat/nested YAML reading.
 */
public class PortalConfig {
    private String proxyAddress = "127.0.0.1";
    private int socketPort = 19131;
    private String secret = "";
    private String serverName = "Hub1";
    private String serverAddress = "";
    private boolean commandsEnabled = true;
    private boolean transferCommand = true;
    private boolean serverCommand = true;
    private boolean serversCommand = true;

    /**
     * Loads configuration from a YAML file, creating it with defaults if missing.
     */
    public static PortalConfig load(Path dataFolder) {
        PortalConfig config = new PortalConfig();
        Path configFile = dataFolder.resolve("config.yml");

        if (!Files.exists(configFile)) {
            // Copy default config from resources
            try {
                Files.createDirectories(dataFolder);
                try (InputStream is = PortalConfig.class.getClassLoader().getResourceAsStream("config.yml")) {
                    if (is != null) {
                        Files.copy(is, configFile);
                    }
                }
            } catch (IOException e) {
                // Failed to create default config, use built-in defaults
            }
        }

        if (Files.exists(configFile)) {
            try {
                Map<String, String> values = parseYaml(configFile);
                config.proxyAddress = values.getOrDefault("proxy-address", config.proxyAddress);
                config.socketPort = parseInt(values.getOrDefault("socket.port", String.valueOf(config.socketPort)));
                config.secret = values.getOrDefault("socket.secret", config.secret);
                config.serverName = values.getOrDefault("server.name", config.serverName);
                config.serverAddress = values.getOrDefault("server.address", config.serverAddress);
                config.commandsEnabled = parseBool(values.getOrDefault("command.enable", "true"));
                config.transferCommand = parseBool(values.getOrDefault("command.commands.transfer", "true"));
                config.serverCommand = parseBool(values.getOrDefault("command.commands.server", "true"));
                config.serversCommand = parseBool(values.getOrDefault("command.commands.servers", "true"));
            } catch (IOException e) {
                // Use defaults
            }
        }

        return config;
    }

    /**
     * Simple YAML parser that flattens nested keys with dots.
     */
    private static Map<String, String> parseYaml(Path file) throws IOException {
        Map<String, String> result = new LinkedHashMap<>();
        String[] prefixStack = new String[10];
        int[] indentStack = new int[10];
        int depth = 0;

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comments and empty lines
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

                // Calculate indentation
                int indent = 0;
                for (char c : line.toCharArray()) {
                    if (c == ' ') indent++;
                    else break;
                }

                // Pop prefix stack to match current indent level
                while (depth > 0 && indent <= indentStack[depth - 1]) {
                    depth--;
                }

                int colonIdx = trimmed.indexOf(':');
                if (colonIdx < 0) continue;

                String key = trimmed.substring(0, colonIdx).trim();
                String value = trimmed.substring(colonIdx + 1).trim();

                // Remove quotes from values
                if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                    value = value.substring(1, value.length() - 1);
                }

                // Build full key path
                StringBuilder fullKey = new StringBuilder();
                for (int i = 0; i < depth; i++) {
                    fullKey.append(prefixStack[i]).append(".");
                }
                fullKey.append(key);

                if (value.isEmpty()) {
                    // This is a parent key - push to stack
                    if (depth < prefixStack.length) {
                        prefixStack[depth] = key;
                        indentStack[depth] = indent;
                        depth++;
                    }
                } else {
                    result.put(fullKey.toString(), value);
                }
            }
        }

        return result;
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static boolean parseBool(String value) {
        return "true".equalsIgnoreCase(value.trim());
    }

    // --- Getters ---

    public String getProxyAddress() {
        return proxyAddress;
    }

    public int getSocketPort() {
        return socketPort;
    }

    public String getSecret() {
        return secret;
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public boolean isCommandsEnabled() {
        return commandsEnabled;
    }

    public boolean isTransferCommandEnabled() {
        return transferCommand;
    }

    public boolean isServerCommandEnabled() {
        return serverCommand;
    }

    public boolean isServersCommandEnabled() {
        return serversCommand;
    }
}
