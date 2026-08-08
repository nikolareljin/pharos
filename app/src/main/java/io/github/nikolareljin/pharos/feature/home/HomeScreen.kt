package io.github.nikolareljin.pharos.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.nikolareljin.pharos.ui.focus.FocusableCard

const val TAG_HOME_STATUS = "home_status"
const val TAG_HOME_DIAGNOSTICS = "home_diagnostics"
const val TAG_HOME_TITLE = "home_title"

/**
 * The placeholder home screen for the bootstrap: enough to prove the app
 * launches, renders, and is fully navigable with a remote. The screen runtime
 * replaces it — this is not the dashboard.
 */
@Composable
fun HomeScreen(
    nodeId: String,
    appVersion: String,
    onOpenDiagnostics: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstFocus = remember { FocusRequester() }

    // A screen that opens with nothing focused is a screen the remote cannot
    // drive: the first press goes nowhere and reads as a freeze.
    LaunchedEffect(Unit) { firstFocus.requestFocus() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(48.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "Pharos",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag(TAG_HOME_TITLE),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Display node · $appVersion",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(40.dp))

        ResponsiveCards(
            cards = listOf(
                { cardModifier ->
                    FocusableCard(
                        modifier = cardModifier,
                        testTag = TAG_HOME_STATUS,
                        contentDescription = "Node status",
                        focusRequester = firstFocus,
                    ) {
                        Text("Node", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            nodeId,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "No provider configured — standalone",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                },
                { cardModifier ->
                    FocusableCard(
                        modifier = cardModifier,
                        testTag = TAG_HOME_DIAGNOSTICS,
                        contentDescription = "Open diagnostics",
                        onSelect = onOpenDiagnostics,
                    ) {
                        Text("Diagnostics", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Versions, memory, display, capabilities",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            ),
        )
    }
}

/**
 * Side by side when there is room, stacked when there is not.
 *
 * The breakpoint is available width rather than a device-type flag: a tablet in
 * landscape and a television are the same layout problem, and a foldable is
 * both within one session. Two cards in a row on a portrait phone wrap every
 * label onto four lines — which is what "designed for TV" looks like in a hand.
 */
@Composable
private fun ResponsiveCards(
    cards: List<@Composable (Modifier) -> Unit>,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        if (maxWidth >= WIDE_BREAKPOINT) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                cards.forEach { card -> card(Modifier.weight(1f)) }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                cards.forEach { card -> card(Modifier.fillMaxWidth()) }
            }
        }
    }
}

private val WIDE_BREAKPOINT = 700.dp
