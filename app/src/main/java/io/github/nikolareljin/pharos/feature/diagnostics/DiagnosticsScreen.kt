package io.github.nikolareljin.pharos.feature.diagnostics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.nikolareljin.pharos.ui.focus.FocusableCard

const val TAG_DIAGNOSTICS_BACK = "diagnostics_back"
const val TAG_DIAGNOSTICS_LIST = "diagnostics_list"

@Composable
fun DiagnosticsScreen(
    diagnostics: Diagnostics,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backFocus = remember { FocusRequester() }

    // A screen that opens with nothing focused is a screen the remote cannot
    // drive: the first press goes nowhere and looks like a freeze.
    LaunchedEffect(Unit) { backFocus.requestFocus() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(40.dp),
    ) {
        Text("Diagnostics", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "Safe to share — no credentials, no unique hardware identifiers.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .testTag(TAG_DIAGNOSTICS_LIST),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(diagnostics.rows()) { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.width(220.dp),
                    )
                    Text(
                        value,
                        style = MaterialTheme.typography.bodyLarge,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        FocusableCard(
            testTag = TAG_DIAGNOSTICS_BACK,
            contentDescription = "Back",
            focusRequester = backFocus,
            onSelect = onBack,
        ) {
            Text("Back", style = MaterialTheme.typography.titleMedium)
        }
    }
}
