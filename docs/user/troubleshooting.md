# Troubleshooting

Start at the **Diagnostics** screen. It shows the app and protocol versions, the
node ID, device model and API level, memory and storage, display size, network
reachability, every provider's state, the last successful connection, the last
message received, queue depths and recent sanitized errors. Most questions here
are answered by reading it.

## The app installed but is not on the TV home screen

TV launchers list applications by their banner, not their icon. If the banner is
missing the entry can be absent while the package is installed perfectly.
Confirm the package is there and start it directly:

```sh
adb shell pm list packages --user 0 | grep pharos
adb shell am start -n io.github.nikolareljin.pharos/.MainActivity
```

If `pm list packages` is empty but `adb install` said `Success`, it installed
into a different user profile. Reinstall with `--user 0`.

## The screen is showing old data

That is deliberate. When a source goes away Pharos keeps rendering the last
valid content and shows a quiet status hint rather than blanking the display or
throwing up an error box. A dashboard that goes white on a Wi-Fi blip is worse
than one showing a stale number with a warning on it.

Check the provider state in Diagnostics:

| State | Meaning |
|---|---|
| `DISABLED` | Not configured, or switched off |
| `CONNECTING` | First attempt in progress |
| `CONNECTED` | Healthy |
| `DEGRADED` | Connected, but something is wrong — slow responses, partial failures |
| `RECONNECTING` | Backing off and retrying: 1s, 2s, 4s, 8s, 15s, 30s, 60s, then capped |
| `FAILED` | Retries exhausted, or the failure is not retryable — bad credentials, for instance |

Use **Reconnect providers** in Diagnostics to reset the backoff without
restarting the app.

If you have ADB access to the device, [Debugging on a Fire
TV](../developer/fire-tv-debugging.md) covers reading logs and reproducing
navigation problems from a keyboard.

## The remote does nothing, or focus disappears

Focus loss is a bug, not a configuration problem. Please
[file an issue](https://github.com/nikolareljin/pharos/issues/new/choose) with
the screen you were on and what you pressed. Include the output of:

```sh
adb shell input keyevent 20    # does anything move?
adb logcat -d | grep -i pharos
```

## A widget shows "unsupported"

The source sent a widget type this version does not know. The screen keeps
rendering everything else — an unknown widget never takes the screen down. Check
the sending side's protocol version against the one in Diagnostics.

## Nothing arrives from my controller

In order:

1. Is the provider `CONNECTED`?
2. Does the last-message timestamp move when you publish?
3. Is the message addressed to this node? Check the node ID in Diagnostics
   against the one you are targeting, and remember that group and broadcast
   addressing are separate paths.
4. Does the message validate? Messages failing schema validation are rejected
   before routing and counted in Diagnostics — a rising rejection count with a
   healthy connection means the payload is wrong, not the network.

## Alerts pile up or will not clear

Pharos deduplicates by key, rate-limits per source, collapses repeats and caps
the visible queue, precisely so a broken integration cannot make the display
unusable. If alerts are stuck, look for a source republishing the same alert
with a changing dedup key — from Pharos's point of view each one is genuinely
new.

## Reporting something

Diagnostics can export a sanitized report — no tokens, no auth headers, no
credential-bearing URLs. Attach that. If you ever find a secret in it, report
that privately as a
[security advisory](https://github.com/nikolareljin/pharos/security/advisories/new),
because a leak in an export people are asked to paste into public issues is a
vulnerability.
