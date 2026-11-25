package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteOrigin
import com.syntey.spiriteddriving.feature.map.model.ui.SheetStep
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.oneRoute.OneRouteComponent
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.placeSearch.PlaceSearchComponent
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.rating.RatingComponent
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.routePlanning.RoutePlanningComponent
import kotlinx.coroutines.flow.Flow

/**
 * The steps the map sheet holds in its child stack, as configurations. Not serialized, so the steps don't survive process death
 * and the configurations can hold domain types that aren't serializable.
 */
internal sealed interface SheetStepConfig {

    /** The destination search, the root: it stays alive under the steps pushed over it. */
    data object DestinationSearch : SheetStepConfig

    /**
     * Route planning to [destination], pushed over the destination search.
     *
     * @property waitsForStart Whether planning opens with its start search, as the user's location isn't usable.
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

    /** What the parent's view state follows. */
    val state: Flow<*>

    /** The kind of sheet the step draws, which the parent's map and sheet content switch on. */
    val activeStep: ActiveSheetStep

    /** The step's pins, route line and bounds on the map, when it shows one route instead of search and planning; null for the others. */
    val sheetStep: SheetStep? get() = null

    class PlaceSearch(val component: PlaceSearchComponent) : SheetStepChild {
        override val state: Flow<*> get() = component.viewState
        override val activeStep: ActiveSheetStep = ActiveSheetStep.Search
    }

    class RoutePlanning(val component: RoutePlanningComponent) : SheetStepChild {
        override val state: Flow<*> get() = component.stateChanges
        override val activeStep: ActiveSheetStep = ActiveSheetStep.RoutePlanning(component)
        override val sheetStep: SheetStep get() = component.viewState.value
    }

    class OneRoute(val component: OneRouteComponent) : SheetStepChild {
        override val state: Flow<*> get() = component.viewState
        override val activeStep: ActiveSheetStep = ActiveSheetStep.OneRoute(component)
        override val sheetStep: SheetStep get() = component.viewState.value.step
    }

    class Rating(val component: RatingComponent) : SheetStepChild {
        override val state: Flow<*> get() = component.viewState
        override val activeStep: ActiveSheetStep = ActiveSheetStep.Rating(component)
        override val sheetStep: SheetStep get() = component.viewState.value.step
    }
}
