# First launch

> **Status: planned.** This describes the designed flow; the screens are being
> built.

Pharos generates its identity on first launch and then asks one question that
matters: where does content come from?

## The flow

1. **Welcome** — what this app is, in two sentences.
2. **Choose a mode** — Demo, Standalone, or Connect to a controller.
3. **Configure the source** — an endpoint, or nothing at all for Demo.
4. **Test the connection** — before you leave the screen, not after.
5. **Name the device** — "Living room TV", "Workshop panel".
6. **Choose the default display** — what shows when nothing else is happening.
7. **Finish.**

Every screen is navigable with a remote. If a step needs typing, it accepts an
on-screen keyboard and a paired Bluetooth keyboard both.

## The three modes

**Demo** needs no network and no server. It shows a rotating dashboard with a
clock, sample metrics, local device status and an example alert that interrupts
and then restores the previous screen. It exists so you can see what Pharos does
before deciding whether to wire anything up — and so a broken integration can
always be compared against something known-good.

**Standalone** points Pharos at sources you configure: a REST endpoint it polls,
an MQTT topic it subscribes to, a media URL, a web page. No controller, no
Pharos server, no account.

**Connect to a controller** hands the decision to an external system that pushes
content, screens, alerts and configuration. Pairing is described in
[../developer/protocol.md](../developer/protocol.md); the short version is that
the controller shows a QR code or a short code, the node generates a keypair,
and only the public half ever leaves the device.

You can change modes later. Hybrid — local dashboards plus controller
interruptions — is the combination most installations end up wanting.

## Node identity

On first launch Pharos generates a random UUID and persists it. It is not
derived from the MAC address, serial number, or any account — those identify
hardware and people, and a display node needs to identify neither.

You can give the node a display name, a location label, groups and tags. Those
are for humans and for addressing; the UUID is what the protocol uses.

Reinstalling generates a **new** identity. Restoring one from a backup is
deliberately an explicit action, because two nodes sharing an identity is a
confusing failure that looks like a network problem for a long time.

## Settings afterwards

Node · Display · Screens · Providers · Network · Media · Alerts · Security ·
Diagnostics · About.

All of it is remote-navigable. A setting you cannot reach without a mouse is a
setting that does not exist on a TV.
