package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import com.syntey.spiriteddriving.core.domain.coverage.FakeCoverageSource
import com.syntey.spiriteddriving.core.domain.drive.FakeDriveRater
import com.syntey.spiriteddriving.core.domain.drive.FakeDriveRecorder
import com.syntey.spiriteddriving.core.domain.drive.FakeOpenDriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.FakeStartedDrives
import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistoryCache
import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistorySource
import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.drive.PastDrivePage
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.FakePlaceSearch
import com.syntey.spiriteddriving.core.domain.place.FakeRecentPlaces
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.PlaceSearch
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.core.domain.route.TripPlan
import com.syntey.spiriteddriving.core.domain.route.TripRequest
import com.syntey.spiriteddriving.core.domain.route.RoutePlanner
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesCache
import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.domain.settings.FakeDistanceUnitSettings
import com.syntey.spiriteddriving.core.domain.settings.FakeVignetteSettings
import com.syntey.spiriteddriving.core.domain.settings.Note
import com.syntey.spiriteddriving.core.platform.binding.Auth
import com.syntey.spiriteddriving.core.platform.binding.AuthUser
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.platform.binding.LocationFix
import com.syntey.spiriteddriving.core.platform.binding.LocationPermission
import com.syntey.spiriteddriving.core.platform.binding.MapsApp
import com.syntey.spiriteddriving.core.platform.binding.MapsAppKind
import com.syntey.spiriteddriving.core.platform.binding.MapsAppResult
import com.syntey.spiriteddriving.core.platform.binding.SignInProvider
import com.syntey.spiriteddriving.core.platform.binding.UserLocation
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.testing.TestComponentContext
import com.syntey.spiriteddriving.core.domain.work.AppWork
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import com.syntey.spiriteddriving.feature.map.domain.AddRecentPlaceUseCase
import com.syntey.spiriteddriving.feature.map.domain.CloseNoteUseCase
import com.syntey.spiriteddriving.feature.map.domain.DriveSender
import com.syntey.spiriteddriving.feature.map.domain.FakeBudgetSettings
import com.syntey.spiriteddriving.feature.map.domain.FakePlatform
import com.syntey.spiriteddriving.feature.map.domain.FakePlatformAuth
import com.syntey.spiriteddriving.feature.map.domain.FakePlatformDriveReminders
import com.syntey.spiriteddriving.feature.map.domain.FakePlatformLocation
import com.syntey.spiriteddriving.feature.map.domain.FakePlatformLocationPermission
import com.syntey.spiriteddriving.feature.map.domain.FakePlatformMapsApp
import com.syntey.spiriteddriving.feature.map.domain.GetBudgetChoiceUseCase
import com.syntey.spiriteddriving.feature.map.domain.GetDriveQuestionUseCase
import com.syntey.spiriteddriving.feature.map.domain.GetLocationAccessUseCase
import com.syntey.spiriteddriving.feature.map.domain.GetPastDriveUseCase
import com.syntey.spiriteddriving.feature.map.domain.GetRecentPlacesUseCase
import com.syntey.spiriteddriving.feature.map.domain.HandOffUseCase
import com.syntey.spiriteddriving.feature.map.domain.InMemoryCoverageCache
import com.syntey.spiriteddriving.feature.map.domain.IsNoteClosedUseCase
import com.syntey.spiriteddriving.feature.map.domain.LoadPastDriveRouteUseCase
import com.syntey.spiriteddriving.feature.map.domain.LoadSavedRouteUseCase
import com.syntey.spiriteddriving.feature.map.domain.MarkNotDrivenUseCase
import com.syntey.spiriteddriving.feature.map.domain.NamePointUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveCoverageUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveDistanceUnitUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveOpenedReminderUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveSavedRoutesUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveSignedInUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveUserLocationUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveVignettesUseCase
import com.syntey.spiriteddriving.feature.map.domain.OpenLocationSettingsUseCase
import com.syntey.spiriteddriving.feature.map.domain.OpenMapsAppStoreUseCase
import com.syntey.spiriteddriving.feature.map.domain.PlanTripUseCase
import com.syntey.spiriteddriving.feature.map.domain.RateDriveUseCase
import com.syntey.spiriteddriving.feature.map.domain.RecordingClosedNotes
import com.syntey.spiriteddriving.feature.map.domain.RemoveRecentPlaceUseCase
import com.syntey.spiriteddriving.feature.map.domain.RequestLocationAccessUseCase
import com.syntey.spiriteddriving.feature.map.domain.RequestPreciseLocationUseCase
import com.syntey.spiriteddriving.feature.map.domain.SaveBudgetChoiceUseCase
import com.syntey.spiriteddriving.feature.map.domain.SaveRouteUseCase
import com.syntey.spiriteddriving.feature.map.domain.SearchPlacesUseCase
import com.syntey.spiriteddriving.feature.map.domain.SendPendingDrivesUseCase
import com.syntey.spiriteddriving.feature.map.domain.ShouldOfferReminderUseCase
import com.syntey.spiriteddriving.feature.map.domain.SkipDriveQuestionUseCase
import com.syntey.spiriteddriving.feature.map.domain.StartDriveUseCase
import com.syntey.spiriteddriving.feature.map.domain.TurnOnRemindersUseCase
import com.syntey.spiriteddriving.feature.map.domain.UnsaveRouteUseCase
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * The map sheet screen built from real use cases over fakes, started and on screen. Tests act on [screen] and read its
 * view state; the other fields are the fakes a test scripts or checks.
 */
internal class MapSheetFixture(
    val screen: MapSheetScreen,
    val navigation: RecordingMapSheetNavigation,
    val lifecycle: TestComponentContext,
    /** The one-time events the screen sent, in order, from the start of the test. */
    val events: MutableList<MapSheetUiEvent>,
    val placeSearch: SwitchablePlaceSearch,
    val routePlanner: SwitchableRoutePlanner,
    val vignettes: FakeVignetteSettings,
    val distanceUnits: FakeDistanceUnitSettings,
    val location: FakePlatformLocation,
    val locationPermission: FakePlatformLocationPermission,
    /** The drive question the screen is asked about on start, as the drive recorder left it. */
    val openDriveQuestion: FakeOpenDriveQuestion,
    /** The platform's drive notifications: a test taps one with [FakePlatformDriveReminders.tap]. */
    val driveReminders: FakePlatformDriveReminders,
    val auth: FakePlatformAuth,
    val savedRoutesSource: FakeSavedRoutesSource,
)

/**
 * Builds a [MapSheetComponent] with [recentPlaces] kept and the user's location access at [locationAccess]. Covered area is
 * Czechia. Nothing is loaded yet: advance the test's time before reading the view state.
 */
internal fun TestScope.mapSheetFixture(
    recentPlaces: List<Place> = emptyList(),
    locationAccess: LocationAccess = LocationAccess.Denied,
    signedIn: Boolean = false,
    savedRoutes: List<SavedRoute> = emptyList(),
    pastDrives: List<PastDrive> = emptyList(),
    initiallyClosed: Set<Note> = emptySet(),
    mapsAppResult: MapsAppResult = MapsAppResult.Opened,
    driveQuestion: DriveQuestion? = null,
    canOfferReminders: Boolean = false,
): MapSheetFixture {
    val lifecycle = TestComponentContext(useCaseDispatcher = StandardTestDispatcher(testScheduler))
    val navigation = RecordingMapSheetNavigation()
    val placeSearch = SwitchablePlaceSearch(FakePlaceSearch())
    val recents = FakeRecentPlaces(recentPlaces)
    val location = FakePlatformLocation()
    val locationPermission = FakePlatformLocationPermission(locationAccess)
    val permission = LocationPermission(locationPermission)
    val closedNotes = RecordingClosedNotes(initiallyClosed)
    val vignettes = FakeVignetteSettings()
    val routePlanner = SwitchableRoutePlanner(FakeRoutePlanner())
    val coverageCache = InMemoryCoverageCache()
    val budgetSettings = FakeBudgetSettings(choice = null)
    val distanceUnits = FakeDistanceUnitSettings(DistanceUnit.Kilometres)
    val platform = FakePlatform()
    val mapsApp = MapsApp(FakePlatformMapsApp(MapsAppKind.GoogleMaps, mapsAppResult))
    val platformReminders = FakePlatformDriveReminders(canOffer = canOfferReminders)
    val reminders = DriveReminders(platformReminders)
    val platformAuth = FakePlatformAuth(initialUser = if (signedIn) TEST_USER else null)
    val auth = Auth(platformAuth)
    val openDriveQuestion = FakeOpenDriveQuestion(driveQuestion)
    val startedDrives = FakeStartedDrives()
    val driveRecorder = FakeDriveRecorder()
    val driveSender = DriveSender(startedDrives, driveRecorder, FakeDriveRater())
    val tripHistorySource = FakeTripHistorySource(pastDrives)
    val tripHistoryCache = FakeTripHistoryCache(PastDrivePage(pastDrives, nextCursor = null))
    val savedRoutesSource = FakeSavedRoutesSource(savedRoutes)
    val savedRoutesCache = FakeSavedRoutesCache(savedRoutes)
    // Runs what's launched on the test's time, as the app's runs it past the screen's close
    val appWork = object : AppWork {
        override fun launch(block: suspend () -> Unit) {
            backgroundScope.launch { block() }
        }
    }

    // The child components are built by their generated factories, which resolve from the global Koin, so Koin is started with
    // the same fakes the parent gets
    startKoin {
        modules(
            module {
                factory { (context: AppComponentContext, listener: DriveHandOffComponent.Listener) ->
                    DriveHandOffComponent(
                        componentContext = context,
                        listener = listener,
                        handOffUseCase = HandOffUseCase(mapsApp),
                        startDriveUseCase = StartDriveUseCase(startedDrives, driveRecorder, openDriveQuestion, tripHistoryCache, reminders),
                        openMapsAppStoreUseCase = OpenMapsAppStoreUseCase(mapsApp),
                        isNoteClosedUseCase = IsNoteClosedUseCase(closedNotes),
                        closeNoteUseCase = CloseNoteUseCase(closedNotes),
                    )
                }
                factory { (context: AppComponentContext, host: MapSheetHost, args: RoutePlanningComponent.Args) ->
                    RoutePlanningComponent(
                        componentContext = context,
                        host = host,
                        args = args,
                        planTripUseCase = PlanTripUseCase(routePlanner, vignettes),
                        getBudgetChoiceUseCase = GetBudgetChoiceUseCase(budgetSettings),
                        saveBudgetChoiceUseCase = SaveBudgetChoiceUseCase(budgetSettings),
                        observeDistanceUnitUseCase = ObserveDistanceUnitUseCase(distanceUnits, platform),
                        observeVignettesUseCase = ObserveVignettesUseCase(vignettes),
                        namePointUseCase = NamePointUseCase(placeSearch),
                    )
                }
                factory { (context: AppComponentContext, host: MapSheetHost, args: PlaceSearchComponent.Args) ->
                    PlaceSearchComponent(
                        componentContext = context,
                        host = host,
                        args = args,
                        searchPlacesUseCase = SearchPlacesUseCase(placeSearch),
                        getRecentPlacesUseCase = GetRecentPlacesUseCase(recents),
                        addRecentPlaceUseCase = AddRecentPlaceUseCase(recents),
                        removeRecentPlaceUseCase = RemoveRecentPlaceUseCase(recents),
                    )
                }
                factory { (context: AppComponentContext, host: MapSheetHost, args: OneRouteComponent.Args) ->
                    OneRouteComponent(
                        componentContext = context,
                        host = host,
                        args = args,
                        loadSavedRouteUseCase = LoadSavedRouteUseCase(savedRoutesSource, savedRoutesCache),
                        loadPastDriveRouteUseCase = LoadPastDriveRouteUseCase(tripHistorySource, tripHistoryCache),
                    )
                }
                factory { (context: AppComponentContext, host: MapSheetHost, args: RatingComponent.Args) ->
                    RatingComponent(
                        componentContext = context,
                        host = host,
                        args = args,
                        appWork = appWork,
                        rateDriveUseCase = RateDriveUseCase(openDriveQuestion, startedDrives, driveSender, reminders),
                        skipDriveQuestionUseCase = SkipDriveQuestionUseCase(openDriveQuestion, reminders),
                        markNotDrivenUseCase = MarkNotDrivenUseCase(openDriveQuestion, startedDrives, driveSender, reminders),
                        shouldOfferReminderUseCase = ShouldOfferReminderUseCase(closedNotes, reminders),
                        turnOnRemindersUseCase = TurnOnRemindersUseCase(reminders),
                    )
                }
                factory { (context: AppComponentContext, listener: RouteSavingComponent.Listener) ->
                    RouteSavingComponent(
                        componentContext = context,
                        listener = listener,
                        observeSignedInUseCase = ObserveSignedInUseCase(auth),
                        observeSavedRoutesUseCase = ObserveSavedRoutesUseCase(savedRoutesCache),
                        saveRouteUseCase = SaveRouteUseCase(savedRoutesSource, savedRoutesCache),
                        unsaveRouteUseCase = UnsaveRouteUseCase(savedRoutesSource, savedRoutesCache),
                    )
                }
            },
        )
    }

    val component = MapSheetComponent(
        componentContext = lifecycle.context,
        navigation = navigation,
        getLocationAccessUseCase = GetLocationAccessUseCase(permission),
        requestLocationAccessUseCase = RequestLocationAccessUseCase(permission),
        openLocationSettingsUseCase = OpenLocationSettingsUseCase(permission),
        observeUserLocationUseCase = ObserveUserLocationUseCase(UserLocation(location)),
        observeCoverageUseCase = ObserveCoverageUseCase(FakeCoverageSource(), coverageCache),
        requestPreciseLocationUseCase = RequestPreciseLocationUseCase(permission),
        isNoteClosedUseCase = IsNoteClosedUseCase(closedNotes),
        closeNoteUseCase = CloseNoteUseCase(closedNotes),
        observeDistanceUnitUseCase = ObserveDistanceUnitUseCase(distanceUnits, platform),
        sendPendingDrivesUseCase = SendPendingDrivesUseCase(startedDrives, driveSender),
        getDriveQuestionUseCase = GetDriveQuestionUseCase(openDriveQuestion, reminders),
        skipDriveQuestionUseCase = SkipDriveQuestionUseCase(openDriveQuestion, reminders),
        observeOpenedReminderUseCase = ObserveOpenedReminderUseCase(reminders, openDriveQuestion),
        getPastDriveUseCase = GetPastDriveUseCase(tripHistoryCache),
    )

    // Unconfined: a background coroutine doesn't run under advanceUntilIdle, so the event is taken as soon as it's sent
    val events = mutableListOf<MapSheetUiEvent>()
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { component.events.collect { events += it } }
    // Destroyed when the test ends, as backgroundScope is cancelled then
    backgroundScope.launch {
        try {
            awaitCancellation()
        } finally {
            lifecycle.destroy()
            stopKoin()
        }
    }

    return MapSheetFixture(
        screen = component,
        navigation = navigation,
        lifecycle = lifecycle,
        events = events,
        placeSearch = placeSearch,
        routePlanner = routePlanner,
        vignettes = vignettes,
        distanceUnits = distanceUnits,
        location = location,
        locationPermission = locationPermission,
        openDriveQuestion = openDriveQuestion,
        driveReminders = platformReminders,
        auth = platformAuth,
        savedRoutesSource = savedRoutesSource,
    )
}

/** Place search that answers like [answers], or throws [failure] instead while it's set. Records the texts it was asked for. */
internal class SwitchablePlaceSearch(private val answers: PlaceSearch) : PlaceSearch {

    var failure: Throwable? = null

    val searchedTexts = mutableListOf<String>()

    override suspend fun search(text: String, near: GeoPoint?): List<PlaceSuggestion> {
        searchedTexts += text
        failure?.let { throw it }
        return answers.search(text, near)
    }

    // The backend names a point with the point as the place's location, so a name lands on the point it was asked for
    override suspend fun placeAt(point: GeoPoint): Place? = answers.placeAt(point)?.copy(location = point)
}

/** Route planner that answers with [plan] while it's set, and throws [failure] instead while it's set. */
internal class SwitchableRoutePlanner(private val answers: RoutePlanner) : RoutePlanner {

    var failure: Throwable? = null

    var plan: TripPlan? = null

    override suspend fun plan(request: TripRequest): TripPlan {
        failure?.let { throw it }
        return plan ?: answers.plan(request)
    }
}

/** The signed-in user when a test starts signed in. */
internal val TEST_USER = AuthUser(id = "user-1", name = null, email = null, provider = SignInProvider.Google)

/** Points in the test area: [prague] is the user's location, [pinned] is inside the covered area, [outside] is Dresden. */
internal object Points {
    val prague = GeoPoint(latitude = 50.0755, longitude = 14.4378)
    val pinned = GeoPoint(latitude = 49.9394, longitude = 14.1882)
    val outside = GeoPoint(latitude = 51.0504, longitude = 13.7373)
}

/** Gives the user's location a fix at [point] and lets the screen react. */
internal fun TestScope.sendFix(trip: MapSheetFixture, point: GeoPoint) {
    trip.location.emit(LocationFix(latitude = point.latitude, longitude = point.longitude))
    advanceUntilIdle()
}

/** Types [text] in the destination search, lets the answer show, and returns the first covered suggestion. */
internal fun TestScope.searchForCovered(trip: MapSheetFixture, text: String = "karlst"): PlaceSuggestion {
    trip.screen.actions.onSearchTextChange(text)
    advanceUntilIdle()
    return firstCovered(trip)
}

/** The first covered suggestion on screen. */
internal fun firstCovered(trip: MapSheetFixture): PlaceSuggestion =
    trip.screen.viewState.value.suggestionRows.first { it.suggestion.isCovered }.suggestion

/** Stops the screen and starts it again, as when the app goes to the background and comes back: the drive question is asked again. */
internal fun TestScope.returnToScreen(trip: MapSheetFixture) {
    trip.lifecycle.stop()
    trip.lifecycle.start()
    advanceUntilIdle()
}

/**
 * The question of a drive on the first fun route, started [startedAgo] ago and planned for [duration]. Started [startedAgo] ago
 * puts the question in the rating step or the reminder bubble, as the planned time is what the screen counts from.
 */
internal fun TestScope.driveQuestion(startedAgo: Duration, duration: Duration = 20.minutes): DriveQuestion {
    val route = FakeRoutePlanner.Recorded.funRoutes.first()
    return DriveQuestion(
        driveId = "drive",
        startedAt = Clock.System.now() - startedAgo,
        routeNumber = 1,
        start = route.start.location,
        destination = route.destination,
        duration = duration,
        distance = route.distance,
        line = route.line,
        attribution = route.attribution,
    )
}
