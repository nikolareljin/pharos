package io.github.nikolareljin.pharos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.nikolareljin.pharos.core.identity.NodeIdentity
import io.github.nikolareljin.pharos.feature.diagnostics.Diagnostics
import io.github.nikolareljin.pharos.feature.diagnostics.DiagnosticsScreen
import io.github.nikolareljin.pharos.feature.home.HomeScreen
import io.github.nikolareljin.pharos.ui.theme.PharosTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Which screen is showing.
 *
 * A two-entry enum rather than a navigation library: the screen runtime will
 * decide what is displayed from screen definitions, not from a navigation
 * graph, so a graph added now would be removed later.
 */
enum class PharosScreen { Home, Diagnostics }

@Composable
fun PharosApp(nodeIdentity: NodeIdentity) {
    val context = LocalContext.current
    var screen by rememberSaveable { mutableStateOf(PharosScreen.Home) }

    // Identity generation touches disk, so the first frame renders without it
    // rather than blocking on it. A placeholder that resolves in milliseconds
    // beats a blank screen that is indistinguishable from a crash.
    var nodeId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { nodeId = nodeIdentity.nodeId() }

    PharosTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (screen) {
                PharosScreen.Home -> HomeScreen(
                    nodeId = nodeId ?: "identifying…",
                    appVersion = BuildConfig.VERSION_NAME,
                    onOpenDiagnostics = { screen = PharosScreen.Diagnostics },
                )

                PharosScreen.Diagnostics -> {
                    BackHandler { screen = PharosScreen.Home }

                    // Collected off the main thread, not inside composition:
                    // StatFs and getMemoryInfo both touch the system, and this
                    // codebase's own rule is that a renderer draws and does not
                    // compute.
                    var diagnostics by remember { mutableStateOf<Diagnostics?>(null) }
                    LaunchedEffect(nodeId) {
                        val id = nodeId ?: return@LaunchedEffect
                        diagnostics = withContext(Dispatchers.IO) {
                            Diagnostics.collect(context, id)
                        }
                    }

                    diagnostics?.let {
                        DiagnosticsScreen(
                            diagnostics = it,
                            onBack = { screen = PharosScreen.Home },
                        )
                    } ?: Text("Collecting diagnostics…")
                }
            }
        }
    }
}
