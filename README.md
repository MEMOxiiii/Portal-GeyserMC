<p align="center">
  <img src="https://geysermc.org/img/geyser-1760-860.png" height="80" alt="GeyserMC"/>
  <br/>
  <strong>Portal-GeyserMC</strong>
</p>

<p align="center">
  <a href="https://github.com/MEMOxiiii/Portal-Geyser/releases"><img src="https://img.shields.io/github/v/release/MEMOxiiii/Portal-Geyser?style=flat-square&color=%2300b894" alt="Release"></a>
  <a href="https://github.com/MEMOxiiii/Portal-Geyser/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-MIT-0984e3?style=flat-square" alt="License"></a>
  <img src="https://img.shields.io/badge/Java-17+-e17055?style=flat-square&logo=openjdk&logoColor=white" alt="Java 17+">
  <img src="https://img.shields.io/badge/Geyser_API-2.9.5-6c5ce7?style=flat-square" alt="Geyser API 2.9.5">
</p>

<p align="center">
  A <a href="https://geysermc.org/">GeyserMC</a> extension that bridges Bedrock servers to the <a href="https://github.com/paroxity/portal">Portal</a> proxy network — enabling seamless cross-server transfers for Bedrock Edition players.
</p>

---

## Overview

**Portal-GeyserMC** allows any server running GeyserMC to join a Portal proxy network alongside [Dragonfly](https://github.com/df-mc/dragonfly) and [PocketMine-MP](https://github.com/pmmp/PocketMine-MP) servers. Players can freely transfer between all connected servers regardless of the underlying server software.

| Platform | Extension |
|:---|:---|
| Dragonfly (Go) | [PortalDF](https://github.com/MEMOxiiii/PortalDF) |
| PocketMine-MP (PHP) | [PortalPM](https://github.com/MEMOxiiii/PortalPM) |
| **GeyserMC (Java)** | **Portal-GeyserMC** ← you are here |

## Features

- **Proxy Integration** — Connects to the Portal proxy via TCP socket with automatic authentication and server registration
- **Auto-Reconnect** — Seamlessly reconnects to the proxy on connection loss
- **Player Transfers** — Transfer players between any servers on the network
- **Server Discovery** — List all connected servers and their player counts
- **Player Lookup** — Find which server any player is currently on
- **Player Info** — Query XUID and IP address of connected players
- **Latency Tracking** — Receive real-time player latency updates from the proxy
- **Built-in Commands** — `/transfer`, `/server`, `/servers` — all configurable
- **Developer API** — Expose the Portal client for use by other Geyser extensions

## Quick Start

### 1. Download

Grab the latest `Portal-GeyserMC-x.x.x.jar` from [Releases](https://github.com/MEMOxiiii/Portal-Geyser/releases), or [build from source](#building).

### 2. Install

Drop the JAR into Geyser's `extensions/` directory:

```
plugins/Geyser-Spigot/extensions/Portal-GeyserMC-1.0.0.jar
```

> Works on any Geyser platform — Spigot, Velocity, BungeeCord, Fabric, standalone, etc.

### 3. Configure

Start the server once to generate the config, then edit `extensions/portalgeyser/config.yml`:

```yaml
proxy-address: "127.0.0.1"

socket:
  port: 19131
  secret: "your-secret-here"     # Must match your Portal proxy config

server:
  name: "Survival"               # Unique name for this server on the network
  address: ""                    # Leave empty to auto-detect from Geyser

command:
  enable: true
  commands:
    transfer: true
    server: true
    servers: true
```

### 4. Run

Start the server. You should see:

```
[Portal-GeyserMC] Authenticated with Portal proxy
[Portal-GeyserMC] Registered server 'Survival' with address 127.0.0.1:19132
```

## Configuration Reference

| Key | Description | Default |
|:---|:---|:---|
| `proxy-address` | IP address of the Portal proxy | `127.0.0.1` |
| `socket.port` | Socket port to connect to | `19131` |
| `socket.secret` | Shared secret for authentication | `""` |
| `server.name` | This server's identifier on the network | `Hub1` |
| `server.address` | Address the proxy dials to reach this server (auto-detected if empty) | `""` |
| `command.enable` | Master toggle for all commands | `true` |
| `command.commands.*` | Toggle individual commands | `true` |

## Commands

All commands use the `/portalgeyser` prefix:

| Command | Description |
|:---|:---|
| `/portalgeyser help` | List all available commands |
| `/portalgeyser transfer <server>` | Transfer yourself to another server |
| `/portalgeyser transfer <server> <player>` | Transfer a specific player |
| `/portalgeyser server` | Show which server you're on |
| `/portalgeyser server <player>` | Show which server a player is on |
| `/portalgeyser servers` | List all servers and their player counts |

## Developer API

Other Geyser extensions can interact with the Portal network programmatically:

```java
PortalExtension portal = (PortalExtension) GeyserApi.api()
    .extensionManager().extension("portalgeyser");
PortalClient client = portal.getClient();

// Transfer a player to another server
client.transferPlayer(playerUUID, "SkyWars1", (uuid, status, error) -> {
    if (status == ProtocolInfo.TRANSFER_SUCCESS) {
        // Player transferred!
    }
});

// List all servers on the network
client.requestServerList(servers -> {
    servers.forEach(s ->
        System.out.println(s.getName() + " — " + s.getPlayerCount() + " online"));
});

// Find a player across the entire network
client.findPlayer(null, "Steve", (uuid, name, online, server) -> {
    if (online) System.out.println(name + " is on " + server);
});

// Get player info (XUID, IP)
client.requestPlayerInfo(playerUUID, (uuid, status, xuid, address) -> {
    System.out.println("XUID: " + xuid + " | IP: " + address);
});

// Read cached latency
long ms = client.getPlayerLatency(playerUUID);
```

### Transfer Status Codes

| Constant | Value | Meaning |
|:---|:---|:---|
| `TRANSFER_SUCCESS` | `0` | Success |
| `TRANSFER_SERVER_NOT_FOUND` | `1` | Target server doesn't exist |
| `TRANSFER_ALREADY_ON_SERVER` | `2` | Player is already there |
| `TRANSFER_PLAYER_NOT_FOUND` | `3` | Player not found on the network |
| `TRANSFER_ERROR` | `4` | Generic error (check error message) |

## Building

**Requirements:** Java 17+

```bash
# Clone
git clone https://github.com/MEMOxiiii/Portal-Geyser.git
cd Portal-Geyser

# Build
./gradlew build          # Linux / macOS
gradlew.bat build        # Windows
```

The compiled JAR is output to `build/libs/Portal-GeyserMC-1.0.0.jar`.

## Project Structure

```
Portal-GeyserMC/
├── build.gradle.kts               # Gradle build config
├── gradle.properties              # Extension metadata
├── src/main/java/.../
│   ├── PortalExtension.java       # Extension lifecycle & event handling
│   ├── PortalClient.java          # High-level API (transfer, query, find)
│   ├── PortalConfig.java          # YAML config loader
│   ├── SocketThread.java          # TCP socket I/O thread
│   ├── command/
│   │   ├── TransferCommand.java   # /transfer
│   │   ├── ServerCommand.java     # /server
│   │   └── ServersCommand.java    # /servers
│   └── packet/                    # Binary protocol implementation
│       ├── Packet.java            # Packet interface
│       ├── PacketBuffer.java      # LE binary reader/writer (varuint32)
│       ├── PacketPool.java        # Packet ID → class mapping
│       ├── ProtocolInfo.java      # Protocol constants & IDs
│       └── *Packet.java           # 12 packet types
└── src/main/resources/
    ├── extension.yml              # Geyser extension descriptor
    └── config.yml                 # Default config template
```

## Network Architecture

```
┌─────────────┐     ┌─────────────┐     ┌──────────────────┐
│  Dragonfly   │────▶│             │◀────│  PocketMine-MP   │
│  (PortalDF)  │     │   Portal    │     │   (PortalPM)     │
└─────────────┘     │   Proxy     │     └──────────────────┘
                    │             │
┌─────────────┐     │  :19131     │
│  GeyserMC   │────▶│  (socket)   │
│  (this ext) │     │             │
└─────────────┘     └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │   Bedrock   │
                    │   Players   │
                    └─────────────┘
```

## Related Projects

| Project | Description |
|:---|:---|
| [Portal](https://github.com/MEMOxiiii/portal) | The Go-based proxy that powers the network |
| [PortalDF](https://github.com/MEMOxiiii/PortalDF) | Portal client library for Dragonfly servers |
| [PortalPM](https://github.com/MEMOxiiii/PortalPM) | Portal plugin for PocketMine-MP servers |

## License

[MIT](LICENSE)
