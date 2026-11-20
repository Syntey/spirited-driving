package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteOrigin
import kotlinx.coroutines.flow.StateFlow

/**
 * The steps the map sheet holds in its child stack, as configurations. Not serialized, so the steps don't survive process death
 * and the configurations can hold domain types that aren't serializable.
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

    /** The kind of sheet the step draws, which the parent's map and sheet content switch on. */
    val activeStep: ActiveSheetStep

    /**
     * @property isStart Whether this is the start search pushed over route planning rather than the destination search.
     */
    class PlaceSearch(val component: PlaceSearchComponent, isStart: Boolean) : SheetStepChild {
        override val state: StateFlow<*> get() = component.viewState
        override val activeStep: ActiveSheetStep = if (isStart) ActiveSheetStep.StartSearch else ActiveSheetStep.Search
    }

    class RoutePlanning(val component: RoutePlanningComponent) : SheetStepChild {
        override val state: StateFlow<*> get() = component.viewState
        override val activeStep: ActiveSheetStep get() = ActiveSheetStep.RoutePlanning
    }

    class OneRoute(val component: OneRouteComponent) : SheetStepChild {
        override val state: StateFlow<*> get() = component.viewState
        override val activeStep: ActiveSheetStep get() = ActiveSheetStep.OneRoute
    }

    class Rating(val component: RatingComponent) : SheetStepChild {
        override val state: StateFlow<*> get() = component.viewState
        override val activeStep: ActiveSheetStep get() = ActiveSheetStep.Rating
    }
}
