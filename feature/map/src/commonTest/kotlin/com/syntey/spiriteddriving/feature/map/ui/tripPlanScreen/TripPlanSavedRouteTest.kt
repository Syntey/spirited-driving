package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.feature.map.model.ui.RoutePlanningState
import com.syntey.spiriteddriving.feature.map.model.ui.SavedRouteStep
import kotlinx.collections.immutable.toImmutableList
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TripPlanSavedRouteTest {

    private val route = FakeRoutePlanner.Recorded.funRoutes.first()

    @Test
    fun `the selected route is saved when a saved route has its route ID`() {
        val state = stateWith(selectedRouteId = route.id, savedRoutes = listOf(FakeSavedRoutesSource.Recorded))

        assertTrue(state.isSelectedRouteSaved)
    }

    @Test
    fun `the selected route isn't saved when no saved route has its route ID`() {
        val other = FakeRoutePlanner.Recorded.fastest.id
        val state = stateWith(selectedRouteId = other, savedRoutes = listOf(FakeSavedRoutesSource.Recorded))

        assertFalse(state.isSelectedRouteSaved)
    }

    @Test
    fun `without a selected route nothing is saved`() {
        val state = stateWith(selectedRouteId = null, savedRoutes = listOf(FakeSavedRoutesSource.Recorded))

        assertFalse(state.isSelectedRouteSaved)
    }

    @Test
    fun `a saved route's step is saved while its saved route is kept`() {
        val state = stepWith(savedRoutes = listOf(FakeSavedRoutesSource.Recorded))

        assertTrue(state.isSelectedRouteSaved)
    }

    @Test
    fun `a saved route's step is unsaved once its saved route is removed`() {
        assertFalse(stepWith(savedRoutes = emptyList()).isSelectedRouteSaved)
    }

    @Test
    fun `a saved route's step is saved by its own saved route, not by another saved route of its route`() {
        val other = FakeSavedRoutesSource.Recorded.copy(id = "saved-2")

        assertFalse(stepWith(savedRoutes = listOf(other)).isSelectedRouteSaved)
    }

    private fun stepWith(savedRoutes: List<SavedRoute>): TripPlanViewState = TripPlanViewState(
        savedRouteStep = SavedRouteStep(savedRoute = FakeSavedRoutesSource.Recorded, route = route),
        savedRoutes = savedRoutes.toImmutableList(),
    )

    private fun stateWith(selectedRouteId: String?, savedRoutes: List<SavedRoute>): TripPlanViewState {
        val planning = RoutePlanningState(
            destination = Place(name = "Karlštejn Castle", area = null, location = route.destination.location),
            budget = TripPlanViewState().budgetChoice.budget,
            selectedRouteId = selectedRouteId,
        )
        return TripPlanViewState(planning = planning, savedRoutes = savedRoutes.toImmutableList())
    }
}
