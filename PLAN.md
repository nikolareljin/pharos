# Pharos — Master Implementation Plan

> **Status:** Initial architecture and implementation roadmap  
> **Project type:** Public open-source Android application  
> **Primary reference target:** Amazon Fire TV Stick 4K (2nd Gen, 2023)  
> **Repository:** `pharos`  
> **Application name:** **Pharos**

## 1. Purpose

Pharos is a general-purpose, network-connected Android display and control node. Its first reference device is an Amazon Fire TV Stick 4K (2nd Gen), but it must not be designed as a Fire-TV-only application. The same app should be able to run on Fire TV, Android TV / Google TV, tablets, phones, Android kiosks, and similar Android-derived devices where technically practical.

Pharos turns inexpensive, old, spare, or dedicated Android hardware into a reusable endpoint for:

- dashboards and telemetry
- status displays
- alerts and emergency notices
- digital signage
- images, audio, and video
- camera feeds
- home/lab automation front ends
- network-monitoring front ends
- operations displays
- remote-controlled command surfaces
- AI-assisted interfaces
- kiosk applications
- shared-screen multiplayer/game experiences
- custom third-party integrations

Pharos is **not** itself a network scanner, observability backend, home automation server, AI server, NVR, or monitoring database. Those systems stay external and integrate through Pharos's public protocol.

**Core architectural rule:** external systems integrate with Pharos. Pharos must not contain hard-coded knowledge of private applications, private repositories, private APIs, or private infrastructure.

## 2. Public-project boundary

This repository is public. Never commit:

- private repository names or URLs
- internal hostnames or identifiable infrastructure details
- real credentials, API keys, passwords, certificates, tokens, cookies, or auth headers
- proprietary source code copied from other projects
- personal information
- private API schemas

All integrations must be generic and documented in terms of REST, WebSocket, MQTT, JSON, media URLs, or future public adapter interfaces.

## 3. Primary goals

Pharos shall:

1. provide a reliable full-screen display application for Android-derived devices;
2. support D-pad/remote-first navigation;
3. support touch where available;
4. support configurable providers;
5. receive structured content, events, alerts, metrics, commands, and configuration;
6. support push and pull models;
7. render reusable screens and widgets;
8. support multiple independently addressable nodes;
9. recover automatically from Wi-Fi, backend, and broker interruptions;
10. run unattended for long periods;
11. operate as a dedicated kiosk/display node without requiring root;
12. support local/standalone operation;
13. support secure authentication;
14. expose a stable versioned public protocol;
15. provide examples usable by third parties without modifying Pharos;
16. avoid mandatory Google Play Services;
17. run correctly on Fire OS 8 / API 30;
18. remain portable to standard Android and Android TV;
19. minimize idle CPU, memory, storage, and network use;
20. keep extension points generic so future use cases do not force a rewrite.

## 4. Non-goals for initial versions

Initial Pharos will not:

- require root or bootloader modification;
- replace the Fire TV launcher;
- bypass Amazon platform restrictions;
- execute arbitrary shell commands received over the network;
- provide arbitrary remote code execution;
- become a packet-capture or network-scanning engine;
- become an NVR;
- run a large modern LLM locally on the reference Fire TV;
- store unbounded telemetry or media history;
- expose unauthenticated control endpoints by default;
- require Google Mobile Services.

## 5. Naming

- App: **Pharos**
- Repository: `pharos`
- Recommended Android application ID: `io.github.nikolareljin.pharos`
- Suggested tagline: **A general-purpose Android display and control node for dashboards, telemetry, alerts, media, automation, and AI-assisted interfaces.**

The Android application ID should be finalized before a public store release because changing it later creates migration problems.

## 6. First reference hardware

Reference device: Amazon Fire TV Stick 4K (2nd Gen, 2023).

Expected characteristics:

- model/build: `AFTKM`
- Fire OS 8
- Android compatibility level/API: Android 11 / API 30
- MediaTek MT8696D
- quad-core Cortex-A55 CPU up to approximately 1.7 GHz
- 2 GB RAM
- 8 GB internal storage
- HDMI display output
- Wi-Fi 6
- Bluetooth remote/controller support
- hardware video decoding / 4K output capability

Official references should remain linked from future Fire TV documentation:

- https://developer.amazon.com/docs/device-specs/identify-fire-tv-devices.html
- https://developer.amazon.com/docs/device-specs/device-specifications-fire-tv-streaming-media-player.html
- https://developer.amazon.com/docs/fire-tv/fire-os-8.html
- https://developer.amazon.com/docs/fire-tv/fire-os-overview.html

Design for constrained hardware: bounded caches, bounded queues, bounded logs, no unbounded histories, no persistent raw video cache by default, no continuous heavy inference, and no assumptions of abundant free storage.

## 7. Reference-device development setup

### 7.1 Enable developer access

On Fire TV:

1. Settings
2. My Fire TV
3. About
4. if Developer Options are hidden, select the device name repeatedly until developer mode is enabled
5. return to My Fire TV
6. open Developer Options
7. enable ADB Debugging
8. enable installation from unknown sources for the chosen development workflow if required

Menu wording may change with Fire OS updates. Document actual observed behavior when development begins.

### 7.2 Determine IP

Use Fire TV network settings or the router/DHCP lease table.

### 7.3 Install ADB on Ubuntu/Xubuntu

```bash
sudo apt update
sudo apt install adb
adb version
```

### 7.4 Connect

```bash
adb connect <FIRE_TV_IP>:5555
adb devices
```

Approve the debugging workstation on the TV if prompted. Do not leave ADB enabled on an untrusted network.

### 7.5 Capture device facts

```bash
adb shell getprop ro.product.model
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.sdk
adb shell getprop ro.product.cpu.abi
adb shell cat /proc/meminfo
adb shell df -h
adb shell pm list features
```

Record sanitized results in future development docs. Expected reference model: `AFTKM`; expected API level: `30`.

## 8. Technology choices

### 8.1 Language

Use **Kotlin**.

### 8.2 UI

Use **Jetpack Compose** with TV/D-pad navigation treated as a first-class requirement. Every UI must also degrade sensibly to touch devices.

### 8.3 Core Android libraries

Initial candidates:

- Kotlin Coroutines / Flow
- kotlinx.serialization
- OkHttp
- OkHttp WebSocket or equivalent
- DataStore
- Room only where structured persistence justifies it
- AndroidX Media3
- Coil or another bounded image loader
- Compose Navigation
- JUnit
- Compose UI tests
- MockWebServer
- a maintained MQTT client validated on Android/Fire OS before committing to it

### 8.4 Google services rule

Do not require Google Play Services. Any Google-specific feature must be optional, isolated behind an interface, and not prevent Fire OS operation.

## 9. SDK strategy

Initial recommendation:

- `compileSdk`: current stable SDK supported by the chosen Android Gradle Plugin
- `targetSdk`: current store-compatible target where practical
- `minSdk`: initially 26, subject to review
- API 30 is mandatory physical-device acceptance coverage

Do not tie `minSdk` to the Fire TV reference device. Evaluate older Fire OS generations only if their value justifies additional compatibility work.

## 10. Target repository structure

Do not create empty modules solely to match this tree; split only when code exists.

```text
pharos/
├── PLAN.md
├── README.md
├── LICENSE
├── CONTRIBUTING.md
├── SECURITY.md
├── CODE_OF_CONDUCT.md
├── CHANGELOG.md
├── .gitignore
├── .editorconfig
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/
├── app/
├── core/
│   ├── model/
│   ├── protocol/
│   ├── runtime/
│   ├── persistence/
│   └── security/
├── data/
│   ├── http/
│   ├── websocket/
│   ├── mqtt/
│   └── local/
├── feature/
│   ├── onboarding/
│   ├── dashboard/
│   ├── alerts/
│   ├── media/
│   ├── camera/
│   ├── browser/
│   ├── settings/
│   ├── diagnostics/
│   └── ai/
├── docs/
├── schemas/
│   └── v1/
├── examples/
│   ├── curl/
│   ├── python/
│   ├── javascript/
│   ├── mqtt/
│   └── websocket/
└── .github/
    ├── workflows/
    ├── ISSUE_TEMPLATE/
    └── pull_request_template.md
```

## 11. Core architecture

Pharos is a **node**. A node has a stable generated identity, human-readable name, capabilities, provider connections, state, screens, alerts, and a safe command surface.

Conceptual flow:

```text
External systems
      |
 REST | WebSocket | MQTT
      |
Provider layer
      |
Protocol validation
      |
Message router
      +------------------+
      |                  |
  State store       Priority engine
      |                  |
      +--------+---------+
               |
          Screen runtime
               |
        Renderer registry
               |
        Jetpack Compose UI
```

UI observes application state. Transport code must not directly manipulate UI components.

## 12. Operating modes

### Managed mode

External controller determines what Pharos displays.

```text
Controller -> Pharos
```

Useful for centralized displays, signage, fleet-managed nodes, operations screens, and event-driven content.

### Standalone mode

Pharos directly fetches or subscribes to configured sources.

```text
Pharos -> REST / MQTT / media / web source
```

Standalone mode must not require a Pharos server.

### Hybrid mode

Local/default dashboards operate normally while managed high-priority events can interrupt them. This is likely the preferred long-term model.

## 13. Node identity

Generate and persist a random UUID on first launch. Do not derive node identity from MAC address, serial number, account ID, or other privacy-sensitive hardware identifier.

Allow users/controllers to assign:

- display name
- location label
- groups
- tags

Example:

```json
{
  "nodeId": "f72a6ef4-2a42-42a9-a39c-9e2dc4f87833",
  "name": "Living Room TV",
  "groups": ["home", "displays"],
  "tags": ["tv", "4k"]
}
```

## 14. Capability discovery

Nodes publish capabilities rather than forcing controllers to assume every Android device is identical.

Example:

```json
{
  "protocolVersion": "1.0",
  "nodeId": "f72a6ef4-2a42-42a9-a39c-9e2dc4f87833",
  "capabilities": {
    "display": true,
    "touch": false,
    "dpad": true,
    "audio": true,
    "video": true,
    "web": true,
    "camera": false,
    "microphone": false,
    "mqtt": true,
    "websocket": true
  }
}
```

## 15. Public protocol v1

The protocol is a major product surface and must be:

- versioned
- JSON-based initially
- transport-independent
- documented
- schema-validated
- backward-compatible within a major version
- safe by default

Suggested envelope:

```json
{
  "protocolVersion": "1.0",
  "id": "message-uuid",
  "timestamp": "2026-08-07T22:00:00Z",
  "type": "alert",
  "source": "example-controller",
  "target": {
    "nodeIds": [],
    "groups": []
  },
  "payload": {}
}
```

Initial message families:

- `content`
- `screen`
- `metric`
- `alert`
- `event`
- `command`
- `configuration`
- `status`
- `heartbeat`
- `acknowledgement`
- `capabilities`

JSON Schemas belong under `schemas/v1/` and become contract-test fixtures.

## 16. Content vs commands

Content describes what should be shown. Commands request a limited predefined application action.

Safe command candidates:

- `show_screen`
- `next_screen`
- `previous_screen`
- `refresh`
- `reload_provider`
- `play`
- `pause`
- `stop`
- `dismiss`
- `acknowledge`
- `open_settings`
- `request_status`
- platform-gated `set_volume`
- platform-gated `set_brightness`

Never add arbitrary shell execution, eval, executable download/run, unrestricted filesystem access, or unrestricted JavaScript bridges.

## 17. Dashboard model

Dashboards are declarative.

Initial layouts:

- grid
- rows
- columns
- hero + grid
- list
- full-screen
- split

Initial widget catalog:

- text
- metric
- status
- image
- clock
- list
- progress
- gauge
- countdown
- QR code
- chart
- media
- web
- camera
- alert feed

MVP widget set:

1. text
2. metric
3. status
4. image
5. clock
6. list

Unknown widget types must not crash a screen. Log a sanitized warning and optionally display an unsupported-widget placeholder.

## 18. Renderer registry

Use a typed renderer registry, conceptually:

```kotlin
interface WidgetRenderer<T : Widget> {
    val type: String

    @Composable
    fun Render(widget: T, context: RenderContext)
}
```

Renderer extension must be internal/source-level initially. Do not implement dynamic APK plugin loading in early versions.

## 19. Screen runtime

Each screen can have:

- ID
- title
- layout
- widgets
- optional duration
- optional refresh policy
- optional transition
- optional validity interval
- optional priority
- optional cache policy

Screens may be persistent, temporary, scheduled, provider-owned, or locally configured.

Support playlists later:

```json
{
  "playlist": [
    {"screenId": "overview", "durationSeconds": 30},
    {"screenId": "weather", "durationSeconds": 20},
    {"screenId": "network", "durationSeconds": 30}
  ]
}
```

Pause rotation on user interaction and resume after a configurable timeout.

## 20. Priority/interruption engine

This is a defining feature.

Suggested ranges:

```text
0-19     background/passive
20-39    normal information
40-59    warning
60-79    important alert
80-94    urgent
95-100   critical/emergency
```

Behavior:

1. record current screen/state;
2. qualifying alert interrupts;
3. show alert;
4. expire, dismiss, or acknowledge alert;
5. restore prior screen/state.

Handle:

- multiple simultaneous alerts
- equal-priority ordering
- TTL
- stale alerts
- sticky alerts
- acknowledgements
- deduplication keys
- source disconnect
- persistence across process restart where appropriate
- rate limits
- alert storms

## 21. Alert-storm protection

Implement:

- deduplication key
- per-source rate limiting
- maximum visible queue
- repeated-alert collapsing
- repetition count
- TTL
- stale-alert cleanup

A broken integration must not make Pharos unusable.

## 22. Remote and input model

TV interaction is first-class.

Support logical actions for:

- D-pad directions
- Select/OK
- Back
- Menu
- Play/Pause
- Rewind/Fast-forward where relevant
- keyboard
- gamepad
- touch

Create an input abstraction instead of hard-coding one Fire remote.

Focus management is a release-quality requirement:

- obvious focused state
- deterministic directional movement
- correct default focus
- modal focus containment
- focus restoration when returning to a screen
- no touch-only controls

## 23. HTTP provider

Use for polling external services.

Support:

- GET JSON initially
- configurable interval
- headers
- bearer-token auth
- optional basic auth
- query parameters
- timeouts
- exponential backoff + jitter
- ETag/conditional requests where useful
- TLS
- cache behavior
- connection/error state

Do not log tokens or sensitive headers.

Configuration should reference secrets rather than contain them directly.

## 24. WebSocket provider

Preferred for managed/push scenarios.

Support:

- authenticated connection
- reconnect with exponential backoff and jitter
- ping/pong
- bounded inbound queue
- validation before routing
- duplicate handling
- provider health state

States:

```text
DISABLED
CONNECTING
CONNECTED
DEGRADED
RECONNECTING
FAILED
```

## 25. MQTT provider

MQTT enables IoT, sensor, automation, and event-driven integration.

Initial target:

- MQTT 3.1.1
- MQTT 5 if chosen client is stable on Android/Fire OS
- TCP/TLS
- username/password or token-style credentials
- QoS 0/1 initially
- retained messages where semantically safe
- reconnect
- last-will status

Suggested topic convention:

```text
pharos/nodes/{nodeId}/content
pharos/nodes/{nodeId}/events
pharos/nodes/{nodeId}/commands
pharos/nodes/{nodeId}/config
pharos/nodes/{nodeId}/status
pharos/nodes/{nodeId}/heartbeat
pharos/groups/{groupId}/content
pharos/groups/{groupId}/events
pharos/groups/{groupId}/commands
pharos/broadcast/content
pharos/broadcast/events
```

Do not provide privileged broadcast commands by default.

## 26. Local provider / demo mode

Pharos must remain useful without infrastructure.

Provide local content such as:

- clock
- static message
- example metrics
- local device status
- image slideshow
- demo dashboard

A `Try Demo` first-launch path is important for public adoption. Demo mode should work offline and demonstrate screen rotation plus a sample alert.

## 27. Optional local control API

A later phase may expose:

```text
POST /api/v1/content
POST /api/v1/events
POST /api/v1/commands
GET  /api/v1/status
GET  /api/v1/capabilities
```

Security requirements:

- disabled by default
- explicit enable
- authentication required
- bind scope clearly shown
- rate limits
- payload size limits
- no arbitrary execution
- no secrets returned
- TLS or trusted-LAN/VPN deployment guidance

Do not include this in MVP unless necessary.

## 28. Persistence

Use DataStore for preferences and Room only when structured persistence is justified.

Persist:

- node ID
- node name
- provider definitions
- selected/default screen
- dashboard configuration
- non-sensitive settings
- last-valid cached screen where appropriate
- diagnostic counters

Do not persist unbounded telemetry, raw video, unbounded logs, or plaintext secrets in exportable config.

## 29. Offline behavior and reconnection

On disconnect:

1. keep rendering current/last-valid content;
2. show subtle connection status;
3. retain local screens;
4. retry with cancellable exponential backoff + jitter;
5. do not show blocking error loops;
6. preserve navigation;
7. expose diagnostics.

Conceptual retry sequence:

```text
1s, 2s, 4s, 8s, 15s, 30s, 60s capped
```

Reset after a stable connection. Jitter prevents fleets from reconnecting simultaneously.

## 30. Media

Use AndroidX Media3. Do not build custom decoders.

Initial media support:

- images
- audio
- HTTP video files
- HLS

Later evaluate:

- DASH
- RTSP
- live camera streams

Requirements:

- hardware decode where available
- graceful codec failures
- bounded buffers
- configurable autoplay
- mute/volume policy
- reconnect/recovery
- proper lifecycle/resource release

## 31. Camera/live streams

Pharos is a viewer, not an NVR.

Possible sources:

- HTTP snapshots
- HLS
- RTSP after compatibility validation
- MJPEG only if needed

Support single-camera view, camera grid, and event-triggered camera pop-up in later phases.

Credentials must never appear in logs or screenshots generated by diagnostics.

## 32. Web renderer

Optional restricted WebView for existing dashboards.

Security requirements:

- URL allowlist
- no arbitrary file access
- no insecure mixed content by default
- no unnecessary JavaScript bridges
- controlled navigation
- loading/error states
- configurable refresh

Treat web content as untrusted relative to native application commands.

## 33. Status and heartbeat

Node status can report:

- app version
- protocol version
- node ID
- uptime
- active screen
- provider states
- foreground/background state
- memory/storage pressure indicator
- display resolution
- capabilities
- timestamp

Avoid unnecessarily exposing unique hardware identifiers.

## 34. Diagnostics

Local diagnostics screen should show:

- app version
- protocol version
- node ID
- device model
- Android/API level
- memory/storage
- display dimensions
- network reachability
- provider states
- last successful connection
- last message received
- queue depths
- sanitized recent errors

Actions:

- reconnect providers
- clear cache
- export/copy sanitized diagnostics
- reset configuration with confirmation

## 35. Logging

Use structured logs with ERROR/WARN/INFO/DEBUG.

Production rules:

- no secrets
- no auth headers
- redact sensitive URL/query parts
- redact payload fields marked secret
- bounded rotating logs
- no indefinite storage growth

## 36. Security model

Threats include unauthorized display control, compromised LAN devices, malicious publishers, token theft, malformed/oversized JSON, alert floods, replayed commands, hostile web content, media parsing edge cases, and credential leakage.

Controls:

1. authenticate controllers/providers;
2. use TLS where feasible;
3. schema-validate all messages;
4. enforce payload size limits;
5. command allowlists;
6. log redaction;
7. secure secret storage;
8. no arbitrary execution;
9. rate limiting;
10. protect configuration changes;
11. anti-replay measures where privileged actions require them;
12. fail closed for privileged operations.

## 37. Secret management

Secrets include API tokens, MQTT credentials, WebSocket tokens, basic-auth credentials, and client private keys.

Use Android Keystore-backed storage where practical.

Normal configuration should reference secrets:

```json
{
  "secretRef": "mqtt-primary"
}
```

Exported configuration excludes secrets by default.

## 38. Pairing

Future managed-mode pairing flow:

1. install Pharos;
2. generate node ID;
3. show QR code/short-lived one-time pairing token;
4. controller scans/enters token;
5. establish association;
6. issue permanent credential;
7. invalidate one-time token.

Pairing tokens must be random, short-lived, and single-use.

## 39. Configuration UX

Initial onboarding:

1. Welcome
2. choose Standalone / Connect to Controller / Demo
3. configure provider
4. test connection
5. name device
6. choose default display
7. finish

Settings sections:

- Node
- Display
- Screens
- Providers
- Network
- Media
- Alerts
- Security
- Diagnostics
- About

Every settings screen must be remote navigable.

## 40. Kiosk reliability

Without root, support where the platform permits:

- launch into last/default screen
- immersive/full-screen presentation
- keep screen awake while active
- restore after process death
- restore prior content state
- optional boot launch if supported and user-enabled

Do not promise launcher replacement or guaranteed boot autostart across all Fire OS versions.

## 41. AI integration

AI is optional and external-first.

Possible model:

```text
Pharos -> configured AI endpoint
```

or

```text
External AI system -> Pharos protocol
```

Use cases:

- explain telemetry
- summarize alerts
- natural-language dashboard queries
- summarize event metadata
- generate display content
- provide recommendations

Do not assume the reference Fire TV can run a useful modern LLM locally.

Suggested interface:

```kotlin
interface AiProvider {
    suspend fun complete(request: AiRequest): AiResponse
    fun availability(): Flow<AiAvailability>
}
```

Potential providers: OpenAI-compatible HTTP endpoints, local LAN model servers, custom enterprise endpoints, or disabled/no-op.

Never automatically send camera frames, private dashboard data, credentials, or personal data to a remote AI service. Cloud AI must be explicit/opt-in.

## 42. Voice

Future optional capability only. Do not assume Fire TV voice-remotes expose unrestricted microphone audio to third-party applications. Voice must be capability detected and may come from an Android microphone, Bluetooth mic, supported remote API, or companion device.

## 43. Accessibility, localization, theming

Accessibility:

- large readable text
- strong focus indicators
- no color-only meaning
- screen-reader semantics where applicable
- optional reduced motion
- scalable UI

Localization:

- all UI strings through Android resources
- English first
- protocol uses stable machine identifiers, never localized values

Theming:

- dark
- light
- system
- high-contrast later
- configurable accent later

## 44. Generic external integrations

Pharos can serve as the display/control front end for external network scanners, home automation, routers/firewalls, sensor systems, Prometheus-compatible services, dashboards, camera systems, or custom REST/MQTT/WebSocket applications.

External monitoring systems remain responsible for scanning, packet capture, AP analysis, anomaly detection, long-term history, router/firewall control, and heavy AI analysis.

Pharos is responsible for generic presentation and safe user actions.

Example metric:

```json
{
  "type": "metric",
  "id": "network.internet.latency",
  "label": "Internet latency",
  "value": 18,
  "unit": "ms"
}
```

No dependency on a specific monitoring repository is allowed.

## 45. Digital signage and shared-screen modes

Signage features later:

- playlists
- scheduling
- cached/offline content
- images/video/text/web
- priority alert interruption

Shared-screen/game use later:

```text
Phones/controllers -> game/service backend -> Pharos
```

Pharos can render a board, timer, score, map, puzzle, mission display, or shared media while game logic remains external.

## 46. Performance and resource targets

Reference constraints: 2 GB RAM, limited free storage.

Goals:

- fast cold launch, target under ~3 seconds where practical
- near-zero idle CPU for static screens
- smooth remote navigation
- stable multi-day operation
- no unbounded allocation during screen rotation
- no network decode on the main thread
- lazy-load large content
- bounded image/media/event caches
- lifecycle-aware Flow collection
- release Media3 resources promptly

Measure on physical hardware; do not assume emulator performance represents Fire TV.

## 47. Android/Fire OS compatibility

Explicitly test:

- no Google-service dependency failures
- remote key codes
- Compose focus behavior
- launcher/foreground behavior
- WebView behavior
- Media3 codecs
- TLS/networking
- wake behavior
- APK sideload/install
- package/API differences
- background restrictions

Emulators:

1. Android TV 1080p API 30
2. current Android TV
3. phone
4. tablet

Physical AFTKM testing remains mandatory for releases.

## 48. Testing strategy

### Unit tests

- protocol parsing/validation
- priority ordering
- deduplication
- provider state machines
- retry/backoff
- screen selection
- command authorization
- configuration migration

### Integration tests

- HTTP provider
- WebSocket reconnect
- MQTT connect/reconnect
- persistence
- media lifecycle
- caches

### UI tests

- D-pad focus/navigation
- dashboard rendering
- alert interruption and restoration
- settings
- connection/error states
- common resolutions

### Resilience tests

Simulate:

- Wi-Fi loss
- DNS failure
- server restart
- MQTT broker restart
- malformed JSON
- duplicate messages
- oversized payloads
- alert floods
- invalid/expired credentials
- HTTP 401/403/429/500
- slow endpoints
- media timeout
- process death
- low storage

### Security tests

- unauthorized connection
- token redaction
- replay attempts where applicable
- WebView navigation escape
- dangerous URI schemes
- command allowlist enforcement
- config export without secrets

## 49. CI/CD

Use GitHub Actions.

Initial workflow:

1. checkout
2. configure supported JDK
3. Gradle validation
4. Android lint
5. unit tests
6. assemble debug APK
7. upload build artifact

Later:

- instrumentation tests
- dependency review
- CodeQL
- release builds
- SBOM
- signed artifacts
- checksums
- automated release notes

Never commit signing credentials.

## 50. Code quality

Use Kotlin formatting/linting appropriate to the project, Android lint, a version catalog, and strict warnings where practical. Add detekt only if it provides useful signal. Avoid adding tooling solely for ceremony.

## 51. Git/version/release strategy

Default branch: `main`.

Use feature branches and PRs for normal development after bootstrap.

Suggested branches:

```text
feature/bootstrap-android
feature/protocol-v1
feature/websocket-provider
feature/dashboard-runtime
fix/fire-tv-focus
```

App version uses semantic versioning. Protocol version is independent.

Example:

```text
App:      0.4.0
Protocol: 1.0
```

Initial distribution:

1. GitHub Releases APK
2. ADB/local sideload
3. evaluate Amazon Appstore
4. evaluate Google Play
5. evaluate F-Droid

For release APKs publish checksums and changelog. Recommended license: Apache-2.0; MIT remains an acceptable simpler alternative. Decide before substantial outside contribution.

## 52. Documentation plan

After Android bootstrap, create:

- `README.md`
- `docs/ARCHITECTURE.md`
- `docs/PROTOCOL.md`
- `docs/PROVIDERS.md`
- `docs/RENDERERS.md`
- `docs/FIRE_TV.md`
- `docs/SECURITY.md`
- `docs/DEVELOPMENT.md`
- `docs/TESTING.md`
- `docs/RELEASE.md`

`PLAN.md` remains the master roadmap until architecture stabilizes.

Public examples should cover curl, Python, JavaScript/Node, MQTT, and WebSocket with fake credentials and safe example addresses.

## 53. Privacy

No cloud telemetry by default. If analytics/crash reporting is ever added, it must be documented, privacy-conscious, and ideally opt-in. Never transmit screen content, secrets, or private payloads merely for analytics.

## 54. Configuration import/export

Future export can contain providers, dashboards, mappings, themes, and preferences, but not secrets by default.

Import must validate schema/version, show meaningful changes, reject incompatible content, and require confirmation before destructive overwrite.

Node identity restore must be explicit because duplicated node IDs are dangerous.

## 55. Fleet management compatibility

Pharos itself does not need a fleet server, but its protocol should permit an independent controller to:

- list/register nodes
- observe online/offline state
- assign groups
- push configuration
- request status
- update playlists
- send alerts
- query capabilities

## 56. Time/scheduling

Use ISO-8601 timestamps with explicit timezone offsets; UTC recommended in the protocol.

Future schedule model may include active dates, local/absolute start and end, recurrence, timezone, and priority.

Use monotonic timers for runtime TTLs where wall-clock changes could cause errors.

## 57. Error UX

Do not expose raw transport errors as the primary UI.

Bad:

```text
Error 1006
```

Better:

```text
Controller disconnected
Retrying automatically
Last connected: 18:42
```

Technical details belong in diagnostics.

## 58. MVP definition

MVP is complete when:

1. app installs on reference AFTKM;
2. launches reliably;
3. TV remote navigation works;
4. demo mode works offline;
5. persistent random node identity exists;
6. configurable HTTP source works;
7. configurable WebSocket source works;
8. public JSON protocol v1 exists;
9. text/metric/status/image/list/clock widgets render;
10. a dashboard layout renders correctly;
11. alerts interrupt dashboards;
12. alert TTL restores prior screen;
13. network failures do not crash or blank the app;
14. settings can configure endpoints;
15. secrets never appear in logs;
16. diagnostics exist;
17. core protocol/priority tests exist;
18. CI builds debug APK;
19. Fire TV sideloading is documented;
20. repository contains no private references.

MQTT may be MVP or immediately post-MVP depending on Android client validation.

## 59. Implementation phases

### Phase 0 — Repository bootstrap

- create public repository
- add this `PLAN.md`
- choose license
- later add minimal README, security/contribution docs, `.gitignore`

For the requested initial state, the repository may contain only `PLAN.md`.

### Phase 1 — Android bootstrap

1. create Android Studio project;
2. Kotlin + Compose;
3. package ID;
4. SDK/version catalog;
5. debug/release build types;
6. emulator launch;
7. AFTKM install via ADB;
8. verify D-pad events;
9. verify full-screen behavior;
10. add diagnostics page;
11. add app theme;
12. add CI.

Exit: app launches on Fire TV and Android emulator; remote navigates basic UI; CI passes.

### Phase 2 — Core runtime

Implement node identity, app state, screen model, widget model, event model, command model, renderer registry, screen router, persistence, and demo screens.

Exit: declarative local dashboards render.

### Phase 3 — Protocol v1

Implement JSON models, serialization, schemas, envelope, target rules, compatibility policy, validation, examples, and contract tests.

Exit: a third-party program can produce valid messages using public docs only.

### Phase 4 — HTTP provider

Implement configuration, polling, auth, timeout, retry, cache, error state, settings UI, tests, and demo endpoint.

Exit: Pharos renders a dashboard from external HTTP JSON.

### Phase 5 — WebSocket provider

Implement authenticated connection, inbound messages, reconnect, heartbeat, bounded queue, and provider health.

Exit: external push updates dashboard immediately.

### Phase 6 — Alert/priority runtime

Implement interruption, TTL, restore, deduplication, acknowledgement, and storm protection.

Exit: urgent messages reliably interrupt and restore normal display state.

### Phase 7 — MQTT

1. select maintained Android-compatible client;
2. validate on API 30 Fire OS;
3. implement provider abstraction;
4. TLS/credentials;
5. subscriptions;
6. node/group topics;
7. retained-message semantics;
8. last-will/status;
9. reconnect;
10. tests.

### Phase 8 — Media

Image/media renderer, Media3 video/audio, HLS, cache policy, error recovery, and physical-device testing.

### Phase 9 — Camera streams

Start with snapshots/HLS, then evaluate RTSP. Add single view, grid, and event-triggered pop-up.

### Phase 10 — Web renderer

Implement restricted WebView with security review and remote navigation testing.

### Phase 11 — Managed pairing

Implement one-time pairing QR/code, credential establishment, node registration, and reconnection semantics. Keep controller implementation independent.

### Phase 12 — AI interface

Add optional provider abstraction, text prompt/response UI, selected context, LAN model endpoint, and optional OpenAI-compatible HTTP support.

### Phase 13 — Kiosk reliability

Run 24-hour, 72-hour, and one-week tests. Track RAM, CPU, Wi-Fi reconnects, media resources, process restart, screen state, and content recovery.

### Phase 14 — Public integration helpers

Only after protocol stability, consider Python, TypeScript, or Kotlin helper libraries. JSON Schema plus examples may be sufficient initially.

### Phase 15 — Distribution

Signed APK, GitHub Release, checksums, release notes, and store evaluation.

## 60. First implementation sprint — exact order

1. bootstrap Android project
2. Compose activity
3. TV/phone capability detection
4. persistent node UUID
5. diagnostics screen
6. logical input abstraction
7. focus-aware navigation
8. `ScreenDefinition`
9. base `Widget` sealed model
10. text widget
11. metric widget
12. status widget
13. image widget
14. clock widget
15. grid renderer
16. offline demo dashboard
17. persist selected screen
18. protocol envelope
19. JSON serialization
20. JSON Schema
21. sample JSON
22. load local JSON screen
23. HTTP provider
24. endpoint settings
25. render remote JSON
26. WebSocket provider
27. live metric updates
28. alert model
29. priority engine
30. interrupt screen
31. restore screen
32. diagnostics expansion
33. tests
34. run on physical Fire TV
35. profile memory
36. fix focus/navigation issues
37. document sideloading
38. publish `0.1.0` pre-release

## 61. Initial Kotlin domain sketch

```kotlin
data class PharosEnvelope(
    val protocolVersion: String,
    val id: String,
    val timestamp: Instant,
    val type: MessageType,
    val source: String?,
    val target: Target?,
    val payload: JsonElement
)
```

```kotlin
sealed interface Widget {
    val id: String
}
```

```kotlin
data class MetricWidget(
    override val id: String,
    val label: String,
    val value: String,
    val unit: String? = null
) : Widget
```

```kotlin
interface Provider {
    val id: String
    val state: StateFlow<ProviderState>
    val messages: Flow<PharosEnvelope>
    suspend fun start()
    suspend fun stop()
}
```

These are starting concepts, not frozen APIs.

## 62. Internal state flow

```text
Provider
   |
Message Validator
   |
Message Router
   +--> Configuration Store
   +--> Alert Engine
   +--> Screen Store
   +--> Command Dispatcher
   |
Application State
   |
Compose UI
```

Rules:

- no networking on main thread
- no `GlobalScope`
- provider retries are cancellable
- cancellation propagates
- lifecycle-aware Flow collection
- renderers stay fast

## 63. Error domain

Typed errors should include concepts such as:

```text
AUTHENTICATION_FAILED
CONNECTION_FAILED
TIMEOUT
INVALID_MESSAGE
UNSUPPORTED_PROTOCOL
UNSUPPORTED_WIDGET
MEDIA_FAILED
STORAGE_LOW
PERMISSION_DENIED
RATE_LIMITED
```

Do not surface raw exceptions directly to normal UI.

## 64. Protocol evolution

- `1.x` changes remain backward-compatible
- unknown optional fields follow documented ignore behavior
- unsupported required behavior is rejected explicitly
- breaking changes become `2.0`
- retain schemas for supported versions
- document migrations
- never silently reinterpret an existing field incompatibly

## 65. Extension points

Planned source-level extension interfaces:

- Provider
- Renderer
- CommandHandler
- AiProvider
- MediaSourceAdapter
- AuthenticationProvider

Do not implement dynamic third-party APK plugin loading initially because of security and compatibility cost.

## 66. Fire TV acceptance checklist

For meaningful releases:

- [ ] installs via ADB
- [ ] launches
- [ ] no Google Play dependency failure
- [ ] D-pad works
- [ ] focus is always visible
- [ ] Back works
- [ ] 1080p layout works
- [ ] 4K scaling is acceptable
- [ ] Wi-Fi reconnect works
- [ ] WebSocket reconnect works
- [ ] MQTT works when enabled
- [ ] media works when enabled
- [ ] extended idle does not break app
- [ ] no obvious memory growth
- [ ] secrets are absent from logs
- [ ] alert interruption works
- [ ] previous screen restores
- [ ] settings are usable with remote

## 67. Public repository safety checklist

Before every push, scan for:

- private repository names
- internal hostnames
- sensitive IP/infrastructure details
- tokens/passwords/API keys
- certificates/private keys
- personal information
- proprietary content

Add automated secret scanning/pre-commit protection once the Android project is bootstrapped.

## 68. Issue backlog structure

After bootstrap, convert this plan into GitHub issues/epics for:

1. Android bootstrap
2. Fire TV compatibility
3. core runtime
4. protocol v1
5. dashboard system
6. remote/focus navigation
7. HTTP provider
8. WebSocket provider
9. MQTT provider
10. alert priority engine
11. security
12. media
13. camera streams
14. web renderer
15. diagnostics
16. pairing
17. AI interface
18. kiosk reliability
19. documentation
20. release/distribution

Every implementation issue should specify problem, scope, non-scope, design notes, acceptance criteria, tests, and documentation requirements.

## 69. Definition of done

A feature is done only when:

- implementation is complete
- appropriate unit/integration tests exist
- TV remote behavior is tested for UI features
- Fire TV is tested when platform-sensitive
- failure states are handled
- logs are sanitized
- documentation is updated
- accessibility is considered
- repository contains no private references
- CI is green

## 70. Success criteria for 1.0

Pharos 1.0 is successful when:

- a new public user can build it from documentation
- APK is reliable on AFTKM
- it also works on standard Android TV
- D-pad interaction is polished
- demo mode works without infrastructure
- HTTP/WebSocket/MQTT integrations are documented
- protocol v1 is stable
- dashboards are configurable
- priority alerts interrupt and restore correctly
- media is stable
- credentials are protected
- network outages are handled gracefully
- diagnostics are useful
- release process is reproducible
- no private dependencies exist
- third-party software can integrate using public docs/schema only

## 71. Immediate next actions after this planning-only commit

1. choose Apache-2.0 vs MIT license
2. create Android bootstrap feature branch
3. generate Android Studio project
4. configure Kotlin/Compose
5. finalize application ID
6. create README
7. create architecture document
8. create protocol document
9. add CI
10. connect physical Fire TV via ADB
11. capture reference-device diagnostics
12. build/install hello-screen APK
13. validate D-pad focus
14. begin node/runtime model
15. create GitHub issues from implementation phases

## 72. Guiding principles

1. **General purpose, not Fire-TV-specific.**
2. **Fire TV is the first reference implementation target.**
3. **External systems integrate with Pharos, not the reverse.**
4. **No private repository dependencies or references.**
5. **Local-first operation remains possible.**
6. **Network failure must not make the display useless.**
7. **Remote/D-pad interaction is first-class.**
8. **Security is designed before remote control surfaces are exposed.**
9. **Protocol compatibility matters more than transport cleverness.**
10. **Keep the Android node lightweight.**
11. **Heavy analysis belongs externally unless a lightweight local model clearly fits.**
12. **Validate every untrusted network payload.**
13. **Never allow generic remote code execution.**
14. **Demo mode must explain the product without infrastructure.**
15. **New integrations should normally be possible without changing Pharos core.**

## 73. Final architectural statement

Pharos should eventually be able to sit behind almost any HDMI screen, or run on almost any suitable Android device, and behave as a programmable, secure, resilient display node.

A user should be able to install Pharos, choose/configure a provider, and turn spare hardware into an operations dashboard, network status display, alert endpoint, smart-home panel, camera viewer, digital sign, AI terminal, shared game screen, or a future use case not yet anticipated.

That flexibility is the core product. The implementation must therefore optimize for stable public interfaces, modular renderers, generic transports, remote-friendly UX, reliable unattended operation, strong security boundaries, and strict separation from any specific external application.