package com.syntey.spiriteddriving.feature.map.tripPlanScreen

/** How far the map's sheet is pulled up. */
enum class SheetHeight {
    /** The search field, and the notes above it. */
    Collapsed,

    /** Half the screen, without the keyboard. */
    Half,

    /** The whole screen, for typing. */
    Full,
}
