package com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen

import com.syntey.spiriteddriving.core.domain.coverage.FakeCoverageSource
import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
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
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.feature.map.model.ui.MyLocationRow
import com.syntey.spiriteddriving.feature.map.model.ui.RatingStep
import com.syntey.spiriteddriving.feature.map.model.ui.ReminderBubble
import com.syntey.spiriteddriving.feature.map.model.ui.RouteMapState
import com.syntey.spiriteddriving.feature.map.model.ui.RoutePlanningState
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteStatus
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteOrigin
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteStep
import com.syntey.spiriteddriving.feature.map.model.ui.SearchState
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import com.syntey.spiriteddriving.feature.map.model.ui.StartSearch
import com.syntey.spiriteddriving.feature.map.model.ui.TripPlanPreviewStates
import com.syntey.spiriteddriving.feature.map.model.ui.UseBudgetButton
import com.syntey.spiriteddriving.feature.map.tools.budgetText
import com.syntey.spiriteddriving.core.resources.measure.durationText
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanViewState
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.desc.StringDesc
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
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
    fun `drive is disabled without a selected route`() {
        assertFalse(planning(Budget.Time(30.minutes)).isDriveEnabled)
    }

    @Test
    fun `drive is enabled with a selected route`() {
        assertTrue(stateStartingAt(start = origin, from = null).isDriveEnabled)
    }

    @Test
    fun `the start distance is null without a selected route`() {
        val state = planning(Budget.Time(30.minutes)).let { it.copy(map = RouteMapState(userLocation = origin)) }

        assertNull(state.startDistance)
    }

    @Test
    fun `the start distance is null when the route starts near the user`() {
        val near = GeoPoint(latitude = origin.latitude + 0.001, longitude = origin.longitude)

        assertNull(stateStartingAt(start = near, from = origin).startDistance)
    }

    @Test
    fun `the start distance is null without the user's location`() {
        val far = GeoPoint(latitude = origin.latitude + 0.045, longitude = origin.longitude)

        assertNull(stateStartingAt(start = far, from = null).startDistance)
    }

    @Test
    fun `the start distance is the distance to the route's start when it's far`() {
        // About 5 km north of the origin
        val far = GeoPoint(latitude = origin.latitude + 0.045, longitude = origin.longitude)

        val distance = assertNotNull(stateStartingAt(start = far, from = origin).startDistance)

        assertTrue(abs(distance.inKilometres - 5.0) < 0.05)
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

    @Test
    fun `the rating step draws only the driven line`() {
        assertEquals(listOf("drive"), ratingState().routeLines.map { it.id })
    }

    @Test
    fun `the rating step's line is selected and numbered`() {
        val line = ratingState().routeLines.single()

        assertTrue(line.isSelected)
        assertEquals(1, line.number)
    }

    @Test
    fun `the rating step's line has no label`() {
        assertNull(ratingState().routeLines.single().label)
    }

    @Test
    fun `the rating step frames the driven line`() {
        val bounds = assertNotNull(ratingState().routeBounds)
        val line = FakeRoutePlanner.Recorded.funRoutes.first().line

        assertTrue(
            line.all { point ->
                point.latitude in bounds.southWest.latitude..bounds.northEast.latitude &&
                    point.longitude in bounds.southWest.longitude..bounds.northEast.longitude
            },
        )
    }

    @Test
    fun `the map buttons hide during the rating step`() {
        assertFalse(ratingState().showsMapButtons)
    }

    @Test
    fun `the rating title names the destination`() {
        assertEquals(StringDesc.ResourceFormatted(MR.strings.drive_feedback_question, "Karlštejn Castle"), ratingState().ratingTitle)
    }

    @Test
    fun `the rating title asks without a name for an unnamed destination`() {
        assertEquals(MR.strings.drive_feedback_question_unnamed.desc(), ratingState(destinationName = null).ratingTitle)
    }

    @Test
    fun `the drive text joins the time, the planned duration and the distance`() {
        val expected = StringDesc.ResourceFormatted(
            MR.strings.drive_feedback_drive,
            "Today, 14:20".desc(),
            StringDesc.ResourceFormatted(MR.strings.unit_hours_minutes, 1, 5),
            km("58"),
        )

        assertEquals(expected, ratingState().ratingDriveText)
    }

    @Test
    fun `there are no reason chips before a thumb`() {
        assertTrue(ratingState().reasonChips.isEmpty())
    }

    @Test
    fun `there is no thanks before a thumb`() {
        assertNull(ratingState().ratingThanks)
    }

    @Test
    fun `a Good rating thanks for what stood out`() {
        assertEquals(MR.strings.drive_feedback_thanks_good.desc(), ratingState(Rating(Thumb.Up)).ratingThanks)
    }

    @Test
    fun `a Not good rating asks what didn't work`() {
        assertEquals(MR.strings.drive_feedback_thanks_not_good.desc(), ratingState(Rating(Thumb.Down)).ratingThanks)
    }

    @Test
    fun `a Good rating gives its five reason chips in order`() {
        val reasons = ratingState(Rating(Thumb.Up)).reasonChips.map { it.reason }

        assertEquals(
            listOf(
                RatingReason.CurvyRoads,
                RatingReason.NiceViews,
                RatingReason.LittleTraffic,
                RatingReason.SmoothSurface,
                RatingReason.WorthTheExtraTime,
            ),
            reasons,
        )
    }

    @Test
    fun `a Not good rating gives its eight reason chips in order`() {
        val reasons = ratingState(Rating(Thumb.Down)).reasonChips.map { it.reason }

        assertEquals(
            listOf(
                RatingReason.BoringRoads,
                RatingReason.TooManyTowns,
                RatingReason.TooNarrow,
                RatingReason.RoughSurface,
                RatingReason.HeavyTraffic,
                RatingReason.Roadworks,
                RatingReason.TookTooLong,
                RatingReason.MapsAppWentAnotherWay,
            ),
            reasons,
        )
    }

    @Test
    fun `the picked reason chip is selected`() {
        val chips = ratingState(Rating(Thumb.Up, setOf(RatingReason.NiceViews))).reasonChips

        assertEquals(listOf(RatingReason.NiceViews), chips.filter { it.isSelected }.map { it.reason })
    }

    @Test
    fun `a reason chip has the text of its reason`() {
        val chip = ratingState(Rating(Thumb.Up)).reasonChips.single { it.reason == RatingReason.NiceViews }

        assertEquals(MR.strings.reason_nice_views.desc(), chip.label)
    }

    // The rating step after the first fun route of the recorded plan, driven at 14:20 today from the start to the castle
    @Test
    fun `the reminder shows on the collapsed search step`() {
        assertTrue(reminderState().showsReminder)
    }

    @Test
    fun `the reminder hides while the sheet is pulled up`() {
        assertFalse(reminderState().copy(sheetHeight = SheetHeight.Half).showsReminder)
    }

    @Test
    fun `the reminder hides while planning`() {
        val planning = RoutePlanningState(destination = dresden, budget = Budget.Time(30.minutes))
        assertFalse(reminderState().copy(planning = planning).showsReminder)
    }

    @Test
    fun `the reminder hides during the rating step`() {
        assertFalse(ratingState().copy(sheetHeight = SheetHeight.Collapsed, reminder = reminder()).showsReminder)
    }

    @Test
    fun `the reminder text joins the destination's name and the day`() {
        val expected = StringDesc.ResourceFormatted(
            MR.strings.drive_feedback_reminder_text,
            "Karlštejn Castle",
            MR.strings.drive_feedback_yesterday_day.desc(),
        )

        assertEquals(expected, reminderState().reminderText)
    }

    @Test
    fun `the reminder text reads as a point on the map for an unnamed destination`() {
        val expected = StringDesc.ResourceFormatted(
            MR.strings.drive_feedback_reminder_text,
            MR.strings.search_point_on_map.desc(),
            MR.strings.drive_feedback_yesterday_day.desc(),
        )

        assertEquals(expected, reminderState(destinationName = null).reminderText)
    }

    private fun reminderState(destinationName: String? = "Karlštejn Castle") = TripPlanViewState(
        reminder = reminder(destinationName),
    )

    private fun reminder(destinationName: String? = "Karlštejn Castle"): ReminderBubble {
        val route = FakeRoutePlanner.Recorded.funRoutes.first()
        val question = DriveQuestion(
            driveId = "drive",
            startedAt = Instant.parse("2026-11-15T13:20:00Z"),
            routeNumber = 1,
            start = route.start.location,
            destination = route.destination.copy(name = destinationName),
            duration = route.duration,
            distance = route.distance,
            line = route.line,
            attribution = route.attribution,
        )
        return ReminderBubble(question = question, day = MR.strings.drive_feedback_yesterday_day.desc())
    }

    private fun ratingState(rating: Rating? = null, destinationName: String? = "Karlštejn Castle"): TripPlanViewState {
        val route = FakeRoutePlanner.Recorded.funRoutes.first()
        val question = DriveQuestion(
            driveId = "drive",
            startedAt = Instant.parse("2026-11-16T13:20:00Z"),
            routeNumber = 1,
            start = route.start.location,
            destination = route.destination.copy(name = destinationName),
            duration = route.duration,
            distance = route.distance,
            line = route.line,
            attribution = route.attribution,
        )
        return TripPlanViewState(
            sheetHeight = SheetHeight.Half,
            sheetStep = RatingStep(question = question, driveTime = "Today, 14:20".desc(), rating = rating),
        )
    }

    private fun noFunRoute(budget: Budget, closest: Budget?) = TripPlanViewState(
        planning = RoutePlanningState(destination = dresden, budget = budget).withPlan(
            FakeRoutePlanner.Recorded.copy(funRoutes = emptyList(), closestFunRouteBudget = closest),
        ),
    )

    // The selected route starts at [start]; the user is at [from]
    private fun stateStartingAt(start: GeoPoint, from: GeoPoint?): TripPlanViewState {
        val recorded = FakeRoutePlanner.Recorded
        val movedStart = Place(name = null, area = null, location = start)
        val plan = recorded.copy(funRoutes = recorded.funRoutes.map { it.copy(start = movedStart) })
        return TripPlanViewState(
            map = RouteMapState(userLocation = from),
            planning = RoutePlanningState(destination = dresden, budget = Budget.Time(30.minutes)).withPlan(plan),
        )
    }

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

    @Test
    fun `the saved routes row asks to sign in while signed out`() {
        assertEquals(MR.strings.sign_in_sheet_title_saved_routes.desc(), TripPlanViewState(isSignedIn = false).savedRoutesRowText)
    }

    @Test
    fun `the saved routes row says to save routes when signed in without any`() {
        assertEquals(MR.strings.map_saved_routes_none.desc(), TripPlanViewState(isSignedIn = true).savedRoutesRowText)
    }

    @Test
    fun `the saved routes row counts one saved route`() {
        val state = TripPlanViewState(isSignedIn = true, savedRoutes = listOf(savedRoute).toImmutableList())

        assertEquals(MR.strings.map_saved_routes_one.desc(), state.savedRoutesRowText)
    }

    @Test
    fun `the saved routes row counts several saved routes`() {
        val state =
            TripPlanViewState(
                isSignedIn = true,
                savedRoutes = listOf(savedRoute, savedRoute.copy(id = SavedRouteId("saved-2"))).toImmutableList(),
            )

        assertEquals(StringDesc.ResourceFormatted(MR.strings.map_saved_routes_many, 2), state.savedRoutesRowText)
    }

    @Test
    fun `the saved routes row shows in the search step while the field is blank`() {
        assertTrue(TripPlanViewState().showsSavedRoutesRow)
    }

    @Test
    fun `the saved routes row hides while typing and in the start step`() {
        assertFalse(TripPlanViewState(search = SearchState(text = "Kar")).showsSavedRoutesRow)
        assertFalse(
            TripPlanViewState(startSearch = StartSearch(destinationSearch = SearchState(), closesPlanning = false)).showsSavedRoutesRow,
        )
    }

    @Test
    fun `a saved route's step draws its line as route 1 without a label`() {
        val line = savedRouteState().routeLines.single()

        assertEquals(1, line.number)
        assertNull(line.label)
        assertTrue(line.isSelected)
        assertEquals(savedRouteRoute.line, line.points.toList())
    }

    @Test
    fun `a saved route's step draws no line while the route loads`() {
        assertTrue(savedRouteState(route = null, status = OneRouteStatus.Loading).routeLines.isEmpty())
    }

    @Test
    fun `a saved route's step frames its line or its start and destination while loading`() {
        val ready = assertNotNull(savedRouteState().routeBounds)
        assertTrue(ready.holds(savedRoute.route.start.location))
        assertTrue(ready.holds(savedRoute.route.destination.location))

        val loading = assertNotNull(savedRouteState(route = null, status = OneRouteStatus.Loading).routeBounds)
        assertTrue(loading.holds(savedRoute.route.start.location))
        assertTrue(loading.holds(savedRoute.route.destination.location))
    }

    @Test
    fun `a saved route's step hides the map buttons`() {
        assertFalse(savedRouteState().showsMapButtons)
    }

    @Test
    fun `drive is enabled once a saved route's route is loaded`() {
        assertEquals(savedRouteRoute, savedRouteState().driveRoute)
        assertTrue(savedRouteState().isDriveEnabled)
        assertFalse(savedRouteState(route = null, status = OneRouteStatus.Loading).isDriveEnabled)
    }

    @Test
    fun `a saved route's row is route 1 without an extra`() {
        val row = assertNotNull(savedRouteState().oneRouteRow)

        assertEquals(1, row.number)
        assertNull(row.extra)
        assertTrue(row.isSelected)
    }

    @Test
    fun `a saved route's start distance counts from the route's start`() {
        val farAway =
            GeoPoint(latitude = savedRoute.route.start.location.latitude + 0.1, longitude = savedRoute.route.start.location.longitude)
        val state = savedRouteState().copy(
            locationAccess = LocationAccess.Precise,
            map = RouteMapState(userLocation = farAway),
        )

        assertNotNull(state.startDistance)
    }

    private val savedRouteRoute = FakeRoutePlanner.Recorded.funRoutes.first()

    private val savedRoute = FakeSavedRoutesSource.Recorded

    private fun GeoBounds.holds(point: GeoPoint): Boolean =
        point.latitude in southWest.latitude..northEast.latitude && point.longitude in southWest.longitude..northEast.longitude

    private fun savedRouteState(
        route: Route? = savedRouteRoute,
        status: OneRouteStatus = OneRouteStatus.Ready,
    ) = TripPlanViewState(
        sheetStep = OneRouteStep(origin = OneRouteOrigin.Saved(savedRoute), route = route, status = status),
        sheetHeight = SheetHeight.Half,
    )

    private val pastRoute = FakeRoutePlanner.Recorded.funRoutes.first()

    private val pastDrive = PastDrive(
        id = "drive-1",
        startedAt = Instant.parse("2026-11-05T13:20:00Z"),
        routeId = pastRoute.id,
        kind = RouteKind.Fun,
        start = Place(name = null, area = "Smíchov, Prague", location = GeoPoint(latitude = 50.07, longitude = 14.40)),
        destination = Place(name = "Karlštejn Castle", area = "Beroun", location = pastRoute.destination.location),
        duration = pastRoute.duration,
        distance = pastRoute.distance,
        funLevel = pastRoute.funLevel,
    )

    private fun pastDriveState(
        drive: PastDrive = pastDrive,
        route: Route? = pastRoute,
        savedRoutes: List<SavedRoute> = emptyList(),
    ) = TripPlanViewState(
        sheetStep = OneRouteStep(origin = OneRouteOrigin.Past(drive), route = route),
        sheetHeight = SheetHeight.Half,
        savedRoutes = savedRoutes.toImmutableList(),
    )

    @Test
    fun `a past drive's header is its destination's name`() {
        assertEquals("Karlštejn Castle", pastDriveState().oneRouteTitle)
    }

    @Test
    fun `a past drive without a destination name is headed by its area`() {
        val unnamed = pastDrive.copy(destination = Place(name = null, area = "49.9, 14.2", location = pastRoute.destination.location))

        assertEquals("49.9, 14.2", pastDriveState(drive = unnamed).oneRouteTitle)
    }

    @Test
    fun `a past drive's route is its drive route once loaded`() {
        assertEquals(pastRoute, pastDriveState().driveRoute)
        assertTrue(pastDriveState().isDriveEnabled)
        assertNull(pastDriveState(route = null).driveRoute)
    }

    @Test
    fun `a past drive is saved while a saved route has its route ID`() {
        val saved = FakeSavedRoutesSource.Recorded.copy(route = FakeSavedRoutesSource.Recorded.route.copy(id = pastRoute.id))

        assertTrue(pastDriveState(savedRoutes = listOf(saved)).isSelectedRouteSaved)
        assertEquals(saved, pastDriveState(savedRoutes = listOf(saved)).savedRouteOnScreen)
    }

    @Test
    fun `a past drive is unsaved when no saved route has its route ID`() {
        val other = FakeSavedRoutesSource.Recorded.copy(route = FakeSavedRoutesSource.Recorded.route.copy(id = "other-route"))

        assertFalse(pastDriveState(savedRoutes = listOf(other)).isSelectedRouteSaved)
        assertFalse(pastDriveState().isSelectedRouteSaved)
    }

    @Test
    fun `a past drive's route is on screen by its route ID`() {
        assertEquals(pastRoute, pastDriveState().routeOnScreen(pastRoute.id))
        assertNull(pastDriveState().routeOnScreen("other-route"))
    }
}
