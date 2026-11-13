package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.coverage.FakeCoverageSource
import com.syntey.spiriteddriving.core.domain.geo.GeoBounds
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.miles
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.PlaceKind
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.BudgetChoice
import com.syntey.spiriteddriving.core.domain.route.BudgetKind
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.feature.map.model.ui.MyLocationRow
import com.syntey.spiriteddriving.feature.map.model.ui.RouteMapState
import com.syntey.spiriteddriving.feature.map.model.ui.RoutePlanningState
import com.syntey.spiriteddriving.feature.map.model.ui.SearchState
import com.syntey.spiriteddriving.feature.map.model.ui.StartSearch
import com.syntey.spiriteddriving.feature.map.model.ui.TripPlanPreviewStates
import com.syntey.spiriteddriving.feature.map.model.ui.UseBudgetButton
import com.syntey.spiriteddriving.feature.map.tools.budgetText
import com.syntey.spiriteddriving.feature.map.tools.durationText
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanViewState
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import com.syntey.spiriteddriving.feature.map.tools.Constants

class TripPlanViewStateTest {

    @Test
    fun `the precise location note shows with approximate access`() {
        assertTrue(approximate(isPreciseLocationNoteClosed = false).showsPreciseLocationNote)
    }

    @Test
    fun `the precise location note hides with precise access`() {
        val state = approximate(isPreciseLocationNoteClosed = false).copy(locationAccess = LocationAccess.Precise)

        assertFalse(state.showsPreciseLocationNote)
    }

    @Test
    fun `the precise location note hides once closed`() {
        assertFalse(approximate(isPreciseLocationNoteClosed = true).showsPreciseLocationNote)
    }

    @Test
    fun `the precise location note hides while the user is outside the covered area`() {
        val dresden = GeoPoint(latitude = 51.05, longitude = 13.74)
        val state = approximate(isPreciseLocationNoteClosed = false).let {
            it.copy(map = it.map.copy(userLocation = dresden, coveredArea = FakeCoverageSource.Czechia.area))
        }

        assertFalse(state.showsPreciseLocationNote)
    }

    @Test
    fun `a row shows the distance to a tenth with precise access`() {
        val row = stateWithSearch(access = LocationAccess.Precise, from = origin).suggestionRows.single()

        assertEquals(km("4.2"), row.distance)
    }

    @Test
    fun `a row shows whole kilometres with approximate access`() {
        val row = stateWithSearch(access = LocationAccess.Approximate, from = origin).suggestionRows.single()

        assertEquals(km("4"), row.distance)
    }

    @Test
    fun `a row has no distance without location access`() {
        val row = stateWithSearch(access = LocationAccess.Denied, from = origin).suggestionRows.single()

        assertNull(row.distance)
    }

    @Test
    fun `a row has no distance before the location is known`() {
        val row = stateWithSearch(access = LocationAccess.Precise, from = null).suggestionRows.single()

        assertNull(row.distance)
    }

    @Test
    fun `a row shows the distance in miles when the unit is miles`() {
        val row = stateWithSearch(access = LocationAccess.Precise, from = origin, unit = DistanceUnit.Miles).suggestionRows.single()

        assertEquals(mi("2.6"), row.distance)
    }

    @Test
    fun `a row bolds the parts of the name that match the text`() {
        val row = stateWithSearch(access = LocationAccess.Precise, from = origin).suggestionRows.single()

        assertEquals(listOf(0..5), row.boldRanges)
    }

    @Test
    fun `rows keep the order of the suggestions`() {
        val state = stateWithSearch(access = LocationAccess.Precise, from = origin)
        val rows = state.copy(search = state.search.copy(suggestions = listOf(karlstejn, prague).toImmutableList())).suggestionRows

        assertEquals(listOf("Karlštejn", "Karlštejnská"), rows.map { it.suggestion.place.name })
    }

    @Test
    fun `a recent row shows its distance`() {
        val state = TripPlanViewState(
            locationAccess = LocationAccess.Precise,
            map = RouteMapState(userLocation = origin),
            search = SearchState(recentPlaces = listOf(karlstejn.place).toImmutableList()),
        )

        assertEquals(km("4.2"), state.recentRows.single().distance)
    }

    @Test
    fun `a recent row outside a loaded covered area is not covered`() {
        val state = TripPlanViewState(
            map = RouteMapState(coveredArea = FakeCoverageSource.Czechia.area),
            search = SearchState(recentPlaces = listOf(dresden).toImmutableList()),
        )

        assertFalse(state.recentRows.single().isCovered)
    }

    @Test
    fun `a recent row is covered while the covered area isn't loaded`() {
        val state = TripPlanViewState(search = SearchState(recentPlaces = listOf(dresden).toImmutableList()))

        assertTrue(state.recentRows.single().isCovered)
    }

    @Test
    fun `a precise location in the covered area is usable`() {
        val state = TripPlanViewState(locationAccess = LocationAccess.Precise, map = RouteMapState(userLocation = origin))

        assertTrue(state.hasUsableLocation)
    }

    @Test
    fun `a precise location is usable before it is known`() {
        assertTrue(TripPlanViewState(locationAccess = LocationAccess.Precise).hasUsableLocation)
    }

    @Test
    fun `an approximate location isn't usable`() {
        val state = TripPlanViewState(locationAccess = LocationAccess.Approximate, map = RouteMapState(userLocation = origin))

        assertFalse(state.hasUsableLocation)
    }

    @Test
    fun `a location without access isn't usable`() {
        val state = TripPlanViewState(locationAccess = LocationAccess.Denied, map = RouteMapState(userLocation = origin))

        assertFalse(state.hasUsableLocation)
    }

    @Test
    fun `a precise location outside a loaded covered area isn't usable`() {
        val state = TripPlanViewState(
            locationAccess = LocationAccess.Precise,
            map = RouteMapState(userLocation = dresdenPoint, coveredArea = FakeCoverageSource.Czechia.area),
        )

        assertFalse(state.hasUsableLocation)
    }

    @Test
    fun `the my location row is absent without the start step`() {
        assertNull(TripPlanViewState(locationAccess = LocationAccess.Precise, map = RouteMapState(userLocation = origin)).myLocationRow)
    }

    @Test
    fun `the my location row is absent while the search has text`() {
        assertNull(startStep(access = LocationAccess.Precise, from = origin, text = "karlst").myLocationRow)
    }

    @Test
    fun `the my location row needs location access when it is denied`() {
        assertEquals(MyLocationRow.NeedsLocationAccess, startStep(access = LocationAccess.Denied, from = origin).myLocationRow)
    }

    @Test
    fun `the my location row needs location access before access is read`() {
        assertEquals(MyLocationRow.NeedsLocationAccess, startStep(access = null, from = origin).myLocationRow)
    }

    @Test
    fun `the my location row needs precise location with approximate access`() {
        assertEquals(
            MyLocationRow.NeedsPreciseLocation,
            startStep(access = LocationAccess.Approximate, from = origin).myLocationRow,
        )
    }

    @Test
    fun `the my location row is outside the covered area when the user is outside it`() {
        val state = startStep(access = LocationAccess.Precise, from = dresdenPoint).let {
            it.copy(map = it.map.copy(coveredArea = FakeCoverageSource.Czechia.area))
        }

        assertEquals(MyLocationRow.OutsideCoveredArea, state.myLocationRow)
    }

    @Test
    fun `the my location row is available with precise access in the covered area`() {
        val state = startStep(access = LocationAccess.Precise, from = origin).let {
            it.copy(map = it.map.copy(coveredArea = FakeCoverageSource.Czechia.area))
        }

        assertEquals(MyLocationRow.Available, state.myLocationRow)
    }

    @Test
    fun `planning from a start picked by hand frames the start and the destination and not the user's location`() {
        val start = GeoPoint(latitude = 49.0, longitude = 12.0)
        val state = TripPlanViewState(
            locationAccess = LocationAccess.Precise,
            map = RouteMapState(userLocation = origin),
            planning = RoutePlanningState(
                destination = dresden,
                start = Place(name = null, area = null, location = start),
                budget = Budget.Time(30.minutes),
            ),
        )

        assertEquals(GeoBounds(southWest = start, northEast = dresden.location), state.routeBounds)
    }

    @Test
    fun `no budget chips without planning`() {
        assertTrue(TripPlanViewState().budgetChips.isEmpty())
    }

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
    fun `the budget kind is the choice's without planning`() {
        assertEquals(BudgetKind.Distance, TripPlanViewState(budgetChoice = choice(kind = BudgetKind.Distance)).budgetKind)
    }

    @Test
    fun `there is no selected route row without planning`() {
        assertNull(TripPlanViewState().selectedRouteRow)
    }

    @Test
    fun `the selected route row is route 1 when planned`() {
        val row = TripPlanPreviewStates.plannedViewState().selectedRouteRow

        assertEquals(1, row?.number)
        assertTrue(row?.isSelected == true)
    }

    @Test
    fun `a distance plan in miles shows the six mile chips with its budget selected`() {
        val chips = planning(Budget.Distance(20.miles)).copy(distanceUnit = DistanceUnit.Miles).budgetChips

        assertEquals(Constants.Budgets.DISTANCE_MILES, chips.map { it.budget })
        assertEquals(listOf(Budget.Distance(20.miles)), chips.filter { it.isSelected }.map { it.budget })
    }

    @Test
    fun `the mile chips are labelled in miles`() {
        val chips = planning(Budget.Distance(20.miles)).copy(distanceUnit = DistanceUnit.Miles).budgetChips

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
        assertNull(TripPlanPreviewStates.plannedViewState().noFunRouteMessage)
    }

    @Test
    fun `no fun route message is absent while planning`() {
        assertNull(TripPlanPreviewStates.planningViewState().noFunRouteMessage)
    }

    private fun noFunRoute(budget: Budget, closest: Budget?) = TripPlanViewState(
        planning = RoutePlanningState(destination = dresden, budget = budget).withPlan(
            FakeRoutePlanner.Recorded.copy(funRoutes = emptyList(), closestFunRouteBudget = closest),
        ),
    )

    private fun planning(budget: Budget) = TripPlanViewState(
        planning = RoutePlanningState(destination = dresden, budget = budget),
    )

    private fun choice(kind: BudgetKind) = BudgetChoice(
        time = Budget.Time(30.minutes),
        distance = Budget.Distance(30.kilometres),
        kind = kind,
    )

    private val dresden = Place(name = "Dresden", area = null, location = GeoPoint(latitude = 51.05, longitude = 13.74))

    private val origin = GeoPoint(latitude = 50.0, longitude = 14.0)

    private val dresdenPoint = GeoPoint(latitude = 51.05, longitude = 13.74)

    private fun startStep(access: LocationAccess?, from: GeoPoint?, text: String = "") = TripPlanViewState(
        locationAccess = access,
        map = RouteMapState(userLocation = from),
        search = SearchState(text = text),
        startSearch = StartSearch(destinationSearch = SearchState(), closesPlanning = false),
    )

    // 4.2 km north of origin
    private val karlstejn = suggestion(name = "Karlštejn", location = GeoPoint(latitude = 50.03777151, longitude = 14.0))

    private val prague = suggestion(name = "Karlštejnská", location = GeoPoint(latitude = 50.0755, longitude = 14.4378))

    private fun stateWithSearch(access: LocationAccess?, from: GeoPoint?, unit: DistanceUnit = DistanceUnit.Kilometres) =
        TripPlanViewState(
            locationAccess = access,
            map = RouteMapState(userLocation = from),
            search = SearchState(text = "karlst", suggestions = listOf(karlstejn).toImmutableList()),
            distanceUnit = unit,
        )

    private fun suggestion(name: String, location: GeoPoint) =
        PlaceSuggestion(
            place = Place(name = name, area = null, location = location),
            isCovered = true,
            kind = PlaceKind.Town,
        )

    private fun approximate(isPreciseLocationNoteClosed: Boolean) = TripPlanViewState(
        locationAccess = LocationAccess.Approximate,
        isPreciseLocationNoteClosed = isPreciseLocationNoteClosed,
    )

    private fun km(number: String) = StringDesc.ResourceFormatted(MR.strings.unit_kilometres, number)

    private fun mi(number: String) = StringDesc.ResourceFormatted(MR.strings.unit_miles, number)
}
