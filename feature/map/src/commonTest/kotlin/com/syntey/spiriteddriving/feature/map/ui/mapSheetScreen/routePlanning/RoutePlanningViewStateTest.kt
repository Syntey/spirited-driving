package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.routePlanning

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.miles
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.BudgetChoice
import com.syntey.spiriteddriving.core.domain.route.BudgetKind
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.resources.measure.durationText
import com.syntey.spiriteddriving.feature.map.model.ui.MapSheetPreviewStates
import com.syntey.spiriteddriving.feature.map.model.ui.RoutePlanningState
import com.syntey.spiriteddriving.feature.map.model.ui.UseBudgetButton
import com.syntey.spiriteddriving.feature.map.tools.Constants
import com.syntey.spiriteddriving.feature.map.tools.budgetText
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetViewState
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class RoutePlanningViewStateTest {

    @Test
    fun `a time plan shows the six time chips with its budget selected`() {
        val chips = planning(Budget.Time(30.minutes)).budgetChips

        assertEquals(Constants.Budgets.TIME, chips.map { it.budget })
        assertEquals(listOf(Budget.Time(30.minutes)), chips.filter { it.isSelected }.map { it.budget })
    }

    @Test
    fun `a distance plan shows the six kilometre chips with its budget selected`() {
        val chips = planning(Budget.Distance(50.kilometres)).budgetChips

        assertEquals(Constants.Budgets.DISTANCE_KILOMETRES, chips.map { it.budget })
        assertEquals(listOf(Budget.Distance(50.kilometres)), chips.filter { it.isSelected }.map { it.budget })
    }

    @Test
    fun `no chip is selected for a budget that isn't a chip`() {
        val chips = planning(Budget.Time(25.minutes)).budgetChips

        assertEquals(6, chips.size)
        assertTrue(chips.none { it.isSelected })
    }

    @Test
    fun `the budget kind follows the planning budget`() {
        val state = planning(Budget.Distance(50.kilometres)).copy(budgetChoice = choice(kind = BudgetKind.Time))

        assertEquals(BudgetKind.Distance, state.budgetKind)
    }

    @Test
    fun `the budget kind is the planning's budget and not the choice's`() {
        assertEquals(BudgetKind.Time, planning(Budget.Time(30.minutes)).copy(budgetChoice = choice(kind = BudgetKind.Distance)).budgetKind)
    }

    @Test
    fun `the selected route row is route 1 when planned`() {
        val row = MapSheetPreviewStates.plannedViewState().planning().selectedRouteRow

        assertEquals(1, row?.number)
        assertTrue(row?.isSelected == true)
    }

    @Test
    fun `a distance plan in miles shows the six mile chips with its budget selected`() {
        val chips = planning(Budget.Distance(20.miles), distanceUnit = DistanceUnit.Miles).budgetChips

        assertEquals(Constants.Budgets.DISTANCE_MILES, chips.map { it.budget })
        assertEquals(listOf(Budget.Distance(20.miles)), chips.filter { it.isSelected }.map { it.budget })
    }

    @Test
    fun `the mile chips are labelled in miles`() {
        val chips = planning(Budget.Distance(20.miles), distanceUnit = DistanceUnit.Miles).budgetChips

        assertEquals(budgetText(Budget.Distance(5.miles), DistanceUnit.Miles), chips.first().label)
    }

    @Test
    fun `no fun route message names the budget and the closest fun route`() {
        val text = noFunRoute(budget = Budget.Time(15.minutes), closest = Budget.Time(22.minutes)).noFunRouteMessage?.text

        assertEquals(
            StringDesc.ResourceFormatted(
                MR.strings.planning_no_fun_route,
                budgetText(Budget.Time(15.minutes), DistanceUnit.Kilometres),
                StringDesc.ResourceFormatted(MR.strings.planning_extra, durationText(22.minutes)),
            ),
            text,
        )
    }

    @Test
    fun `the use budget button picks the smallest chip that fits the closest fun route`() {
        val useBudget = noFunRoute(budget = Budget.Time(15.minutes), closest = Budget.Time(22.minutes))
            .noFunRouteMessage?.useBudget

        assertEquals(
            UseBudgetButton(
                budget = Budget.Time(30.minutes),
                label = StringDesc.ResourceFormatted(
                    MR.strings.planning_use_budget,
                    budgetText(Budget.Time(30.minutes), DistanceUnit.Kilometres),
                ),
            ),
            useBudget,
        )
    }

    @Test
    fun `the closest fun route minutes round up to whole minutes`() {
        val rounded = noFunRoute(budget = Budget.Time(15.minutes), closest = Budget.Time(21.minutes + 10.seconds))
        val whole = noFunRoute(budget = Budget.Time(15.minutes), closest = Budget.Time(22.minutes))

        assertEquals(whole.noFunRouteMessage?.text, rounded.noFunRouteMessage?.text)
    }

    @Test
    fun `the use budget button picks the distance chip that fits the closest fun route`() {
        val useBudget = noFunRoute(budget = Budget.Distance(10.kilometres), closest = Budget.Distance(23.kilometres))
            .noFunRouteMessage?.useBudget

        assertEquals(Budget.Distance(30.kilometres), useBudget?.budget)
    }

    @Test
    fun `no fun route message without a chip big enough says it needs more than the largest one`() {
        val message = noFunRoute(budget = Budget.Time(15.minutes), closest = Budget.Time(150.minutes)).noFunRouteMessage

        assertEquals(
            StringDesc.ResourceFormatted(
                MR.strings.planning_no_fun_route_beyond,
                budgetText(Budget.Time(15.minutes), DistanceUnit.Kilometres),
                budgetText(Budget.Time(120.minutes), DistanceUnit.Kilometres),
            ),
            message?.text,
        )
        assertNull(message?.useBudget)
    }

    @Test
    fun `no fun route message without a closest fun route says it needs more than the largest chip`() {
        val message = noFunRoute(budget = Budget.Time(15.minutes), closest = null).noFunRouteMessage

        assertEquals(
            StringDesc.ResourceFormatted(
                MR.strings.planning_no_fun_route_beyond,
                budgetText(Budget.Time(15.minutes), DistanceUnit.Kilometres),
                budgetText(Budget.Time(120.minutes), DistanceUnit.Kilometres),
            ),
            message?.text,
        )
        assertNull(message?.useBudget)
    }

    @Test
    fun `no fun route message is absent when fun routes are listed`() {
        assertNull(MapSheetPreviewStates.plannedViewState().planning().noFunRouteMessage)
    }

    @Test
    fun `no fun route message is absent while planning`() {
        assertNull(MapSheetPreviewStates.planningViewState().planning().noFunRouteMessage)
    }

    private fun MapSheetViewState.planning(): RoutePlanningViewState =
        requireNotNull(sheetStep as? RoutePlanningViewState) { "The sheet shows route planning" }

    private fun planning(budget: Budget, distanceUnit: DistanceUnit = DistanceUnit.Kilometres) = RoutePlanningViewState(
        planning = RoutePlanningState(destination = dresden, budget = budget),
        budgetChoice = choice(kind = BudgetKind.Time),
        distanceUnit = distanceUnit,
    )

    private fun noFunRoute(budget: Budget, closest: Budget?) = RoutePlanningViewState(
        planning = RoutePlanningState(destination = dresden, budget = budget).withPlan(
            FakeRoutePlanner.Recorded.copy(funRoutes = emptyList(), closestFunRouteBudget = closest),
        ),
        budgetChoice = choice(kind = BudgetKind.Time),
        distanceUnit = DistanceUnit.Kilometres,
    )

    private fun choice(kind: BudgetKind) = BudgetChoice(
        time = Budget.Time(30.minutes),
        distance = Budget.Distance(30.kilometres),
        kind = kind,
    )

    private val dresden = Place(name = "Dresden", area = null, location = GeoPoint(latitude = 51.05, longitude = 13.74))
}
