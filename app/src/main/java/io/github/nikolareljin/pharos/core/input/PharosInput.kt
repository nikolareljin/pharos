package io.github.nikolareljin.pharos.core.input

import android.view.KeyEvent

/**
 * What the user asked for, independent of what they pressed it on.
 *
 * The UI reacts to these and never to key codes. A Fire remote, a game
 * controller, a Bluetooth keyboard and a phone's back gesture all arrive here as
 * the same action, so supporting the next device is a mapping change rather
 * than a change to every screen.
 */
enum class PharosAction {
    Up,
    Down,
    Left,
    Right,
    Select,
    Back,
    Menu,
    PlayPause,
    Play,
    Pause,
    Stop,
    Rewind,
    FastForward,
    ChannelUp,
    ChannelDown,
}

/** Maps a hardware key code to a logical action, or null if we do not handle it. */
object PharosInput {

    fun actionFor(keyCode: Int): PharosAction? = when (keyCode) {
        KeyEvent.KEYCODE_DPAD_UP -> PharosAction.Up
        KeyEvent.KEYCODE_DPAD_DOWN -> PharosAction.Down
        KeyEvent.KEYCODE_DPAD_LEFT -> PharosAction.Left
        KeyEvent.KEYCODE_DPAD_RIGHT -> PharosAction.Right

        KeyEvent.KEYCODE_DPAD_CENTER,
        KeyEvent.KEYCODE_ENTER,
        KeyEvent.KEYCODE_NUMPAD_ENTER,
        KeyEvent.KEYCODE_BUTTON_A,
        -> PharosAction.Select

        KeyEvent.KEYCODE_BACK,
        KeyEvent.KEYCODE_ESCAPE,
        KeyEvent.KEYCODE_BUTTON_B,
        -> PharosAction.Back

        KeyEvent.KEYCODE_MENU,
        KeyEvent.KEYCODE_BUTTON_START,
        -> PharosAction.Menu

        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
        KeyEvent.KEYCODE_SPACE,
        -> PharosAction.PlayPause

        KeyEvent.KEYCODE_MEDIA_PLAY -> PharosAction.Play
        KeyEvent.KEYCODE_MEDIA_PAUSE -> PharosAction.Pause
        KeyEvent.KEYCODE_MEDIA_STOP -> PharosAction.Stop
        KeyEvent.KEYCODE_MEDIA_REWIND -> PharosAction.Rewind
        KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> PharosAction.FastForward
        KeyEvent.KEYCODE_CHANNEL_UP -> PharosAction.ChannelUp
        KeyEvent.KEYCODE_CHANNEL_DOWN -> PharosAction.ChannelDown

        else -> null
    }

    /**
     * Whether an action moves focus. Directional actions are left to Compose's
     * focus system rather than handled by hand — hand-rolled focus movement is
     * how a screen ends up with a corner the remote cannot reach.
     */
    fun isDirectional(action: PharosAction): Boolean = when (action) {
        PharosAction.Up, PharosAction.Down, PharosAction.Left, PharosAction.Right -> true
        else -> false
    }
}
