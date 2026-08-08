package io.github.nikolareljin.pharos.feature.diagnostics

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.StatFs
import android.util.DisplayMetrics
import android.view.WindowManager
import io.github.nikolareljin.pharos.BuildConfig
import io.github.nikolareljin.pharos.core.device.DeviceCapabilities

/**
 * A snapshot of what this node is and what it is doing.
 *
 * Everything here is safe to show on a screen and safe to paste into a public
 * issue. Nothing that identifies the hardware uniquely — no serial, no MAC, no
 * advertising id — and nothing carrying a credential. A diagnostics export
 * people are asked to share has to be shareable without them reading it line by
 * line first.
 */
data class Diagnostics(
    val appVersion: String,
    val protocolVersion: String,
    val nodeId: String,
    val deviceModel: String,
    val androidRelease: String,
    val apiLevel: Int,
    val totalMemoryMb: Long,
    val availableMemoryMb: Long,
    val lowMemory: Boolean,
    val freeStorageMb: Long,
    val displayWidth: Int,
    val displayHeight: Int,
    val displayDensity: Float,
    val capabilities: DeviceCapabilities,
) {
    /** The screen-readable form, in the order a person reads it. */
    fun rows(): List<Pair<String, String>> = listOf(
        "App version" to appVersion,
        "Protocol version" to protocolVersion,
        "Node ID" to nodeId,
        "Device" to deviceModel,
        "Android" to "$androidRelease (API $apiLevel)",
        "Memory" to "$availableMemoryMb MB free of $totalMemoryMb MB${if (lowMemory) " — LOW" else ""}",
        "Storage" to "$freeStorageMb MB free",
        "Display" to "${displayWidth}x$displayHeight @ ${displayDensity}x",
        "Input" to buildString {
            if (capabilities.dpad) append("D-pad ")
            if (capabilities.touch) append("touch ")
            if (isEmpty()) append("none detected")
        }.trim(),
        "Media" to buildString {
            if (capabilities.audio) append("audio ")
            if (capabilities.video) append("video ")
            if (capabilities.camera) append("camera ")
            if (capabilities.microphone) append("mic ")
            if (isEmpty()) append("none")
        }.trim(),
        "Transports" to buildString {
            if (capabilities.websocket) append("websocket ")
            if (capabilities.mqtt) append("mqtt ")
            if (isEmpty()) append("none enabled")
        }.trim(),
    )

    companion object {
        @Suppress("DEPRECATION") // getMetrics covers API 26-29; see below.
        fun collect(context: Context, nodeId: String): Diagnostics {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo().also(activityManager::getMemoryInfo)

            val stat = StatFs(context.filesDir.absolutePath)
            val freeBytes = stat.availableBlocksLong * stat.blockSizeLong

            val windowManager =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            // WindowMetrics arrives in API 30; minSdk is 26, so the deprecated
            // path is the only one that covers every supported device.
            val metrics = DisplayMetrics().also { windowManager.defaultDisplay.getMetrics(it) }

            return Diagnostics(
                appVersion = BuildConfig.VERSION_NAME,
                protocolVersion = BuildConfig.PROTOCOL_VERSION,
                nodeId = nodeId,
                deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                androidRelease = Build.VERSION.RELEASE,
                apiLevel = Build.VERSION.SDK_INT,
                totalMemoryMb = memoryInfo.totalMem / BYTES_PER_MB,
                availableMemoryMb = memoryInfo.availMem / BYTES_PER_MB,
                lowMemory = memoryInfo.lowMemory,
                freeStorageMb = freeBytes / BYTES_PER_MB,
                displayWidth = metrics.widthPixels,
                displayHeight = metrics.heightPixels,
                displayDensity = metrics.density,
                capabilities = DeviceCapabilities.detect(context),
            )
        }

        private const val BYTES_PER_MB = 1024L * 1024L
    }
}
