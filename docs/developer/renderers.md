# Screens, layouts and widgets

Dashboards are declarative. A source describes what it wants shown; Pharos
decides how to draw it on this device, at this resolution, with this input
method.

## Screens

A screen has an id, a title, a layout, widgets, and optionally: a duration, a
refresh policy, a transition, a validity interval, a priority and a cache
policy.

Screens may be persistent, temporary, scheduled, provider-owned or locally
configured. Playlists rotate them:

```json
{
  "playlist": [
    {"screenId": "overview", "durationSeconds": 30},
    {"screenId": "weather",  "durationSeconds": 20},
    {"screenId": "network",  "durationSeconds": 30}
  ]
}
```

Rotation pauses on user interaction and resumes after a configurable timeout —
otherwise the screen changes under someone's hand while they are reading it.

## Layouts

`grid` · `rows` · `columns` · `hero + grid` · `list` · `full-screen` · `split`

## Widget catalog

MVP: **text**, **metric**, **status**, **image**, **clock**, **list**.

Planned: progress, gauge, countdown, QR code, chart, media, web, camera, alert
feed.

```json
{
  "type": "metric",
  "id": "network.internet.latency",
  "label": "Internet latency",
  "value": 18,
  "unit": "ms"
}
```

Note what that example does *not* contain: a colour, a font size, a position, or
a threshold for "bad". The source says what the number is; the node decides how
it looks on a 4K television across a room.

## Unknown widgets never break a screen

A widget type this version does not know renders a placeholder and logs a
sanitized warning. Everything else on the screen renders normally.

This is the single most important rule in this file. Sources will run ahead of
nodes — that is the normal state of a fleet, not a fault — and a display that
goes blank because one tile was too new is a display nobody trusts.

## The renderer registry

```kotlin
interface WidgetRenderer<T : Widget> {
    val type: String

    @Composable
    fun Render(widget: T, context: RenderContext)
}
```

Registration is source-level: add a renderer, register its type. Dynamic APK
plugin loading is deliberately not supported — see
[architecture.md](architecture.md#extension-points).

Renderers stay fast. A renderer does layout and drawing; it does not fetch,
parse, decode or compute. Anything else belongs upstream in the runtime, because
a slow renderer is a stutter on every frame of a screen that may be up for days.

## Focus and input

Every renderer that can be interacted with participates in focus. The
requirements are release-quality, not polish:

- an obviously focused state — visible across a room, not a subtle tint
- deterministic directional movement
- a correct default focus when a screen appears
- focus containment inside modals
- focus restoration when returning to a screen
- **no touch-only controls**, anywhere

Focus indication never relies on colour alone; it changes shape, border or
scale as well. Colour-only meaning fails for a colour-blind viewer and fails
again on a badly calibrated television.

Input arrives as logical actions — `Up`, `Down`, `Left`, `Right`, `Select`,
`Back`, `Menu`, `PlayPause`, and the rest — never as raw key codes from one
particular remote. A Fire remote, a game controller, a Bluetooth keyboard and a
touchscreen all produce the same actions, which is why supporting the next
device is a mapping change rather than a UI change.

## Accessibility and theming

Large readable text, strong focus indicators, no colour-only meaning,
screen-reader semantics where applicable, optional reduced motion, and a UI that
scales.

All UI strings go through Android resources — English first. Protocol values are
stable machine identifiers and are never localized; a `status` of `"degraded"`
means the same thing in every locale, and translating it would break every
consumer that switches on it.

Themes: dark (default), light, system. High contrast and a configurable accent
come later.
