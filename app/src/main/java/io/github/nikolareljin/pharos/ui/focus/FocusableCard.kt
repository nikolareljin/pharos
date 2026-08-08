package io.github.nikolareljin.pharos.ui.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.focusable

/**
 * A card that can hold focus, drawn so the focus is unmistakable from across a
 * room.
 *
 * Focus is indicated three ways at once — border colour, border width and a
 * slight scale. That redundancy is deliberate: colour alone fails for a
 * colour-blind viewer and fails again on a television with the contrast wound
 * down, which is most televisions.
 */
@Composable
fun FocusableCard(
    modifier: Modifier = Modifier,
    testTag: String? = null,
    contentDescription: String? = null,
    focusRequester: FocusRequester? = null,
    onSelect: (() -> Unit)? = null,
    content: @Composable ColumnScope.(focused: Boolean) -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.03f else 1f, label = "focusScale")

    val borderColor: Color =
        if (focused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .scale(scale)
            .border(
                width = if (focused) 3.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp),
            )
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                },
            )
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            // clickable rather than a hand-rolled key handler: it answers a tap
            // *and* Select/Enter from a remote, and it makes the card focusable
            // on its own. Handling only key events left the card dead to touch,
            // and this APK installs on phones and tablets too.
            //
            // Directional keys stay with Compose's focus system on purpose —
            // moving focus by hand is how a layout ends up with a corner the
            // remote cannot reach.
            .then(
                if (onSelect != null) {
                    Modifier.clickable(role = Role.Button, onClick = onSelect)
                } else {
                    // Still reachable by the remote; it just does not act.
                    Modifier.focusable()
                },
            ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            content(focused)
        }
    }
}
