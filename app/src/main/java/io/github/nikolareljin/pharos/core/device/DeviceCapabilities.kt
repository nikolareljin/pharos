package io.github.nikolareljin.pharos.core.device

import android.content.Context
import android.content.pm.PackageManager

/**
 * Answers "does this device have <feature>?". An interface rather than a direct
 * PackageManager call so capability mapping can be tested without a device.
 */
fun interface FeatureLookup {
    fun has(feature: String): Boolean
}

/**
 * What this node can actually do.
 *
 * Published to controllers so they can address a fleet without assuming every
 * Android device is the same one: a stick has a D-pad and no touchscreen, a
 * tablet is the reverse, and a controller that guesses gets it wrong on half
 * the fleet.
 *
 * The fields mirror the `capabilities` message in the protocol. Transport
 * capabilities are not device features — they depend on what is compiled in and
 * configured — so they are passed rather than probed.
 */
data class DeviceCapabilities(
    val display: Boolean = true,
    val touch: Boolean,
    val dpad: Boolean,
    val audio: Boolean,
    val video: Boolean,
    val web: Boolean,
    val camera: Boolean,
    val microphone: Boolean,
    val mqtt: Boolean,
    val websocket: Boolean,
) {
    companion object {
        /**
         * A television is identified by leanback rather than by the absence of a
         * touchscreen: a phone with a broken digitiser is not a TV, and some TV
         * boxes report a touchscreen they do not have.
         */
        fun detect(
            lookup: FeatureLookup,
            mqttEnabled: Boolean = false,
            webSocketEnabled: Boolean = true,
        ): DeviceCapabilities {
            val leanback = lookup.has(PackageManager.FEATURE_LEANBACK)
            val touch = lookup.has(PackageManager.FEATURE_TOUCHSCREEN)
            return DeviceCapabilities(
                touch = touch,
                // Every TV has a D-pad; a handheld may still have one attached,
                // but we cannot detect that until keys arrive, so assume not.
                dpad = leanback || !touch,
                audio = lookup.has(PackageManager.FEATURE_AUDIO_OUTPUT),
                video = true,
                web = lookup.has(PackageManager.FEATURE_WEBVIEW),
                camera = lookup.has(PackageManager.FEATURE_CAMERA_ANY),
                microphone = lookup.has(PackageManager.FEATURE_MICROPHONE),
                mqtt = mqttEnabled,
                websocket = webSocketEnabled,
            )
        }

        fun detect(context: Context): DeviceCapabilities =
            detect(FeatureLookup { context.packageManager.hasSystemFeature(it) })
    }
}
