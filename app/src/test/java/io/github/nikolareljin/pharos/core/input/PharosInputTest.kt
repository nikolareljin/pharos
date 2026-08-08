package io.github.nikolareljin.pharos.core.input

import android.view.KeyEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PharosInputTest {

    @Test
    fun `maps the d-pad`() {
        assertEquals(PharosAction.Up, PharosInput.actionFor(KeyEvent.KEYCODE_DPAD_UP))
        assertEquals(PharosAction.Down, PharosInput.actionFor(KeyEvent.KEYCODE_DPAD_DOWN))
        assertEquals(PharosAction.Left, PharosInput.actionFor(KeyEvent.KEYCODE_DPAD_LEFT))
        assertEquals(PharosAction.Right, PharosInput.actionFor(KeyEvent.KEYCODE_DPAD_RIGHT))
    }

    @Test
    fun `every device's select key means Select`() {
        // A TV remote, a keyboard and a gamepad each send a different code for
        // the same intent. Handling only one is how a build works on the
        // developer's remote and nowhere else.
        listOf(
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_NUMPAD_ENTER,
            KeyEvent.KEYCODE_BUTTON_A,
        ).forEach { code ->
            assertEquals("keycode $code", PharosAction.Select, PharosInput.actionFor(code))
        }
    }

    @Test
    fun `every device's back key means Back`() {
        listOf(
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_ESCAPE,
            KeyEvent.KEYCODE_BUTTON_B,
        ).forEach { code ->
            assertEquals("keycode $code", PharosAction.Back, PharosInput.actionFor(code))
        }
    }

    @Test
    fun `maps media transport keys`() {
        assertEquals(PharosAction.PlayPause, PharosInput.actionFor(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
        assertEquals(PharosAction.Play, PharosInput.actionFor(KeyEvent.KEYCODE_MEDIA_PLAY))
        assertEquals(PharosAction.Pause, PharosInput.actionFor(KeyEvent.KEYCODE_MEDIA_PAUSE))
        assertEquals(PharosAction.Stop, PharosInput.actionFor(KeyEvent.KEYCODE_MEDIA_STOP))
        assertEquals(PharosAction.Rewind, PharosInput.actionFor(KeyEvent.KEYCODE_MEDIA_REWIND))
        assertEquals(
            PharosAction.FastForward,
            PharosInput.actionFor(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD),
        )
    }

    @Test
    fun `unhandled keys map to nothing rather than to something wrong`() {
        assertNull(PharosInput.actionFor(KeyEvent.KEYCODE_A))
        assertNull(PharosInput.actionFor(KeyEvent.KEYCODE_VOLUME_UP))
        assertNull(PharosInput.actionFor(KeyEvent.KEYCODE_UNKNOWN))
    }

    @Test
    fun `only directional actions are directional`() {
        listOf(PharosAction.Up, PharosAction.Down, PharosAction.Left, PharosAction.Right)
            .forEach { assertTrue(it.name, PharosInput.isDirectional(it)) }

        listOf(PharosAction.Select, PharosAction.Back, PharosAction.Menu, PharosAction.PlayPause)
            .forEach { assertFalse(it.name, PharosInput.isDirectional(it)) }
    }
}
