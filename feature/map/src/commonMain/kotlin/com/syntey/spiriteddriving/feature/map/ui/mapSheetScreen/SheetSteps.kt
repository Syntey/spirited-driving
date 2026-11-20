package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteOrigin
import kotlinx.coroutines.flow.StateFlow

/**
 * The steps the map sheet holds in its child stack, as configurations. Not serialized: nothing survives process death, as before.
 */
internal sealed interface SheetStepConfig {

    /** The destination search, the root: it stays alive under the steps pushed over it. */
    data object DestinationSearch : SheetStepConfig

    /**
     * The start search, pushed over route planning.
     *
     * @property closesPlanning Whether closing it closes route planning too.
     */
    data class StartSearch(val closesPlanning: Boolean) : SheetStepConfig

    /**
     * Route planning to [destination], pushed over the destination search.
     *
     * @property waitsForStart Whether the start search is pushed over it at once, as the user's location isn't usable.
     */
    data class RoutePlanning(val destination: Place, val waitsForStart: Boolean) : SheetStepConfig

    /**
     * One route on its own: a saved route or a past drive, replacing everything above the root search.
     *
     * @property origin Where the route comes from.
     */
    data class OneRoute(val origin: OneRouteOrigin) : SheetStepConfig

    /**
     * The rating of [question], replacing everything above the root search.
     *
     * @property question The drive's question, which the rating answers.
     */
    data class Rating(val question: DriveQuestion) : SheetStepConfig
}

/** The child component of each sheet step, one per configuration. */
internal sealed interface SheetStepChild {

    /** The state the step shows, which the parent's view state follows. */
    val state: StateFlow<*>

    class PlaceSearch(val component: PlaceSearchComponent) : SheetStepChild {
        override val state: StateFlow<*> get() = component.viewState
    }

    class RoutePlanning(val component: RoutePlanningComponent) : SheetStepChild {
        override val state: StateFlow<*> get() = component.viewState
    }

    class OneRoute(val component: OneRouteComponent) : SheetStepChild {
        override val state: StateFlow<*> get() = component.viewState
    }

    class Rating(val component: RatingComponent) : SheetStepChild {
        override val state: StateFlow<*> get() = component.viewState
    }
}
