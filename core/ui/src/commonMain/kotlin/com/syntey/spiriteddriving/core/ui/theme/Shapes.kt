package com.syntey.spiriteddriving.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

// Liquid Glass and Material 3 Expressive both use capsules (`CircleShape`) and nested rounded corners, so one set fits both
// platforms. A shape inside another is concentric: its radius is the outer radius minus the padding between them.
internal val AppShapes = Shapes(
    // Small badges, route number markers
    extraSmall = RoundedCornerShape(Grid.d2),
    // Text fields, menus
    small = RoundedCornerShape(Grid.d3),
    // Route cards, list groups
    medium = RoundedCornerShape(Grid.d4),
    // Dialogs
    large = RoundedCornerShape(Grid.d6),
    // Sheet top corners
    extraLarge = RoundedCornerShape(Grid.d8),
)
