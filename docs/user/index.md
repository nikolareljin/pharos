# Using Pharos

Pharos runs on the device that has the screen. Everything here is about that
device.

- [install.md](install.md) — getting the app onto a phone, tablet, TV box or
  streaming stick
- [fire-tv.md](fire-tv.md) — the Fire TV reference device: developer mode, ADB
  over the network, and the platform's limits
- [onboarding.md](onboarding.md) — first launch and the three operating modes
- [troubleshooting.md](troubleshooting.md) — the screen is blank, the remote
  does nothing, the data is stale

## The three modes, in one paragraph

In **standalone** mode Pharos fetches from sources you configure and needs no
server of its own. In **managed** mode an external controller decides what the
node displays. **Hybrid** is both: your normal dashboards run locally, and a
controller can interrupt them with something urgent. Hybrid is the intended
long-term shape, and nothing about standalone requires a Pharos server to exist
anywhere.

## What it costs the device

Pharos is designed against a 2 GB streaming stick, so the ceilings are real
rather than aspirational: bounded caches, bounded queues, bounded logs, no
unbounded history, no persistent raw video cache, no continuous local inference.
A static screen should cost close to nothing while it sits there.
