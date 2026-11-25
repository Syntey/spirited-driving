package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import app.futured.arkitekt.decompose.ext.asStateFlow
import co.touchlab.kermit.Logger
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.lifecycle.doOnStart
import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import com.syntey.spiriteddriving.core.domain.settings.Note
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.platform.binding.MapsAppResult
import com.syntey.spiriteddriving.core.resources.datetime.driveDayText
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.ScreenComponent
import com.syntey.spiriteddriving.core.ui.base.executeWithLifecycle
import com.syntey.spiriteddriving.core.ui.base.derivedStateFlow
import com.syntey.spiriteddriving.feature.map.domain.CloseNoteUseCase
import com.syntey.spiriteddriving.feature.map.domain.DrivePrompt
import com.syntey.spiriteddriving.feature.map.domain.GetDriveQuestionUseCase
import com.syntey.spiriteddriving.feature.map.domain.GetLocationAccessUseCase
import com.syntey.spiriteddriving.feature.map.domain.GetPastDriveUseCase
import com.syntey.spiriteddriving.feature.map.domain.HandOffUseCase
import com.syntey.spiriteddriving.feature.map.domain.IsNoteClosedUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveCoverageUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveDistanceUnitUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveOpenedReminderUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveSavedRoutesUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveSignedInUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveUserLocationUseCase
import com.syntey.spiriteddriving.feature.map.domain.OpenLocationSettingsUseCase
import com.syntey.spiriteddriving.feature.map.domain.OpenMapsAppStoreUseCase
import com.syntey.spiriteddriving.feature.map.domain.RequestLocationAccessUseCase
import com.syntey.spiriteddriving.feature.map.domain.RequestPreciseLocationUseCase
import com.syntey.spiriteddriving.feature.map.domain.SaveRouteResult
import com.syntey.spiriteddriving.feature.map.domain.SaveRouteUseCase
import com.syntey.spiriteddriving.feature.map.domain.SendPendingDrivesUseCase
import com.syntey.spiriteddriving.feature.map.domain.SkipDriveQuestionUseCase
import com.syntey.spiriteddriving.feature.map.domain.StartDriveUseCase
import com.syntey.spiriteddriving.feature.map.domain.UnsaveRouteUseCase
import com.syntey.spiriteddriving.feature.map.domain.unnamedPlace
import com.syntey.spiriteddriving.feature.map.model.ui.DriveDialog
import com.syntey.spiriteddriving.feature.map.model.ui.LocateButton
import com.syntey.spiriteddriving.feature.map.model.ui.MyLocationRow
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteOrigin
import com.syntey.spiriteddriving.feature.map.model.ui.ReminderBubble
import com.syntey.spiriteddriving.feature.map.model.ui.RouteNameDialog
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import com.syntey.spiriteddriving.feature.map.model.ui.allowsLocation
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.oneRoute.OneRouteComponent
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.oneRoute.OneRouteComponentFactory
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.placeSearch.PlaceSearchComponent
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.placeSearch.PlaceSearchComponentFactory
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.rating.RatingComponent
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.rating.RatingComponentFactory
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.routePlanning.RoutePlanningComponent
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.routePlanning.RoutePlanningComponentFactory
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import kotlin.time.Clock

@Factory
@GenerateFactory
// Each action of the sheet and each read on start is a function here, for the location, Drive, Save and the drive question
@Suppress("TooManyFunctions", "LargeClass")
internal class MapSheetComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam override val navigation: MapSheetScreenNavigation,
    private val observeDistanceUnitUseCase: ObserveDistanceUnitUseCase,
    private val sendPendingDrivesUseCase: SendPendingDrivesUseCase,
    private val getPastDriveUseCase: GetPastDriveUseCase,
    private val getLocationAccessUseCase: GetLocationAccessUseCase,
    private val requestLocationAccessUseCase: RequestLocationAccessUseCase,
    private val openLocationSettingsUseCase: OpenLocationSettingsUseCase,
    private val requestPreciseLocationUseCase: RequestPreciseLocationUseCase,
    private val observeUserLocationUseCase: ObserveUserLocationUseCase,
    private val observeCoverageUseCase: ObserveCoverageUseCase,
    private val isNoteClosedUseCase: IsNoteClosedUseCase,
    private val closeNoteUseCase: CloseNoteUseCase,
    private val handOffUseCase: HandOffUseCase,
    private val startDriveUseCase: StartDriveUseCase,
    private val openMapsAppStoreUseCase: OpenMapsAppStoreUseCase,
    private val observeSignedInUseCase: ObserveSignedInUseCase,
    private val observeSavedRoutesUseCase: ObserveSavedRoutesUseCase,
    private val saveRouteUseCase: SaveRouteUseCase,
    private val unsaveRouteUseCase: UnsaveRouteUseCase,
    private val getDriveQuestionUseCase: GetDriveQuestionUseCase,
    private val skipDriveQuestionUseCase: SkipDriveQuestionUseCase,
    private val observeOpenedReminderUseCase: ObserveOpenedReminderUseCase,
) : ScreenComponent<MapSheetState, MapSheetUiEvent, MapSheetScreenNavigation>(
    componentContext = componentContext,
    defaultState = MapSheetState(),
),
    MapSheetScreen,
    MapSheetScreenNavigation by navigation,
    MapSheetScreen.Actions {

    private val logger = Logger.withTag("MapSheetComponent")

    // What the sheet's steps reach on this parent
    private val host = object : MapSheetHost {
        override val userLocation: GeoPoint? get() = componentState.value.baseMap.userLocation
        override val sheetHeight: SheetHeight get() = componentState.value.sheetHeight
        override fun requestSheetHeight(height: SheetHeight) = setSheetHeight(height)
        override fun onStepClosed(sheetHeight: SheetHeight) = closeStep(sheetHeight)
        override fun covers(point: GeoPoint): Boolean = this@MapSheetComponent.covers(point)
        override fun onNotCovered() = sendUiEvent(MapSheetUiEvent.NotCovered)
        override fun onPlaceChosen(place: Place) = planTo(place)
        override fun keepRecentPlace(place: Place) = rootPlaceSearch().keep(place)
        override fun onRecentPlacesChanged(places: List<Place>) = placeSearches().forEach { it.showRecentPlaces(places) }
    }

    private val stepNavigation = StackNavigation<SheetStepConfig>()

    // The root is the destination search. The steps pushed over it keep it alive, with its text, suggestions and status unchanged
    private val stack: StateFlow<ChildStack<SheetStepConfig, SheetStepChild>> = childStack(
        source = stepNavigation,
        serializer = null,
        key = "sheetSteps",
        initialConfiguration = SheetStepConfig.DestinationSearch,
        handleBackButton = false,
        childFactory = { config, childCtx ->
            when (config) {
                SheetStepConfig.DestinationSearch -> SheetStepChild.PlaceSearch(
                    PlaceSearchComponentFactory.createComponent(childCtx, host, PlaceSearchComponent.Args(isStart = false)),
                )

                is SheetStepConfig.RoutePlanning -> SheetStepChild.RoutePlanning(
                    RoutePlanningComponentFactory.createComponent(
                        childCtx,
                        host,
                        RoutePlanningComponent.Args(config.destination, config.waitsForStart),
                    ),
                )

                is SheetStepConfig.OneRoute -> SheetStepChild.OneRoute(
                    OneRouteComponentFactory.createComponent(childCtx, host, OneRouteComponent.Args(config.origin)),
                )

                is SheetStepConfig.Rating -> SheetStepChild.Rating(
                    RatingComponentFactory.createComponent(childCtx, host, RatingComponent.Args(config.question)),
                )
            }
        },
    ).asStateFlow()

    // The drive that waits for the first-drive dialog's answer, with its number
    private var pendingDrive: PendingDrive? = null

    // The route Save was tapped on while signed out, saved once the sign-in sheet closes signed in
    private var pendingSaveRouteId: String? = null

    // Projected from the parent's own state and the step on top of the stack, which is followed as it changes
    @OptIn(ExperimentalCoroutinesApi::class)
    override val viewState: StateFlow<MapSheetViewState> = derivedStateFlow(
        changes = merge(
            componentState,
            stack.flatMapLatest { stack -> combine(stack.items.map { it.instance.state }) { } },
        ),
    ) { project() }

    // The kind of the step on top, which the sheet's content switches on
    override val activeStep: StateFlow<ActiveSheetStep> = derivedStateFlow(changes = stack) { stack.value.active.instance.activeStep }

    override val actions: MapSheetScreen.Actions = this

    // Back closes the search while the sheet is pulled up. The steps over it handle Back themselves while they're on top.
    private val backCallback = BackCallback(isEnabled = false) { closeSearch() }

    init {
        backHandler.register(backCallback)
        // On every start: access turned on in the system settings shows when the user comes back, and coming back from the
        // maps app asks about the drive
        doOnStart(isOneTime = false) {
            readLocationAccess()
            askAboutDrive()
        }
        loadCoverage()
        readPreciseLocationNote()
        readFirstDriveNote()
        observeSignedIn()
        observeSavedRoutes()
        observeOpenedReminder()
        observeDistanceUnit()
        sendPendingDrives()
    }

    // Observed, so a unit picked in Settings applies while the map is under the Profile sheet
    private fun observeDistanceUnit() = observeDistanceUnitUseCase.execute {
        onNext { unit -> componentState.update { it.copy(distanceUnit = unit) } }
        onError { error -> logger.e(error) { "Distance unit not observed" } }
    }

    override fun onSearchFocus() = activePlaceSearch().onFocus()

    override fun onSearchTextChange(text: String) = activePlaceSearch().onTextChange(text)

    override fun onSearchClear() = activePlaceSearch().clear()

    override fun onSearchRetry() = activePlaceSearch().onRetry()

    override fun onSearchClose() = closeSearchStep()

    // The start search has no collapsed layout, so dragging it down closes it
    override fun onSheetHeightChange(height: SheetHeight) {
        if (height == SheetHeight.Collapsed) routePlanningChild()?.closeStartSearch()
        setSheetHeight(height)
    }

    override fun onSuggestionClick(suggestion: PlaceSuggestion) = activePlaceSearch().onSuggestionClick(suggestion)

    override fun onRecentPlaceClick(place: Place) = activePlaceSearch().onRecentPlaceClick(place)

    override fun onRecentPlaceRemove(place: Place) = activePlaceSearch().onRecentPlaceRemove(place)

    // Kept with the recent places, then planned from the user's location or from a start to be picked
    private fun planTo(destination: Place) {
        rootPlaceSearch().keep(destination)
        frame(destination.location)
        startPlanning(destination)
    }

    // Replaces everything above the root, so the trip is the only step over the search. Without a usable location the trip waits
    // for a start: planning opens with its start search, and closing that closes planning
    private fun startPlanning(destination: Place) {
        val usable = viewState.value.hasUsableLocation
        replaceAboveSearch(SheetStepConfig.RoutePlanning(destination, waitsForStart = !usable))
        setSheetHeight(if (usable) SheetHeight.Half else SheetHeight.Full)
    }

    override fun onLocateClick() {
        val state = componentState.value
        when (LocateButton.of(state.locationAccess, state.baseMap.followsUser)) {
            LocateButton.Following -> Unit
            LocateButton.NotFollowing -> componentState.update { it.copy(baseMap = it.baseMap.copy(followsUser = true)) }
            LocateButton.NoAccess -> askForLocationAccess()
        }
    }

    override fun onMapPanned() = stopFollowing()

    // The rating step is about the drive just made, not a new trip, so a long press doesn't start one
    override fun onMapLongPress(point: GeoPoint) {
        if (!isRatingOpen()) pressMap(point)
    }

    private fun pressMap(point: GeoPoint) {
        if (!covers(point)) {
            sendUiEvent(MapSheetUiEvent.NotCovered)
            return
        }
        val planning = routePlanningChild()
        if (planning?.isStartSearchOpen == true) {
            planning.pickStart(point)
            return
        }
        stopFollowing()
        // Replaces a saved route or a past drive on screen, as the trip replaces everything above the root
        startPlanning(unnamedPlace(point))
        routePlanningChild()?.nameDestination(point)
    }

    override fun onRouteClick(routeId: String) {
        routePlanningChild()?.onRouteClick(routeId)
    }

    // A saved route's route is loaded in place of a plan; the start and destination pins frame it until it is
    override fun showSavedRoute(savedRouteId: SavedRouteId) {
        val savedRoute = viewState.value.savedRoutes.firstOrNull { it.id == savedRouteId } ?: return
        showOneRoute(OneRouteOrigin.Saved(savedRoute))
    }

    // Nothing shows when the drive isn't kept on the device, as the list it was chosen from is what keeps it
    override fun showPastDrive(driveId: String) = readPastDrive(driveId) { showOneRoute(OneRouteOrigin.Past(it)) }

    // Plans from the user's current location, and the rating step, a route on its own and the search make way for it
    override fun planNewRoutes(driveId: String) = readPastDrive(driveId) { planTripTo(it.destination) }

    private fun readPastDrive(driveId: String, onDrive: (PastDrive) -> Unit) {
        getPastDriveUseCase.execute(driveId) {
            onSuccess { drive -> drive?.let(onDrive) }
            onError { error -> logger.e(error) { "Past drive not read" } }
        }
    }

    // Replaces everything above the root search, which is cleared, so the route is the only step over it
    private fun showOneRoute(origin: OneRouteOrigin) {
        replaceAboveSearch(SheetStepConfig.OneRoute(origin))
        rootPlaceSearch().clear()
        stopFollowing()
        setSheetHeight(SheetHeight.Half)
    }

    private fun planTripTo(destination: Place) {
        closeOneRouteStep()
        rootPlaceSearch().clear()
        if (!covers(destination.location)) {
            sendUiEvent(MapSheetUiEvent.NotCovered)
            return
        }
        planTo(destination)
    }

    // Pops a saved route's or a past drive's step without collapsing the sheet, as planning takes its place
    private fun closeOneRouteStep() {
        if (oneRouteChild() != null) stepNavigation.pop()
    }

    override fun onSavedRoutesClick() {
        if (viewState.value.isSignedIn) openSavedRoutes() else openSignInForSavedRoutes()
    }

    override fun onProfileClick() = openProfile()

    override fun onPreciseLocationClick() = askForPreciseLocation()

    // Hidden at once, so the note doesn't wait for the close to be saved
    override fun onPreciseLocationNoteClose() {
        componentState.update { it.copy(isPreciseLocationNoteClosed = true) }
        closeNoteUseCase.execute(Note.PreciseLocation) {
            disposePrevious(false)
            onError { error -> logger.e(error) { "Precise location note not closed" } }
        }
    }

    override fun onDriveClick() {
        val state = viewState.value
        val route = state.driveRoute ?: return
        drive(route, routeNumber = state.sheetStep?.driveRouteNumber)
    }

    // Closing the note comes before the hand-off, so the dialog doesn't come back when the maps app turns out to be missing
    override fun onFirstDriveConfirm() {
        val pending = pendingDrive
        componentState.update { it.copy(driveDialog = null, isFirstDriveExplained = true) }
        closeNoteUseCase.execute(Note.FirstDrive) {
            disposePrevious(false)
            onError { error -> logger.e(error) { "First drive note not closed" } }
        }
        pending?.let { handOff(it.route, it.routeNumber) }
    }

    override fun onDriveDialogDismiss() = componentState.update { it.copy(driveDialog = null) }

    override fun onMapsAppStoreClick() {
        componentState.update { it.copy(driveDialog = null) }
        openMapsAppStoreUseCase.execute {
            onError { error -> logger.e(error) { "Maps app store not opened" } }
        }
    }

    override fun onReminderClick() {
        val reminder = componentState.value.reminder ?: return
        openRatingStep(reminder.question)
    }

    // Dismissing is for good: the question is skipped, so the drive isn't asked about again
    override fun onReminderDismiss() {
        val reminder = componentState.value.reminder ?: return
        clearReminder()
        skipDriveQuestion(reminder.question.driveId)
    }

    // The step takes the place of everything above the search, and the map shows the drive's ends
    private fun openRatingStep(question: DriveQuestion) {
        clearReminder()
        replaceAboveSearch(SheetStepConfig.Rating(question))
        stopFollowing()
        setSheetHeight(SheetHeight.Half)
    }

    // Drives from an earlier start without a connection; offline is expected here, so it isn't logged
    private fun sendPendingDrives() = sendPendingDrivesUseCase.execute {
        onError { error -> if (error !is OfflineException) logger.e(error) { "Pending drives not sent" } }
    }

    // A saved route's step saves its route under the saved route's name, and is checked against that saved route; a past drive's
    // step saves under the destination's name, and is checked against the drive's route
    override fun onSaveClick() {
        val state = viewState.value
        val route = state.driveRoute ?: return
        toggleSave(route, name = savedRouteName(state), savedOnScreen = state.savedRouteOnScreen)
    }

    override fun onRouteNameSave(name: String) {
        val dialog = componentState.value.nameDialog ?: return
        componentState.update { it.copy(nameDialog = null) }
        val route = viewState.value.routeOnScreen(dialog.routeId) ?: return
        saveRoute(route, name)
    }

    override fun onRouteNameDismiss() = componentState.update { it.copy(nameDialog = null) }

    // Saving waits for the sign-in: the route is saved once the sheet closes signed in, and closing it leaves it unsaved
    override fun onSignInToSaveFinished(isSignedIn: Boolean) {
        val state = viewState.value
        val routeId = pendingSaveRouteId
        pendingSaveRouteId = null
        if (!isSignedIn || routeId == null) return
        val route = state.driveRoute?.takeIf { it.id == routeId } ?: return
        saveRoute(route, savedRouteName(state))
    }

    override fun onMyLocationClick() = useMyLocation()

    private fun useMyLocation() {
        when (viewState.value.myLocationRow) {
            MyLocationRow.Available -> routePlanningChild()?.useMyLocation()
            MyLocationRow.NeedsPreciseLocation -> askForPreciseLocation(then = ::useMyLocationOnceUsable)
            MyLocationRow.NeedsLocationAccess -> askForLocationAccess(then = ::useMyLocationOnceUsable)
            MyLocationRow.OutsideCoveredArea -> sendUiEvent(MapSheetUiEvent.NotCovered)
            null -> Unit
        }
    }

    // Access given in the prompt makes the user's location the start, unless it's still not usable or the step has closed
    private fun useMyLocationOnceUsable() {
        val planning = routePlanningChild() ?: return
        if (planning.isStartSearchOpen && viewState.value.hasUsableLocation) planning.useMyLocation()
    }

    private fun closeSearchStep() {
        val planning = routePlanningChild()
        if (planning?.isStartSearchOpen == true) planning.closeStartSearch() else closeSearch()
    }

    private fun closeSearch() {
        rootPlaceSearch().clear()
        setSheetHeight(SheetHeight.Collapsed)
    }

    // Every change of the sheet's height goes through here to keep Back right
    private fun setSheetHeight(height: SheetHeight) {
        componentState.update { it.copy(sheetHeight = height) }
        syncBackCallback()
    }

    private fun syncBackCallback() {
        backCallback.isEnabled = componentState.value.sheetHeight != SheetHeight.Collapsed
    }

    // The parent's own state, with the start search of planning and the step on top of the stack
    private fun project(): MapSheetViewState {
        val state = componentState.value
        return MapSheetViewState(
            locationAccess = state.locationAccess,
            baseMap = state.baseMap,
            isPreciseLocationNoteClosed = state.isPreciseLocationNoteClosed,
            sheetHeight = state.sheetHeight,
            search = activePlaceSearch().viewState.value.search,
            distanceUnit = state.distanceUnit,
            startSearch = routePlanningChild()?.viewState?.value?.startSearch,
            reminder = state.reminder,
            sheetStep = stack.value.active.instance.sheetStep,
            isFirstDriveExplained = state.isFirstDriveExplained,
            driveDialog = state.driveDialog,
            isSignedIn = state.isSignedIn,
            savedRoutes = state.savedRoutes,
            isSavingRoute = state.isSavingRoute,
            nameDialog = state.nameDialog,
        )
    }

    // Everything above the root search makes way for [config]. A rating step left this way closes as its close button closes
    // it, so its question isn't asked again unanswered
    private fun replaceAboveSearch(config: SheetStepConfig) {
        ratingChild()?.close()
        stepNavigation.navigate { listOf(it.first(), config) }
    }

    // A step closes by popping, and the sheet goes to the height the step asks for over the search it leaves
    private fun closeStep(sheetHeight: SheetHeight) {
        stepNavigation.pop()
        setSheetHeight(sheetHeight)
    }

    private fun isRatingOpen(): Boolean = ratingChild() != null

    private fun ratingChild(): RatingComponent? = (stack.value.active.instance as? SheetStepChild.Rating)?.component

    private fun oneRouteChild(): OneRouteComponent? = (stack.value.active.instance as? SheetStepChild.OneRoute)?.component

    private fun placeSearches(): List<PlaceSearchComponent> =
        listOfNotNull(rootPlaceSearch(), routePlanningChild()?.startSearch)

    // Planning's start search while it's open, else the destination search
    private fun activePlaceSearch(): PlaceSearchComponent = routePlanningChild()?.startSearch ?: rootPlaceSearch()

    private fun rootPlaceSearch(): PlaceSearchComponent = (stack.value.items.first().instance as SheetStepChild.PlaceSearch).component

    // Route planning, while it's the step on top
    private fun routePlanningChild(): RoutePlanningComponent? =
        (stack.value.active.instance as? SheetStepChild.RoutePlanning)?.component

    private fun covers(point: GeoPoint): Boolean = componentState.value.baseMap.covers(point)

    // The map stops moving with the user, as the user has moved it or a step shows a route or drive instead
    private fun stopFollowing() = componentState.update { it.copy(baseMap = it.baseMap.copy(followsUser = false)) }

    private fun frame(point: GeoPoint) = componentState.update { it.copy(baseMap = it.baseMap.framing(point)) }

    /** Asks for access, then calls [then] once the answer is applied. */
    private fun askForLocationAccess(then: () -> Unit = {}) {
        // iOS asks only once, so on iOS a denial is always for good and this opens the settings
        if (componentState.value.locationAccess == LocationAccess.DeniedForGood) {
            openLocationSettingsUseCase.execute { onError { error -> logger.e(error) { "System settings not opened" } } }
        } else {
            requestLocationAccessUseCase.execute {
                onSuccess { access ->
                    applyLocationAccess(access)
                    then()
                }
                onError { error -> logger.e(error) { "Location prompt failed" } }
            }
        }
    }

    /** Asks for precise location, then calls [then] once the answer is applied. */
    private fun askForPreciseLocation(then: () -> Unit = {}) = requestPreciseLocationUseCase.execute {
        onSuccess { access ->
            applyLocationAccess(access)
            then()
        }
        onError { error -> logger.e(error) { "Precise location prompt failed" } }
    }

    // The first covered area sets the overview; until it's loaded the map shows the launch region, undimmed
    private fun loadCoverage() = observeCoverageUseCase.execute {
        onNext { coverage -> componentState.update { it.copy(baseMap = it.baseMap.withCoverage(coverage)) } }
        onError { error -> logger.e(error) { "Covered area not loaded" } }
    }

    private fun readLocationAccess() = getLocationAccessUseCase.execute {
        onSuccess(::applyLocationAccess)
        onError { error -> logger.e(error) { "Location access not read" } }
    }

    // The precise location and first-drive notes share these use cases, so one read or close doesn't cancel the other
    private fun readPreciseLocationNote() = isNoteClosedUseCase.execute(Note.PreciseLocation) {
        disposePrevious(false)
        onSuccess { isClosed -> componentState.update { it.copy(isPreciseLocationNoteClosed = isClosed) } }
        onError { error -> logger.e(error) { "Precise location note not read" } }
    }

    private fun applyLocationAccess(access: LocationAccess) {
        componentState.update { it.copy(locationAccess = access, baseMap = it.baseMap.withAccess(access)) }
        if (access.allowsLocation) startLocationUpdates() else stopLocationUpdates()
    }

    // Updates pause while the screen is stopped and resume on start, so access read again on start doesn't restart them
    private fun startLocationUpdates() {
        if (useCaseJobPool[observeUserLocationUseCase]?.isActive == true) return
        observeUserLocationUseCase.executeWithLifecycle {
            onNext { point ->
                componentState.update { it.copy(baseMap = it.baseMap.withUserLocation(point)) }
                routePlanningChild()?.onUserLocationChange(point)
            }
            onError { error -> logger.e(error) { "Location updates failed" } }
        }
    }

    // Running flow use cases are kept in the job pool, by use case
    private fun stopLocationUpdates() {
        useCaseJobPool[observeUserLocationUseCase]?.cancel()
    }

    // Shows the first-drive dialog until the user has gone past it; after that the hand-off goes on at once
    private fun drive(route: Route, routeNumber: Int?) {
        if (componentState.value.isFirstDriveExplained) {
            handOff(route, routeNumber)
        } else {
            pendingDrive = PendingDrive(route, routeNumber)
            componentState.update { it.copy(driveDialog = DriveDialog.FirstDrive) }
        }
    }

    private fun handOff(route: Route, routeNumber: Int?) {
        handOffUseCase.execute(route) {
            onSuccess { result ->
                when (result) {
                    MapsAppResult.Opened -> {
                        // A new drive replaces the reminder about an earlier one
                        clearReminder()
                        startDrive(route, routeNumber)
                    }

                    MapsAppResult.Missing -> componentState.update { it.copy(driveDialog = DriveDialog.MapsAppMissing) }
                }
            }
            onError { error -> logger.e(error) { "Route not handed off" } }
        }
    }

    // Errors are only logged: the drive stays stored and is recorded on a later try
    private fun startDrive(route: Route, routeNumber: Int?) {
        startDriveUseCase.execute(StartDriveUseCase.Args(route = route, routeNumber = routeNumber, at = Clock.System.now())) {
            onError { error -> if (error !is OfflineException) logger.e(error) { "Drive not started" } }
        }
    }

    private fun readFirstDriveNote() = isNoteClosedUseCase.execute(Note.FirstDrive) {
        disposePrevious(false)
        onSuccess { isClosed -> componentState.update { it.copy(isFirstDriveExplained = isClosed) } }
        onError { error -> logger.e(error) { "First drive note not read" } }
    }

    // Locked while a route is being saved or removed. [savedOnScreen] is the saved route the route on screen is checked against.
    private fun toggleSave(route: Route, name: String?, savedOnScreen: SavedRoute?) {
        val state = componentState.value
        if (state.isSavingRoute) return
        if (!state.isSignedIn) {
            pendingSaveRouteId = route.id
            openSignInToSaveRoute()
            return
        }
        if (savedOnScreen != null) {
            removeRoute(savedOnScreen.id)
        } else {
            saveRoute(route, name)
        }
    }

    private fun observeSignedIn() = observeSignedInUseCase.execute {
        onNext { isSignedIn -> componentState.update { it.copy(isSignedIn = isSignedIn) } }
        onError { error -> logger.e(error) { "Sign-in not observed" } }
    }

    private fun observeSavedRoutes() = observeSavedRoutesUseCase.execute {
        onNext { routes -> componentState.update { it.copy(savedRoutes = routes.toImmutableList()) } }
        onError { error -> logger.e(error) { "Saved routes not observed" } }
    }

    private fun saveRoute(route: Route, name: String?) {
        setSavingRoute(true)
        saveRouteUseCase.execute(SaveRouteUseCase.Args(route = route, name = name)) {
            onSuccess { result ->
                setSavingRoute(false)
                when (result) {
                    is SaveRouteResult.NameTaken -> showNameDialog(route.id, result)
                    is SaveRouteResult.Saved -> oneRouteChild()?.savedAs(route.id, result.savedRoute)
                }
            }
            onError { error ->
                setSavingRoute(false)
                reportSaveError(error, MapSheetUiEvent.SaveRouteOffline, MapSheetUiEvent.SaveRouteFailed, "Route not saved")
            }
        }
    }

    private fun setSavingRoute(isSaving: Boolean) = componentState.update { it.copy(isSavingRoute = isSaving) }

    private fun reportSaveError(error: Throwable, offline: MapSheetUiEvent, failed: MapSheetUiEvent, logMessage: String) {
        if (error is OfflineException) {
            sendUiEvent(offline)
        } else {
            logger.e(error) { logMessage }
            sendUiEvent(failed)
        }
    }

    private fun showNameDialog(routeId: String, taken: SaveRouteResult.NameTaken) = componentState.update {
        it.copy(nameDialog = RouteNameDialog(routeId, taken.takenName, taken.suggestion, taken.takenNames.toImmutableList()))
    }

    private fun removeRoute(savedRouteId: SavedRouteId) {
        setSavingRoute(true)
        unsaveRouteUseCase.execute(savedRouteId) {
            onSuccess { setSavingRoute(false) }
            onError { error ->
                setSavingRoute(false)
                reportSaveError(error, MapSheetUiEvent.RemoveRouteOffline, MapSheetUiEvent.RemoveRouteFailed, "Route not removed")
            }
        }
    }

    private fun clearReminder() = componentState.update { it.copy(reminder = null) }

    private fun askAboutDrive() {
        if (isRatingOpen()) return
        getDriveQuestionUseCase.execute(Clock.System.now()) {
            onSuccess { prompt -> if (prompt != null) showDrivePrompt(prompt) }
            onError { error -> logger.e(error) { "Drive question not read" } }
        }
    }

    private fun showDrivePrompt(prompt: DrivePrompt) = when (prompt.kind) {
        DrivePrompt.Kind.RatingStep -> openRatingStep(prompt.question)
        DrivePrompt.Kind.Bubble -> showReminder(prompt.question)
    }

    private fun showReminder(question: DriveQuestion) {
        val day = driveDayText(question.startedAt, Clock.System.now(), TimeZone.currentSystemDefault())
        componentState.update { it.copy(reminder = ReminderBubble(question = question, day = day)) }
    }

    // A tap on a drive's notification opens its rating step over whatever is open; a rating step already open stays as it is, as
    // the phone keeps one question
    private fun observeOpenedReminder() = observeOpenedReminderUseCase.execute {
        onNext { question ->
            if (!isRatingOpen()) openRatingStep(question)
        }
        onError { error -> logger.e(error) { "Opened reminder not observed" } }
    }

    // Skipping the question of a drive, as the reminder bubble's dismiss does
    private fun skipDriveQuestion(driveId: String) = skipDriveQuestionUseCase.execute(driveId) {
        onError { error -> logger.e(error) { "Drive question not skipped" } }
    }

    private class PendingDrive(val route: Route, val routeNumber: Int?)
}

// The name a saved route's step saves under: its saved route's name, or none for any other step
private fun savedRouteName(state: MapSheetViewState): String? =
    (state.oneRouteStep?.origin as? OneRouteOrigin.Saved)?.savedRoute?.name
