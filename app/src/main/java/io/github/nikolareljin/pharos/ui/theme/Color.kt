package io.github.nikolareljin.pharos.ui.theme

import androidx.compose.ui.graphics.Color

// The palette is lifted from brand/pharos-logo.svg so the app and the mark are
// the same object. Names describe the role, not the hue, because the light
// theme reassigns them.

/** Deep slate — the night the lighthouse stands in. */
val PharosSlate900 = Color(0xFF0F172A)

/** Raised slate — cards and surfaces one step above the background. */
val PharosSlate800 = Color(0xFF1E293B)

/** Hairline borders and dividers. */
val PharosSlate700 = Color(0xFF334155)

val PharosSlate200 = Color(0xFFE2E8F0)
val PharosSlate50 = Color(0xFFF8FAFC)

/** The beacon fire. Focus, primary action, "look here". */
val PharosAmber = Color(0xFFF59E0B)

/** The light it throws. */
val PharosAmberLight = Color(0xFFFBBF24)

/** The flame's core — the brightest thing on any screen, used sparingly. */
val PharosFlame = Color(0xFFFEF08A)

/** The tower itself. Structure, secondary emphasis, links. */
val PharosSky = Color(0xFF38BDF8)

val PharosSkyDeep = Color(0xFF0284C7)

// Status colours. Meaning is never carried by colour alone — every status also
// carries a label or an icon, because a colour-blind viewer and a badly
// calibrated television fail in the same way.
val PharosOk = Color(0xFF34D399)
val PharosWarn = Color(0xFFFBBF24)
val PharosError = Color(0xFFF87171)
