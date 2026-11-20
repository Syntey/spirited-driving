package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

/**
 * The kind of the sheet step on top of the stack, which decides the sheet's content: the destination search, the start search
 * over route planning, route planning, a saved route's or a past drive's one route, or the rating step.
 */
enum class ActiveSheetStep {
    Search,
    StartSearch,
    RoutePlanning,
    OneRoute,
    Rating,
}
