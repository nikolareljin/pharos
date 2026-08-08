package io.github.nikolareljin.pharos.core.device

import android.content.pm.PackageManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceCapabilitiesTest {

    private fun lookupOf(vararg present: String) = FeatureLookup { present.contains(it) }

    @Test
    fun `a television has a d-pad and no touchscreen`() {
        val caps = DeviceCapabilities.detect(
            lookupOf(
                PackageManager.FEATURE_LEANBACK,
                PackageManager.FEATURE_AUDIO_OUTPUT,
                PackageManager.FEATURE_WEBVIEW,
            ),
        )

        assertTrue(caps.dpad)
        assertFalse(caps.touch)
        assertFalse(caps.camera)
        assertFalse(caps.microphone)
        assertTrue(caps.audio)
        assertTrue(caps.web)
    }

    @Test
    fun `a phone has a touchscreen and a camera`() {
        val caps = DeviceCapabilities.detect(
            lookupOf(
                PackageManager.FEATURE_TOUCHSCREEN,
                PackageManager.FEATURE_CAMERA_ANY,
                PackageManager.FEATURE_MICROPHONE,
                PackageManager.FEATURE_AUDIO_OUTPUT,
                PackageManager.FEATURE_WEBVIEW,
            ),
        )

        assertTrue(caps.touch)
        assertTrue(caps.camera)
        assertTrue(caps.microphone)
        assertFalse("a plain phone should not claim a d-pad", caps.dpad)
    }

    @Test
    fun `leanback wins over a reported touchscreen`() {
        // Some TV boxes report a touchscreen they do not have. Leanback is the
        // signal that matters; deciding "TV" by the absence of touch would also
        // classify a phone with a broken digitiser as a television.
        val caps = DeviceCapabilities.detect(
            lookupOf(PackageManager.FEATURE_LEANBACK, PackageManager.FEATURE_TOUCHSCREEN),
        )

        assertTrue(caps.dpad)
        assertTrue(caps.touch)
    }

    @Test
    fun `a device with no touchscreen is assumed to be remote-driven`() {
        val caps = DeviceCapabilities.detect(lookupOf())
        assertTrue("no touch means the only way in is keys", caps.dpad)
    }

    @Test
    fun `transports are configuration, not hardware`() {
        val bare = DeviceCapabilities.detect(lookupOf())
        assertFalse("mqtt is off until it is implemented and configured", bare.mqtt)
        assertTrue(bare.websocket)

        val withMqtt = DeviceCapabilities.detect(lookupOf(), mqttEnabled = true)
        assertTrue(withMqtt.mqtt)
    }
}
