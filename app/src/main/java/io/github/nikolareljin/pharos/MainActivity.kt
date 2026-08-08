package io.github.nikolareljin.pharos

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // A display node exists to be looked at. Letting the screen sleep after
        // fifteen minutes defeats the entire product; this is not a preference.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        hideSystemBars()

        val application = application as PharosApplication

        setContent {
            PharosApp(nodeIdentity = application.nodeIdentity)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // System UI comes back on its own after certain interactions; putting it
        // away again on regaining focus is what keeps a signage screen clean
        // over days rather than minutes.
        if (hasFocus) hideSystemBars()
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
