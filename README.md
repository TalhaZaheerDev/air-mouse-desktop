# AirMouse Server (Desktop App)

A lightweight Java desktop application that turns your phone into a wireless
trackpad. AirMouse Server listens for a TCP connection from the AirMouse
mobile app and translates incoming touch gestures into real mouse movement,
clicks, and scrolling on your computer using `java.awt.Robot`.

## Features

- **Low-latency TCP server** — `TCP_NODELAY` enabled, byte-level command
  parsing (no `String.split()` on the hot path), zero artificial delay on
  `Robot` actions.
- **Sub-pixel movement accumulation** — small, frequent touch deltas are
  accumulated so no movement is lost to integer rounding.
- **Adjustable cursor sensitivity** — live slider in the UI, persisted
  between launches.
- **System tray support** — minimize to tray and keep running in the
  background; double-click the tray icon to restore the window.
- **Launch on startup (Windows)** — optional "Start with Windows" toggle
  that creates/removes a startup shortcut automatically.
- **Auto network detection** — detects your machine's active LAN IP
  (filtering out VMware/VirtualBox/Docker/loopback adapters) and displays
  it along with the connection type (Wi-Fi / Ethernet) so you can quickly
  point the phone app at the right address.
- **Live log panel** — see every command received from the phone in
  real time, capped to the last 100 entries.

## Requirements

- Java 17 or later (uses modern `switch` expressions)
- Windows, macOS, or Linux with a desktop environment (uses `java.awt.Robot`
  and `SystemTray`, both of which require a graphical session)
- Phone and computer on the same local network (same Wi-Fi/LAN)

> **Note:** The "Start with Windows" auto-launch feature is Windows-specific
> (it writes a `.vbs` shortcut into the Windows Startup folder). On macOS/Linux
> the checkbox has no effect.

## Getting Started

1. Build or download the server jar (see [Building](#building) below).
2. Run it:
   ```bash
   java -jar airmouse-server-1.0.0-RC1.jar
   ```
3. The window shows your computer's **Desktop IP** and **Port** (default
   port: `5000`). Click **Copy IP** to copy `ip:port` to your clipboard.
4. Open the AirMouse app on your phone, enter that IP and port, and tap
   **Connect**.
5. Once connected, the status dot turns green and the phone's touchpad is
   live.

## Building

This is a standard Maven project.

```bash
mvn clean package
```

The resulting jar will be under `target/`. If you're packaging a native
`.exe` wrapper, place it alongside the jar in the same working directory —
the auto-startup feature looks for `AirMouse Server.exe` first and falls
back to launching the jar via `java -jar` if it isn't found.

## Project Structure

```
com.airmouse.server
├── Main.java              Entry point — wires up UI, tray, and server on the EDT
├── AirMouseWindow.java     Swing UI (status card, IP/port display, sensitivity
│                           slider, startup toggle, log panel)
├── TrayManager.java        System tray icon + popup menu (Open / Restart / Exit)
├── Server.java             TCP accept loop, one client at a time
├── ClientHandler.java      Per-connection stream reader + command parser
├── MouseController.java    Translates parsed commands into Robot mouse actions
├── ServerListener.java     Callback interface: Server/ClientHandler → UI
├── SettingsManager.java    Persists sensitivity + auto-start preference
└── NetworkUtils.java       Detects active LAN IP and connection type
```

## Wire Protocol

The server listens on a single TCP socket and reads newline-terminated
ASCII commands. One client is served at a time; when a client disconnects,
the server goes back to waiting for the next connection.

| Command       | Meaning                          | Example        |
|---------------|-----------------------------------|----------------|
| `M,dx,dy`     | Move the cursor by `(dx, dy)`     | `M,3.5,-1.2`   |
| `C,L`         | Left click                        | `C,L`          |
| `C,R`         | Right click                       | `C,R`          |
| `S,delta`     | Scroll by `delta`                 | `S,-4.0`       |

Each command is terminated with `\n`. `dx`, `dy`, and `delta` are decimal
numbers (a leading `-` is supported). Cursor sensitivity is applied
server-side before movement is executed, and the setting is adjustable live
from the UI without needing to reconnect.

> Earlier revisions of this protocol included a `D,START` / `D,END` pair for
> hold-to-drag. Drag support has been removed to eliminate an entire class
> of "stuck mouse button" bugs that could occur if a client disconnected
> mid-drag. If you're integrating a custom client, do not send `D,*`
> commands — they are no longer recognized.

## Troubleshooting

- **Phone can't connect:** Confirm both devices are on the same network,
  no firewall is blocking inbound TCP on the configured port, and the IP
  shown in the server window matches what you typed on the phone.
- **Right-click works but left-click doesn't (or vice versa):** Restart
  the server. This was a known issue in earlier builds tied to drag-state
  tracking; it's resolved as of this version since drag support was removed
  entirely and `leftClick()`/`rightClick()` no longer depend on any shared
  mutable state.
- **Cursor feels too sensitive / sluggish:** Adjust the **Cursor
  Sensitivity** slider in the app — the range is 0.5x–3.0x, and the setting
  is saved automatically.

## License

This project is free to use for everyone
@TalhaZaheerDev
