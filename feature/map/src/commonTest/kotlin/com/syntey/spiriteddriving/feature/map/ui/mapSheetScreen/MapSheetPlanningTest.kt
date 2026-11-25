package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.miles
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.BudgetKind
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.route.OutsideCoverageException
import com.syntey.spiriteddriving.core.domain.route.TripPlan
import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.feature.map.model.ui.PlanningStatus
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class MapSheetPlanningTest {

    private val main = MapSheetTestDispatcher()

    @BeforeTest
    fun setUp() = main.install()

    @AfterTest
    fun tearDown() = main.uninstall()

    @Test
    fun `choosing a budget plans again with it`() = runTest(main.dispatcher) {
        val trip = planFromPrague()

        trip.planning.actions.onBudgetClick(Budget.Time(60.minutes))
        assertEquals(PlanningStatus.Planning, trip.planningState?.planning?.status)
        advanceUntilIdle()

        assertEquals(Budget.Time(60.minutes), trip.planningState?.planning?.budget)
        assertEquals(PlanningStatus.Planned, trip.planningState?.planning?.status)
    }

    @Test
    fun `the budget chosen is kept for the next trip`() = runTest(main.dispatcher) {
        val trip = planFromPrague()
        trip.planning.actions.onBudgetClick(Budget.Time(60.minutes))
        advanceUntilIdle()

        trip.planning.actions.onCloseClick()
        trip.screen.actions.onSuggestionClick(firstCovered(trip))
        advanceUntilIdle()

        assertEquals(Budget.Time(60.minutes), trip.planningState?.planning?.budget)
    }

    @Test
    fun `switching the budget to distance plans again in kilometres`() = runTest(main.dispatcher) {
        val trip = planFromPrague()

        trip.planning.actions.onBudgetKindChange(BudgetKind.Distance)
        assertEquals(PlanningStatus.Planning, trip.planningState?.planning?.status)
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertEquals(Budget.Distance(30.kilometres), trip.planningState?.planning?.budget)
        assertEquals(BudgetKind.Distance, trip.planning.viewState.value.budgetKind)
        assertEquals(DistanceUnit.Kilometres, state.distanceUnit)
        assertEquals(PlanningStatus.Planned, trip.planningState?.planning?.status)
    }

    @Test
    fun `changing the distance unit moves a distance budget`() = runTest(main.dispatcher) {
        val trip = planFromPrague()
        trip.planning.actions.onBudgetKindChange(BudgetKind.Distance)
        advanceUntilIdle()
        trip.routePlanner.plan = otherPlan

        trip.distanceUnits.set(DistanceUnit.Miles)
        advanceUntilIdle()

        val state = trip.screen.viewState.value
        assertEquals(DistanceUnit.Miles, state.distanceUnit)
        // 30 km is nearest to 20 miles among the mile budgets
        assertEquals(Budget.Distance(20.miles), trip.planningState?.planning?.budget)
    }

    @Test
    fun `changing the distance unit plans again`() = runTest(main.dispatcher) {
        val trip = planFromPrague()
        trip.planning.actions.onBudgetKindChange(BudgetKind.Distance)
        advanceUntilIdle()
        trip.routePlanner.plan = otherPlan

        trip.distanceUnits.set(DistanceUnit.Miles)
        advanceUntilIdle()

        assertEquals(otherPlan, trip.planningState?.planning?.plan)
        assertEquals(PlanningStatus.Planned, trip.planningState?.planning?.status)
    }

    @Test
    fun `changing the vignettes plans again`() = runTest(main.dispatcher) {
        val trip = planFromPrague()
        trip.routePlanner.plan = otherPlan

        trip.vignettes.setHasVignette(CountryCode.Czechia, hasVignette = true)
        advanceUntilIdle()

        assertEquals(otherPlan, trip.planningState?.planning?.plan)
        assertEquals(PlanningStatus.Planned, trip.planningState?.planning?.status)
    }

    @Test
    fun `an offline plan shows offline`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(locationAccess = LocationAccess.Precise)
        advanceUntilIdle()
        sendFix(trip, Points.prague)
        trip.routePlanner.failure = OfflineException(cause = null)

        trip.screen.actions.onSuggestionClick(searchForCovered(trip))
        advanceUntilIdle()

        assertEquals(PlanningStatus.Offline, trip.planningState?.planning?.status)
    }

    @Test
    fun `Retry after an offline plan plans again`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(locationAccess = LocationAccess.Precise)
        advanceUntilIdle()
        sendFix(trip, Points.prague)
        trip.routePlanner.failure = OfflineException(cause = null)
        trip.screen.actions.onSuggestionClick(searchForCovered(trip))
        advanceUntilIdle()

        trip.routePlanner.failure = null
        trip.planning.actions.onRetryClick()
        advanceUntilIdle()

        assertEquals(PlanningStatus.Planned, trip.planningState?.planning?.status)
    }

    @Test
    fun `a plan outside the covered area shows not covered`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(locationAccess = LocationAccess.Precise)
        advanceUntilIdle()
        sendFix(trip, Points.prague)
        trip.routePlanner.failure = OutsideCoverageException(cause = null)

        trip.screen.actions.onSuggestionClick(searchForCovered(trip))
        advanceUntilIdle()

        assertEquals(PlanningStatus.NotCovered, trip.planningState?.planning?.status)
    }

    @Test
    fun `a failed plan shows failed`() = runTest(main.dispatcher) {
        val trip = mapSheetFixture(locationAccess = LocationAccess.Precise)
        advanceUntilIdle()
        sendFix(trip, Points.prague)
        trip.routePlanner.failure = IllegalStateException("Backend broke")

        trip.screen.actions.onSuggestionClick(searchForCovered(trip))
        advanceUntilIdle()

        assertEquals(PlanningStatus.Failed, trip.planningState?.planning?.status)
    }

    @Test
    fun `closing route planning brings the search back at full height`() = runTest(main.dispatcher) {
        val trip = planFromPrague()

        trip.planning.actions.onCloseClick()

        val state = trip.screen.viewState.value
        assertNull(trip.planningState)
        assertEquals(SheetHeight.Full, state.sheetHeight)
        assertEquals("karlst", state.search.text)
    }

    @Test
    fun `closing route planning clears the pins`() = runTest(main.dispatcher) {
        val trip = planFromPrague()
        assertNotNull(trip.screen.viewState.value.map.destination)

        trip.planning.actions.onCloseClick()

        val state = trip.screen.viewState.value
        assertNull(state.map.destination)
        assertNull(state.map.start)
        assertTrue(state.routeLines.isEmpty())
    }

    @Test
    fun `the user's first location plans a trip that was waiting for it`() = runTest(main.dispatcher) {
        // Precise access without a fix yet: the trip is chosen, but has no location to start from
        val trip = mapSheetFixture(locationAccess = LocationAccess.Precise)
        advanceUntilIdle()
        trip.screen.actions.onSuggestionClick(searchForCovered(trip))
        advanceUntilIdle()
        assertEquals(PlanningStatus.Planning, trip.planningState?.planning?.status)

        trip.planning.actions.onStartClick()
        trip.screen.actions.onMyLocationClick()
        advanceUntilIdle()
        assertNull(trip.screen.viewState.value.startSearch)
        assertEquals(PlanningStatus.Planning, trip.planningState?.planning?.status)

        sendFix(trip, Points.prague)

        assertEquals(PlanningStatus.Planned, trip.planningState?.planning?.status)
        assertNull(trip.planningState?.planning?.start)
    }

    /** A trip planned from the user's location at Prague, with the destination's first covered suggestion chosen. */
    private fun TestScope.planFromPrague(): MapSheetFixture {
        val trip = mapSheetFixture(locationAccess = LocationAccess.Precise)
        advanceUntilIdle()
        sendFix(trip, Points.prague)
        trip.screen.actions.onSuggestionClick(searchForCovered(trip))
        advanceUntilIdle()
        return trip
    }
}

/** The recorded plan with another fastest route, so a test sees the result of a new plan on screen. */
private val otherPlan: TripPlan = FakeRoutePlanner.Recorded.copy(
    fastest = FakeRoutePlanner.Recorded.fastest.copy(id = "other-fastest"),
)
