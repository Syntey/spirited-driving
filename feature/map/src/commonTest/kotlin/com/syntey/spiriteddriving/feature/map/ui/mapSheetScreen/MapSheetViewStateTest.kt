package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.core.domain.coverage.FakeCoverageSource
import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.geo.GeoBounds
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
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
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.desc.StringDesc
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.routePlanning.RoutePlanningViewState
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class MapSheetViewStateTest {

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
            it.copy(baseMap = it.baseMap.copy(userLocation = dresden, coveredArea = FakeCoverageSource.Czechia.area))
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
        val state = MapSheetViewState(
            locationAccess = LocationAccess.Precise,
            baseMap = RouteMapState(userLocation = origin),
            search = SearchState(recentPlaces = listOf(karlstejn.place).toImmutableList()),
        )

        assertEquals(km("4.2"), state.recentRows.single().distance)
    }

    @Test
    fun `a recent row outside a loaded covered area is not covered`() {
        val state = MapSheetViewState(
            baseMap = RouteMapState(coveredArea = FakeCoverageSource.Czechia.area),
            search = SearchState(recentPlaces = listOf(dresden).toImmutableList()),
        )

        assertFalse(state.recentRows.single().isCovered)
    }

    @Test
    fun `a recent row is covered while the covered area isn't loaded`() {
        val state = MapSheetViewState(search = SearchState(recentPlaces = listOf(dresden).toImmutableList()))

        assertTrue(state.recentRows.single().isCovered)
    }

    @Test
    fun `a precise location in the covered area is usable`() {
        val state = MapSheetViewState(locationAccess = LocationAccess.Precise, baseMap = RouteMapState(userLocation = origin))

        assertTrue(state.hasUsableLocation)
    }

    @Test
    fun `a precise location is usable before it is known`() {
        assertTrue(MapSheetViewState(locationAccess = LocationAccess.Precise).hasUsableLocation)
    }

    @Test
    fun `an approximate location isn't usable`() {
        val state = MapSheetViewState(locationAccess = LocationAccess.Approximate, baseMap = RouteMapState(userLocation = origin))

        assertFalse(state.hasUsableLocation)
    }

    @Test
    fun `a location without access isn't usable`() {
        val state = MapSheetViewState(locationAccess = LocationAccess.Denied, baseMap = RouteMapState(userLocation = origin))

        assertFalse(state.hasUsableLocation)
    }

    @Test
    fun `a precise location outside a loaded covered area isn't usable`() {
        val state = MapSheetViewState(
            locationAccess = LocationAccess.Precise,
            baseMap = RouteMapState(userLocation = dresdenPoint, coveredArea = FakeCoverageSource.Czechia.area),
        )

        assertFalse(state.hasUsableLocation)
    }

    @Test
    fun `the my location row is absent without the start step`() {
        assertNull(MapSheetViewState(locationAccess = LocationAccess.Precise, baseMap = RouteMapState(userLocation = origin)).myLocationRow)
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
            it.copy(baseMap = it.baseMap.copy(coveredArea = FakeCoverageSource.Czechia.area))
        }

        assertEquals(MyLocationRow.OutsideCoveredArea, state.myLocationRow)
    }

    @Test
    fun `the my location row is available with precise access in the covered area`() {
        val state = startStep(access = LocationAccess.Precise, from = origin).let {
            it.copy(baseMap = it.baseMap.copy(coveredArea = FakeCoverageSource.Czechia.area))
        }

        assertEquals(MyLocationRow.Available, state.myLocationRow)
    }

    @Test
    fun `planning from a start picked by hand frames the start and the destination and not the user's location`() {
        val start = GeoPoint(latitude = 49.0, longitude = 12.0)
        val state = MapSheetViewState(
            locationAccess = LocationAccess.Precise,
            baseMap = RouteMapState(userLocation = origin),
            sheetStep = RoutePlanningViewState(
                planning = RoutePlanningState(
                    destination = dresden,
                    start = Place(name = null, area = null, location = start),
                    budget = Budget.Time(30.minutes),
                ),
                budgetChoice = choice(kind = BudgetKind.Time),
                distanceUnit = DistanceUnit.Kilometres,
                userLocation = origin,
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
        val state = planning(Budget.Time(30.minutes)).let { it.copy(baseMap = RouteMapState(userLocation = origin)) }

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
        val sheetStep = RoutePlanningViewState(
            planning = planning,
            budgetChoice = choice(kind = BudgetKind.Time),
            distanceUnit = DistanceUnit.Kilometres,
        )
        assertFalse(reminderState().copy(sheetStep = sheetStep).showsReminder)
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

    private fun reminderState(destinationName: String? = "Karlštejn Castle") = MapSheetViewState(
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

    private fun ratingState(rating: Rating? = null, destinationName: String? = "Karlštejn Castle"): MapSheetViewState {
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
        return MapSheetViewState(
            sheetHeight = SheetHeight.Half,
            sheetStep = RatingStep(question = question, driveTime = "Today, 14:20".desc(), rating = rating),
        )
    }

    // The selected route starts at [start]; the user is at [from]
    private fun stateStartingAt(start: GeoPoint, from: GeoPoint?): MapSheetViewState {
        val recorded = FakeRoutePlanner.Recorded
        val movedStart = Place(name = null, area = null, location = start)
        val plan = recorded.copy(funRoutes = recorded.funRoutes.map { it.copy(start = movedStart) })
        return MapSheetViewState(
            baseMap = RouteMapState(userLocation = from),
            sheetStep = RoutePlanningViewState(
                planning = RoutePlanningState(destination = dresden, budget = Budget.Time(30.minutes)).withPlan(plan),
                budgetChoice = choice(kind = BudgetKind.Time),
                distanceUnit = DistanceUnit.Kilometres,
                userLocation = from,
            ),
        )
    }

    private fun planning(budget: Budget) = MapSheetViewState(
        sheetStep = RoutePlanningViewState(
            planning = RoutePlanningState(destination = dresden, budget = budget),
            budgetChoice = choice(kind = BudgetKind.Time),
            distanceUnit = DistanceUnit.Kilometres,
        ),
    )

    private fun choice(kind: BudgetKind) = BudgetChoice(
        time = Budget.Time(30.minutes),
        distance = Budget.Distance(30.kilometres),
        kind = kind,
    )

    private val dresden = Place(name = "Dresden", area = null, location = GeoPoint(latitude = 51.05, longitude = 13.74))

    private val origin = GeoPoint(latitude = 50.0, longitude = 14.0)

    private val dresdenPoint = GeoPoint(latitude = 51.05, longitude = 13.74)

    private fun startStep(access: LocationAccess?, from: GeoPoint?, text: String = "") = MapSheetViewState(
        locationAccess = access,
        baseMap = RouteMapState(userLocation = from),
        search = SearchState(text = text),
        startSearch = StartSearch(closesPlanning = false),
    )

    // 4.2 km north of origin
    private val karlstejn = suggestion(name = "Karlštejn", location = GeoPoint(latitude = 50.03777151, longitude = 14.0))

    private val prague = suggestion(name = "Karlštejnská", location = GeoPoint(latitude = 50.0755, longitude = 14.4378))

    private fun stateWithSearch(access: LocationAccess?, from: GeoPoint?, unit: DistanceUnit = DistanceUnit.Kilometres) =
        MapSheetViewState(
            locationAccess = access,
            baseMap = RouteMapState(userLocation = from),
            search = SearchState(text = "karlst", suggestions = listOf(karlstejn).toImmutableList()),
            distanceUnit = unit,
        )

    private fun suggestion(name: String, location: GeoPoint) =
        PlaceSuggestion(
            place = Place(name = name, area = null, location = location),
            isCovered = true,
            kind = PlaceKind.Town,
        )

    private fun approximate(isPreciseLocationNoteClosed: Boolean) = MapSheetViewState(
        locationAccess = LocationAccess.Approximate,
        isPreciseLocationNoteClosed = isPreciseLocationNoteClosed,
    )

    private fun km(number: String) = StringDesc.ResourceFormatted(MR.strings.unit_kilometres, number)

    private fun mi(number: String) = StringDesc.ResourceFormatted(MR.strings.unit_miles, number)

    @Test
    fun `the saved routes row asks to sign in while signed out`() {
        assertEquals(MR.strings.sign_in_sheet_title_saved_routes.desc(), MapSheetViewState(isSignedIn = false).savedRoutesRowText)
    }

    @Test
    fun `the saved routes row says to save routes when signed in without any`() {
        assertEquals(MR.strings.map_saved_routes_none.desc(), MapSheetViewState(isSignedIn = true).savedRoutesRowText)
    }

    @Test
    fun `the saved routes row counts one saved route`() {
        val state = MapSheetViewState(isSignedIn = true, savedRoutes = listOf(savedRoute).toImmutableList())

        assertEquals(MR.strings.map_saved_routes_one.desc(), state.savedRoutesRowText)
    }

    @Test
    fun `the saved routes row counts several saved routes`() {
        val state =
            MapSheetViewState(
                isSignedIn = true,
                savedRoutes = listOf(savedRoute, savedRoute.copy(id = SavedRouteId("saved-2"))).toImmutableList(),
            )

        assertEquals(StringDesc.ResourceFormatted(MR.strings.map_saved_routes_many, 2), state.savedRoutesRowText)
    }

    @Test
    fun `the saved routes row shows in the search step while the field is blank`() {
        assertTrue(MapSheetViewState().showsSavedRoutesRow)
    }

    @Test
    fun `the saved routes row hides while typing and in the start step`() {
        assertFalse(MapSheetViewState(search = SearchState(text = "Kar")).showsSavedRoutesRow)
        assertFalse(
            MapSheetViewState(startSearch = StartSearch(closesPlanning = false)).showsSavedRoutesRow,
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
    fun `a saved route's start distance counts from the route's start`() {
        val farAway =
            GeoPoint(latitude = savedRoute.route.start.location.latitude + 0.1, longitude = savedRoute.route.start.location.longitude)
        val state = savedRouteState().copy(
            locationAccess = LocationAccess.Precise,
            baseMap = RouteMapState(userLocation = farAway),
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
    ) = MapSheetViewState(
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
    ) = MapSheetViewState(
        sheetStep = OneRouteStep(origin = OneRouteOrigin.Past(drive), route = route),
        sheetHeight = SheetHeight.Half,
        savedRoutes = savedRoutes.toImmutableList(),
    )

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
