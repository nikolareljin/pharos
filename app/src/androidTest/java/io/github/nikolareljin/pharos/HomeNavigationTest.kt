package io.github.nikolareljin.pharos

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyPress
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.NativeKeyEvent
import androidx.compose.ui.input.key.nativeKeyCode
import io.github.nikolareljin.pharos.feature.home.HomeScreen
import io.github.nikolareljin.pharos.feature.home.TAG_HOME_DIAGNOSTICS
import io.github.nikolareljin.pharos.feature.home.TAG_HOME_STATUS
import io.github.nikolareljin.pharos.feature.home.TAG_HOME_TITLE
import io.github.nikolareljin.pharos.ui.theme.PharosTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * The remote-navigation contract for the home screen. These run on a device or
 * emulator: `./gradlew connectedDebugAndroidTest`.
 *
 * They exist because focus bugs are invisible to unit tests and obvious to
 * anyone holding a remote — which means they are found by users rather than by
 * CI unless something like this is here.
 */
class HomeNavigationTest {

    @get:Rule
    val compose = createComposeRule()

    private fun setHome(onOpenDiagnostics: () -> Unit = {}) {
        compose.setContent {
            PharosTheme {
                HomeScreen(
                    nodeId = "f72a6ef4-2a42-42a9-a39c-9e2dc4f87833",
                    appVersion = "0.1.0-test",
                    onOpenDiagnostics = onOpenDiagnostics,
                )
            }
        }
    }

    @Test
    fun renders() {
        setHome()
        compose.onNodeWithTag(TAG_HOME_TITLE).assertIsDisplayed()
        compose.onNodeWithTag(TAG_HOME_STATUS).assertIsDisplayed()
        compose.onNodeWithTag(TAG_HOME_DIAGNOSTICS).assertIsDisplayed()
    }

    @Test
    fun opensWithSomethingFocused() {
        // A screen that opens with nothing focused swallows the first key press
        // and reads as a freeze.
        setHome()
        compose.onNodeWithTag(TAG_HOME_STATUS).assertIsFocused()
    }

    @Test
    fun everyCardIsReachableWithTheDpad() {
        setHome()
        compose.onNodeWithTag(TAG_HOME_STATUS).assertIsFocused()

        compose.onNodeWithTag(TAG_HOME_STATUS).performKeyPress(keyDown(Key.DirectionRight))
        compose.waitForIdle()

        compose.onNodeWithTag(TAG_HOME_DIAGNOSTICS).assertIsFocused()
    }

    @Test
    fun selectOnTheDiagnosticsCardOpensDiagnostics() {
        var opened = false
        setHome(onOpenDiagnostics = { opened = true })

        compose.onNodeWithTag(TAG_HOME_STATUS).performKeyPress(keyDown(Key.DirectionRight))
        compose.waitForIdle()

        // Down then up: Compose's clickable arms on the key-down and fires on
        // the matching key-up, so a lone key-up does nothing and the test would
        // pass or fail for the wrong reason.
        compose.onNodeWithTag(TAG_HOME_DIAGNOSTICS).performKeyPress(keyDown(Key.DirectionCenter))
        compose.onNodeWithTag(TAG_HOME_DIAGNOSTICS).performKeyPress(keyUp(Key.DirectionCenter))
        compose.waitForIdle()

        // assertTrue, not Kotlin's assert(): JVM assertions are disabled unless
        // -ea is passed, and instrumented tests do not pass it — assert() would
        // silently pass on a broken build.
        assertTrue("Select on the diagnostics card must open diagnostics", opened)
    }

    @Test
    fun tappingTheDiagnosticsCardOpensDiagnostics() {
        // The APK installs on phones and tablets, so every control has to answer
        // a finger as well as a remote.
        var opened = false
        setHome(onOpenDiagnostics = { opened = true })

        compose.onNodeWithTag(TAG_HOME_DIAGNOSTICS).performClick()
        compose.waitForIdle()

        assertTrue("Tapping the diagnostics card must open diagnostics", opened)
    }

    private fun keyDown(key: Key) = KeyEvent(
        NativeKeyEvent(NativeKeyEvent.ACTION_DOWN, key.nativeKeyCode),
    )

    private fun keyUp(key: Key) = KeyEvent(
        NativeKeyEvent(NativeKeyEvent.ACTION_UP, key.nativeKeyCode),
    )
}
