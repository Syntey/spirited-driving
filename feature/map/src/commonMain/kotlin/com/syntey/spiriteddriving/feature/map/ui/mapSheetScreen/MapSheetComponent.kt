package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import app.futured.arkitekt.decompose.ext.asStateFlow
import co.touchlab.kermit.Logger
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.lifecycle.doOnStart
import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.PlaceSuggestion
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.BudgetKind
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.domain.settings.Note
import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.ScreenComponent
import com.syntey.spiriteddriving.core.ui.base.derivedStateFlow
import com.syntey.spiriteddriving.core.ui.base.executeWithLifecycle
import com.syntey.spiriteddriving.feature.map.domain.CloseNoteUseCase
import com.syntey.spiriteddriving.feature.map.domain.GetLocationAccessUseCase
import com.syntey.spiriteddriving.feature.map.domain.GetPastDriveUseCase
import com.syntey.spiriteddriving.feature.map.domain.HandOffUseCase
import com.syntey.spiriteddriving.feature.map.domain.IsNoteClosedUseCase
import com.syntey.spiriteddriving.feature.map.domain.LoadPastDriveRouteUseCase
import com.syntey.spiriteddriving.feature.map.domain.LoadSavedRouteUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveCoverageUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveDistanceUnitUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveUserLocationUseCase
import com.syntey.spiriteddriving.feature.map.domain.OpenLocationSettingsUseCase
import com.syntey.spiriteddriving.feature.map.domain.OpenMapsAppStoreUseCase
import com.syntey.spiriteddriving.feature.map.domain.RequestLocationAccessUseCase
import com.syntey.spiriteddriving.feature.map.domain.RequestPreciseLocationUseCase
import com.syntey.spiriteddriving.feature.map.domain.SendPendingDrivesUseCase
import com.syntey.spiriteddriving.feature.map.domain.StartDriveUseCase
import com.syntey.spiriteddriving.feature.map.domain.DrivePrompt
import com.syntey.spiriteddriving.feature.map.domain.GetDriveQuestionUseCase
import com.syntey.spiriteddriving.feature.map.domain.MarkNotDrivenUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveOpenedReminderUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveSavedRoutesUseCase
import com.syntey.spiriteddriving.feature.map.domain.ObserveSignedInUseCase
import com.syntey.spiriteddriving.feature.map.domain.SaveRouteResult
import com.syntey.spiriteddriving.feature.map.domain.SaveRouteUseCase
import com.syntey.spiriteddriving.feature.map.domain.UnsaveRouteUseCase
import com.syntey.spiriteddriving.feature.map.domain.ShouldOfferReminderUseCase
import com.syntey.spiriteddriving.feature.map.domain.TurnOnRemindersUseCase
import com.syntey.spiriteddriving.feature.map.domain.RateDriveUseCase
import com.syntey.spiriteddriving.feature.map.domain.SkipDriveQuestionUseCase
import com.syntey.spiriteddriving.feature.map.domain.unnamedPlace
import com.syntey.spiriteddriving.feature.map.model.ui.DriveDialog
import com.syntey.spiriteddriving.feature.map.model.ui.LocateButton
import com.syntey.spiriteddriving.feature.map.model.ui.MyLocationRow
import com.syntey.spiriteddriving.feature.map.model.ui.PlanningStatus
import com.syntey.spiriteddriving.feature.map.model.ui.RatingStep
import com.syntey.spiriteddriving.feature.map.model.ui.ReminderBubble
import com.syntey.spiriteddriving.feature.map.model.ui.RouteNameDialog
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteOrigin
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteStatus
import com.syntey.spiriteddriving.feature.map.model.ui.OneRouteStep
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import com.syntey.spiriteddriving.feature.map.model.ui.savedAs
import com.syntey.spiriteddriving.feature.map.model.ui.StartSearch
import com.syntey.spiriteddriving.feature.map.model.ui.allowsLocation
import com.syntey.spiriteddriving.core.resources.datetime.driveDayText
import com.syntey.spiriteddriving.core.resources.datetime.driveTimeText
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
// Each action of the sheet and each read on start is a function here
@Suppress("TooManyFunctions", "LargeClass")
internal class MapSheetComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam override val navigation: MapSheetScreenNavigation,
    private val getLocationAccessUseCase: GetLocationAccessUseCase,
    private val requestLocationAccessUseCase: RequestLocationAccessUseCase,
    private val openLocationSettingsUseCase: OpenLocationSettingsUseCase,
    private val observeUserLocationUseCase: ObserveUserLocationUseCase,
    private val observeCoverageUseCase: ObserveCoverageUseCase,
    private val requestPreciseLocationUseCase: RequestPreciseLocationUseCase,
    private val isNoteClosedUseCase: IsNoteClosedUseCase,
    private val closeNoteUseCase: CloseNoteUseCase,
    private val observeDistanceUnitUseCase: ObserveDistanceUnitUseCase,
    private val sendPendingDrivesUseCase: SendPendingDrivesUseCase,
    private val getDriveQuestionUseCase: GetDriveQuestionUseCase,
    private val rateDriveUseCase: RateDriveUseCase,
    private val skipDriveQuestionUseCase: SkipDriveQuestionUseCase,
    private val markNotDrivenUseCase: MarkNotDrivenUseCase,
    private val observeOpenedReminderUseCase: ObserveOpenedReminderUseCase,
    private val shouldOfferReminderUseCase: ShouldOfferReminderUseCase,
    private val turnOnRemindersUseCase: TurnOnRemindersUseCase,
    private val loadSavedRouteUseCase: LoadSavedRouteUseCase,
    private val getPastDriveUseCase: GetPastDriveUseCase,
    private val loadPastDriveRouteUseCase: LoadPastDriveRouteUseCase,
) : ScreenComponent<MapSheetViewState, MapSheetUiEvent, MapSheetScreenNavigation>(
    componentContext = componentContext,
    defaultState = MapSheetViewState(),
),
    MapSheetScreen,
    MapSheetScreenNavigation by navigation,
    MapSheetScreen.Actions {

    private val logger = Logger.withTag("MapSheetComponent")

    // What the sheet's steps reach on this parent
    private val host = object : MapSheetHost {
        override val userLocation: GeoPoint? get() = componentState.value.map.userLocation
        override val isStartSearchOpen: Boolean get() = isStartSearchOpen()
        override fun requestSheetHeight(height: SheetHeight) = setSheetHeight(height)
        override fun covers(point: GeoPoint): Boolean = componentState.value.map.covers(point)
        override fun onNotCovered() = sendUiEvent(MapSheetUiEvent.NotCovered)
        override fun onPlaceChosen(place: Place) = choose(place)
        override fun onPlaceNamed(place: Place) = rootPlaceSearch().keep(place)
        override fun onRecentPlacesChanged(places: List<Place>) = placeSearches().forEach { it.showRecentPlaces(places) }
    }

    private val stepNavigation = StackNavigation<SheetStepConfig>()

    // The root is the destination search, which stays alive under the steps pushed over it, so coming back shows it as it was
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

                is SheetStepConfig.StartSearch -> SheetStepChild.PlaceSearch(
                    PlaceSearchComponentFactory.createComponent(childCtx, host, PlaceSearchComponent.Args(isStart = true)),
                )

                is SheetStepConfig.RoutePlanning -> SheetStepChild.RoutePlanning(
                    RoutePlanningComponentFactory.createComponent(
                        childCtx,
                        host,
                        RoutePlanningComponent.Args(config.destination, config.waitsForStart),
                    ),
                )
            }
        },
    ).asStateFlow()

    // Drive and Save live as long as the sheet, each with its own use case scope, so they stay out of the navigation
    private val driveListener = object : DriveHandOffComponent.Listener {
        override fun onHandedOff() {
            // A new drive replaces the reminder about an earlier one
            routePlanningChild()?.markHandedOff()
            componentState.update {
                it.copy(
                    sheetStep = it.sheetStep.let { step ->
                        if (step is OneRouteStep) step.copy(isHandedOff = true) else step
                    },
                    reminder = null,
                )
            }
        }
    }

    private val saveListener = object : RouteSavingComponent.Listener {
        override fun openSignInToSaveRoute() = openSignInForSaving()
        override fun onSaved(routeId: String, savedRoute: SavedRoute) = pointStepAtSavedRoute(routeId, savedRoute)
        override fun onSaveEvent(event: MapSheetUiEvent) = sendUiEvent(event)
    }

    private val driveHandOff: DriveHandOffComponent = DriveHandOffComponentFactory.createComponent(
        childContext("driveHandOff"),
        driveListener,
    )

    private val routeSaving: RouteSavingComponent = RouteSavingComponentFactory.createComponent(
        childContext("routeSaving"),
        saveListener,
    )

    // Projected from the parent's state, the dialogs of Drive and Save, and the step on top of the stack, and followed as each changes
    @OptIn(ExperimentalCoroutinesApi::class)
    override val viewState: StateFlow<MapSheetViewState> = derivedStateFlow(
        changes = merge(
            componentState,
            driveHandOff.viewState,
            routeSaving.viewState,
            stack.flatMapLatest { stack -> combine(stack.items.map { it.instance.state }) { } },
        ),
    ) { project() }

    override val actions: MapSheetScreen.Actions = this

    // Back closes the innermost step first: the rating step, then a saved route, then the start step, then route planning,
    // then the search while the sheet is pulled up
    private val backCallback = BackCallback(isEnabled = false) {
        val state = componentState.value
        if (state.sheetStep != null) {
            closeSheetStep()
        } else if (isStartSearchOpen()) {
            closeStartSearch()
        } else if (isRoutePlanningOpen()) {
            onRoutePlanningClose()
        } else {
            closeSearch()
        }
    }

    init {
        backHandler.register(backCallback)
        // On every start, so access turned on in the system settings shows when the user comes back, and so coming back from
        // the maps app asks about the drive
        doOnStart(isOneTime = false) {
            readLocationAccess()
            askAboutDrive()
        }
        loadCoverage()
        readPreciseLocationNote()
        observeDistanceUnit()
        sendPendingDrives()
        observeOpenedReminder()
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

    // The start step has no collapsed layout, so dragging it down closes it
    override fun onSheetHeightChange(height: SheetHeight) {
        if (isStartSearchOpen() && height == SheetHeight.Collapsed) closeStartSearch()
        setSheetHeight(height)
    }

    override fun onSuggestionClick(suggestion: PlaceSuggestion) = activePlaceSearch().onSuggestionClick(suggestion)

    override fun onRecentPlaceClick(place: Place) = activePlaceSearch().onRecentPlaceClick(place)

    override fun onRecentPlaceRemove(place: Place) = activePlaceSearch().onRecentPlaceRemove(place)

    private fun choose(place: Place) {
        if (isStartSearchOpen()) {
            rootPlaceSearch().keep(place)
            popStartSearch()
            routePlanningChild()?.useStart(place)
            return
        }
        planTo(place)
    }

    // Kept with the recent places, then planned from the user's location or from a start to be picked
    private fun planTo(destination: Place) {
        rootPlaceSearch().keep(destination)
        componentState.update { it.copy(baseMap = it.baseMap.framing(destination.location)) }
        startPlanning(destination)
    }

    // Replaces everything above the root, so the trip is the only step over the search. Without a usable location the trip waits
    // for a start, so the start step opens and closing it closes planning
    private fun startPlanning(destination: Place) {
        val usable = componentState.value.hasUsableLocation
        stepNavigation.navigate { listOf(it.first(), SheetStepConfig.RoutePlanning(destination, waitsForStart = !usable)) }
        if (usable) {
            setSheetHeight(SheetHeight.Half)
        } else {
            openStartSearch(closesPlanning = true)
        }
    }

    override fun onStartClick() = openStartSearch(closesPlanning = false)

    // The destination search stays under the start search, so closing the step brings it back as it was
    private fun openStartSearch(closesPlanning: Boolean) {
        stepNavigation.navigate { it + SheetStepConfig.StartSearch(closesPlanning) }
        setSheetHeight(SheetHeight.Full)
    }

    // Pops the start search if it's the step on top, and returns the step it was
    private fun popStartSearch(): SheetStepConfig.StartSearch? {
        val config = stack.value.active.configuration as? SheetStepConfig.StartSearch ?: return null
        stepNavigation.pop()
        return config
    }

    private fun closeStartSearch() {
        val closed = popStartSearch() ?: return
        if (closed.closesPlanning) closeRoutePlanning() else setSheetHeight(SheetHeight.Half)
    }

    private fun closeSearchStep() {
        if (isStartSearchOpen()) closeStartSearch() else closeSearch()
    }

    // A null start is the user's location
    private fun useStart(start: Place?) {
        popStartSearch()
        routePlanningChild()?.useStart(start)
    }

    override fun onLocateClick() = locate()
    override fun onMapPanned() = componentState.update { it.copy(baseMap = it.baseMap.copy(followsUser = false)) }

    // The rating step is about the drive just made, not a new trip, so a long press doesn't start one
    override fun onMapLongPress(point: GeoPoint) {
        if (componentState.value.ratingStep == null) pressMap(point)
    }

    private fun pressMap(point: GeoPoint) {
        if (!componentState.value.map.covers(point)) {
            sendUiEvent(MapSheetUiEvent.NotCovered)
            return
        }
        if (isStartSearchOpen()) {
            pickStart(point)
            return
        }
        // A new trip replaces the saved route on screen
        closeOneRouteStep()
        componentState.update { it.copy(baseMap = it.baseMap.copy(followsUser = false)) }
        startPlanning(unnamedPlace(point))
        routePlanningChild()?.nameDestination(point)
    }

    private fun pickStart(point: GeoPoint) {
        popStartSearch()
        routePlanningChild()?.pickStart(point)
    }

    override fun onRouteClick(routeId: String) {
        routePlanningChild()?.onRouteClick(routeId)
    }

    override fun onBudgetClick(budget: Budget) {
        routePlanningChild()?.onBudgetClick(budget)
    }

    override fun onBudgetKindChange(kind: BudgetKind) {
        routePlanningChild()?.onBudgetKindChange(kind)
    }

    override fun onRoutePlanningClose() = closeRoutePlanning()

    // Pops the start search and planning, and brings the search back as it was
    private fun closeRoutePlanning() {
        popToRoot()
        // Full brings the search back as it was, without the keyboard: the keyboard only comes from focusing the field
        setSheetHeight(SheetHeight.Full)
    }

    override fun onPlanningRetry() {
        routePlanningChild()?.onRetry()
    }

    // A saved route's route is loaded in place of a plan; the start and destination pins frame it until it is
    override fun showSavedRoute(savedRouteId: SavedRouteId) {
        val savedRoute = viewState.value.savedRoutes.firstOrNull { it.id == savedRouteId } ?: return
        showOneRoute(OneRouteOrigin.Saved(savedRoute))
    }

    // Nothing shows when the drive isn't kept on the device, as the list it was chosen from is what keeps it
    override fun showPastDrive(driveId: String) {
        getPastDriveUseCase.execute(driveId) {
            onSuccess { drive -> drive?.let { showOneRoute(OneRouteOrigin.Past(it)) } }
            onError { error -> logger.e(error) { "Past drive not read" } }
        }
    }

    // Plans from where the user is now, as a new trip does, so the rating step, a route on its own and the search make way
    override fun planNewRoutes(driveId: String) {
        getPastDriveUseCase.execute(driveId) {
            onSuccess { drive -> drive?.let { planTripTo(it.destination) } }
            onError { error -> logger.e(error) { "Past drive not read" } }
        }
    }

    private fun showOneRoute(origin: OneRouteOrigin) {
        if (componentState.value.ratingStep != null) closeRatingStep()
        popToRoot()
        rootPlaceSearch().clear()
        componentState.update {
            it.copy(
                sheetStep = OneRouteStep(origin = origin),
                baseMap = it.baseMap.copy(followsUser = false),
            )
        }
        setSheetHeight(SheetHeight.Half)
        loadOneRoute(origin)
    }

    private fun planTripTo(destination: Place) {
        if (componentState.value.ratingStep != null) closeRatingStep()
        closeOneRouteStep()
        popStartSearch()
        rootPlaceSearch().clear()
        if (!componentState.value.map.covers(destination.location)) {
            sendUiEvent(MapSheetUiEvent.NotCovered)
            return
        }
        planTo(destination)
    }

    override fun onOneRouteRetry() = retryOneRoute()

    override fun onOneRouteClose() = closeOneRoute()

    private fun retryOneRoute() {
        val origin = componentState.value.oneRouteStep?.origin ?: return
        loadOneRoute(origin)
    }

    private fun closeOneRoute() {
        closeOneRouteStep()
        setSheetHeight(SheetHeight.Collapsed)
    }

    private fun closeOneRouteStep() {
        cancelOneRouteLoads()
        componentState.update { it.copy(sheetStep = it.sheetStep.takeUnless { step -> step is OneRouteStep }) }
    }

    private fun closeSheetStep() {
        when (componentState.value.sheetStep) {
            is RatingStep -> closeRatingStep()
            is OneRouteStep -> closeOneRoute()
            null -> Unit
        }
    }

    private fun cancelOneRouteLoads() {
        useCaseJobPool[loadSavedRouteUseCase]?.cancel()
        useCaseJobPool[loadPastDriveRouteUseCase]?.cancel()
    }

    override fun onSavedRoutesClick() {
        if (viewState.value.isSignedIn) openSavedRoutes() else openSignInForSavedRoutes()
    }

    private fun loadOneRoute(origin: OneRouteOrigin) = when (origin) {
        is OneRouteOrigin.Saved -> loadSavedRoute(origin.savedRoute.id)
        is OneRouteOrigin.Past -> loadPastDriveRoute(origin.drive.id)
    }

    private fun loadSavedRoute(savedRouteId: SavedRouteId) {
        updateOneRouteStep(savedRouteId.value) { it.copy(status = OneRouteStatus.Loading) }
        loadSavedRouteUseCase.execute(savedRouteId) {
            onSuccess { route -> showLoadedRoute(savedRouteId.value, route) }
            onError { error -> showLoadFailure(savedRouteId.value, error, "Saved route not loaded") }
        }
    }

    private fun loadPastDriveRoute(driveId: String) {
        updateOneRouteStep(driveId) { it.copy(status = OneRouteStatus.Loading) }
        loadPastDriveRouteUseCase.execute(driveId) {
            onSuccess { route -> showLoadedRoute(driveId, route) }
            onError { error -> showLoadFailure(driveId, error, "Past drive route not loaded") }
        }
    }

    private fun showLoadedRoute(originId: String, route: Route) {
        updateOneRouteStep(originId) { it.copy(route = route, status = OneRouteStatus.Ready) }
    }

    private fun showLoadFailure(originId: String, error: Throwable, message: String) {
        val status = if (error is OfflineException) {
            OneRouteStatus.Offline
        } else {
            logger.e(error) { message }
            OneRouteStatus.Failed
        }
        updateOneRouteStep(originId) { it.copy(status = status) }
    }

    // Changes the step only while it's still the step of this origin: it may have closed meanwhile
    private fun updateOneRouteStep(originId: String, change: (OneRouteStep) -> OneRouteStep) {
        componentState.update { state ->
            val step = state.oneRouteStep?.takeIf { it.origin.id == originId } ?: return@update state
            state.copy(sheetStep = change(step))
        }
    }

    override fun onProfileClick() = openProfile()

    override fun onPreciseLocationClick() = askForPreciseLocation()

    // Hidden at once, so the note doesn't wait for the close to be saved
    override fun onPreciseLocationNoteClose() {
        componentState.update { it.copy(isPreciseLocationNoteClosed = true) }
        closeNoteUseCase.execute(Note.PreciseLocation) {
            onError { error -> logger.e(error) { "Precise location note not closed" } }
        }
    }

    override fun onDriveClick() {
        val state = viewState.value
        val route = state.driveRoute ?: return
        driveHandOff.drive(route, routeNumber = state.routeRows.firstOrNull { it.id == route.id }?.number)
    }

    override fun onFirstDriveConfirm() = driveHandOff.onFirstDriveConfirm()

    override fun onDriveDialogDismiss() = driveHandOff.onDialogDismiss()

    override fun onMapsAppStoreClick() = driveHandOff.onMapsAppStoreClick()

    override fun onRatingThumbClick(thumb: Thumb) = pickThumb(thumb)

    override fun onRatingReasonClick(reason: RatingReason) = toggleReason(reason)

    override fun onRatingDone() = closeRatingStep()

    override fun onRatingClose() = closeRatingStep()

    override fun onNotDrivenClick() = markNotDriven()

    override fun onReminderClick() = openReminder()

    override fun onReminderDismiss() = dismissReminder()

    override fun onReminderOfferClick() = turnOnReminders()

    private fun askAboutDrive() {
        if (componentState.value.ratingStep != null) return
        getDriveQuestionUseCase.execute(Clock.System.now()) {
            onSuccess { prompt -> if (prompt != null) showDrivePrompt(prompt) }
            onError { error -> logger.e(error) { "Drive question not read" } }
        }
    }

    private fun showDrivePrompt(prompt: DrivePrompt) {
        val state = componentState.value
        // A trip planned or a saved route opened after the hand-off isn't thrown away
        val planning = viewState.value.planning
        val canRatingStepTakeOver = (planning == null || planning.isHandedOff) &&
            (state.oneRouteStep?.isHandedOff ?: true)
        if (prompt.kind == DrivePrompt.Kind.RatingStep && canRatingStepTakeOver) {
            openRatingStep(prompt.question)
        } else {
            // A rating step that can't take the planning's place waits as the reminder, so the question isn't lost while
            // the user plans another trip
            showReminder(prompt.question)
        }
    }

    private fun showReminder(question: DriveQuestion) {
        val day = driveDayText(question.startedAt, Clock.System.now(), TimeZone.currentSystemDefault())
        componentState.update { it.copy(reminder = ReminderBubble(question = question, day = day)) }
    }

    private fun openReminder() {
        val reminder = componentState.value.reminder ?: return
        openRatingStep(reminder.question)
    }

    // Dismissing is for good: the question is skipped, so the drive isn't asked about again
    private fun dismissReminder() {
        val reminder = componentState.value.reminder ?: return
        componentState.update { it.copy(reminder = null) }
        skipDriveQuestion(reminder.question.driveId)
    }

    // The step takes the place of the search and of any planning, and the map shows the drive's ends
    private fun openRatingStep(question: DriveQuestion) {
        cancelOneRouteLoads()
        popToRoot()
        val now = Clock.System.now()
        componentState.update {
            it.copy(
                reminder = null,
                sheetStep = RatingStep(
                    question = question,
                    driveTime = driveTimeText(question.startedAt, now, TimeZone.currentSystemDefault()),
                ),
                baseMap = it.baseMap.copy(followsUser = false),
            )
        }
        setSheetHeight(SheetHeight.Half)
    }

    // Tapping the thumb already picked changes nothing and sends nothing
    private fun pickThumb(thumb: Thumb) {
        val step = componentState.value.ratingStep ?: return
        if (step.rating?.thumb == thumb) return
        updateRating(Rating(thumb))
        // The reasons come below the thumbs, so a collapsed sheet opens to show them
        if (componentState.value.sheetHeight == SheetHeight.Collapsed) setSheetHeight(SheetHeight.Half)
        sendRating()
        if (step.rating == null) offerReminder()
    }

    // Asked after the first thumb only: the offer is for the user who has just rated a drive
    private fun offerReminder() = shouldOfferReminderUseCase.execute {
        onSuccess { shouldOffer -> if (shouldOffer) showReminderOffer() }
        onError { error -> logger.e(error) { "Reminder offer not read" } }
    }

    private fun showReminderOffer() = updateRatingStep { it.copy(showsReminderOffer = true) }

    // Hidden at once, so the offer doesn't wait for the settings to open
    private fun turnOnReminders() {
        updateRatingStep { it.copy(showsReminderOffer = false) }
        turnOnRemindersUseCase.execute {
            onError { error -> logger.e(error) { "Reminders not turned on" } }
        }
    }

    // A tap on a drive's notification opens its rating step, even over planning, as the user asked for it; the step that
    // already shows that drive stays as it is
    private fun observeOpenedReminder() = observeOpenedReminderUseCase.execute {
        onNext { question ->
            if (componentState.value.ratingStep?.question?.driveId != question.driveId) openRatingStep(question)
        }
        onError { error -> logger.e(error) { "Opened reminder not observed" } }
    }

    // Only the reasons of the picked thumb can be picked
    private fun toggleReason(reason: RatingReason) {
        val rating = componentState.value.ratingStep?.rating ?: return
        if (reason.thumb != rating.thumb) return
        val reasons = if (reason in rating.reasons) rating.reasons - reason else rating.reasons + reason
        updateRating(Rating(rating.thumb, reasons))
        sendRating()
    }

    private fun updateRating(rating: Rating) = updateRatingStep { it.copy(rating = rating) }

    // Changes the step only while the rating step is open, as a tap may arrive after it closed
    private fun updateRatingStep(change: (RatingStep) -> RatingStep) {
        componentState.update { state ->
            val step = state.sheetStep as? RatingStep ?: return@update state
            state.copy(sheetStep = change(step))
        }
    }

    // Each send cancels the one before, as a repeated execute does, so the latest rating is the one kept
    private fun sendRating() {
        val step = componentState.value.ratingStep ?: return
        val rating = step.rating ?: return
        rateDriveUseCase.execute(RateDriveUseCase.Args(driveId = step.question.driveId, rating = rating)) {
            // Offline, the rating is sent later, so it isn't logged
            onError { error -> if (error !is OfflineException) logger.e(error) { "Rating not sent" } }
        }
    }

    // A step closed without a thumb drops the drive's question, so it isn't asked about again
    private fun closeRatingStep() {
        val step = componentState.value.ratingStep ?: return
        if (step.rating == null) skipDriveQuestion(step.question.driveId)
        hideRatingStep()
    }

    private fun skipDriveQuestion(driveId: String) = skipDriveQuestionUseCase.execute(driveId) {
        onError { error -> logger.e(error) { "Drive question not skipped" } }
    }

    private fun hideRatingStep() {
        componentState.update { it.copy(sheetStep = it.sheetStep.takeUnless { step -> step is RatingStep }) }
        setSheetHeight(SheetHeight.Collapsed)
    }

    // Marked not driven, the question is already cleared, so the step closes without a skip
    private fun markNotDriven() {
        val step = componentState.value.ratingStep ?: return
        markNotDrivenUseCase.execute(step.question.driveId) {
            // Offline, the removal is sent later, so it isn't logged
            onError { error -> if (error !is OfflineException) logger.e(error) { "Drive not marked as not driven" } }
        }
        hideRatingStep()
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
        routeSaving.toggle(route, name = savedRouteName(state), savedOnScreen = state.savedRouteOnScreen)
    }

    override fun onRouteNameSave(name: String) = routeSaving.onNameSave(name) { routeId -> viewState.value.routeOnScreen(routeId) }

    override fun onRouteNameDismiss() = routeSaving.onNameDismiss()

    // Saving waits for the sign-in: the route is saved once the sheet closes signed in, and closing it leaves it unsaved
    override fun onSignInToSaveFinished(isSignedIn: Boolean) {
        val state = viewState.value
        routeSaving.onSignInFinished(isSignedIn, routeOnScreen = state.driveRoute, name = savedRouteName(state))
    }

    private fun openSignInForSaving() = openSignInToSaveRoute()

    // Saving a saved route's route again makes a new saved route, which the step is about from then on
    private fun pointStepAtSavedRoute(routeId: String, savedRoute: SavedRoute) = componentState.update { state ->
        val step = state.oneRouteStep ?: return@update state
        state.copy(sheetStep = step.savedAs(routeId, savedRoute))
    }

    override fun onMyLocationClick() = useMyLocation()

    private fun useMyLocation() {
        when (viewState.value.myLocationRow) {
            MyLocationRow.Available -> useStart(null)
            MyLocationRow.NeedsPreciseLocation -> askForPreciseLocation(then = ::useMyLocationOnceUsable)
            MyLocationRow.NeedsLocationAccess -> askForLocationAccess(then = ::useMyLocationOnceUsable)
            MyLocationRow.OutsideCoveredArea -> sendUiEvent(MapSheetUiEvent.NotCovered)
            null -> Unit
        }
    }

    // Access given in the prompt makes the user's location the start, unless it's still not usable or the step has closed
    private fun useMyLocationOnceUsable() {
        val state = componentState.value
        if (isStartSearchOpen() && state.hasUsableLocation) useStart(null)
    }

    private fun locate() = when (componentState.value.locateButton) {
        LocateButton.Following -> Unit
        LocateButton.NotFollowing -> componentState.update { it.copy(baseMap = it.baseMap.copy(followsUser = true)) }
        LocateButton.NoAccess -> askForLocationAccess()
    }

    private fun closeSearch() {
        rootPlaceSearch().clear()
        setSheetHeight(SheetHeight.Collapsed)
    }

    // Route planning changes the sheet's height too, so every change of either goes through here to keep Back right
    private fun setSheetHeight(height: SheetHeight) {
        componentState.update { it.copy(sheetHeight = height) }
        syncBackCallback()
    }

    private fun syncBackCallback() {
        val state = componentState.value
        backCallback.isEnabled = state.sheetHeight != SheetHeight.Collapsed ||
            isRoutePlanningOpen() ||
            state.sheetStep != null
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

    private fun readPreciseLocationNote() = isNoteClosedUseCase.execute(Note.PreciseLocation) {
        onSuccess { isClosed -> componentState.update { it.copy(isPreciseLocationNoteClosed = isClosed) } }
        onError { error -> logger.e(error) { "Precise location note not read" } }
    }

    private fun askForPreciseLocation(then: () -> Unit = {}) = requestPreciseLocationUseCase.execute {
        onSuccess { access ->
            applyLocationAccess(access)
            then()
        }
        onError { error -> logger.e(error) { "Precise location prompt failed" } }
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
                val wasUnknown = componentState.value.baseMap.userLocation == null
                componentState.update { it.copy(baseMap = it.baseMap.withUserLocation(point)) }
                if (wasUnknown) routePlanningChild()?.onUserLocationArrived()
            }
            onError { error -> logger.e(error) { "Location updates failed" } }
        }
    }

    // Running flow use cases are kept in the job pool, by use case
    private fun stopLocationUpdates() {
        useCaseJobPool[observeUserLocationUseCase]?.cancel()
    }

    // The parent's own state, with the dialogs of Drive and Save, and the search and start of the step on top of the stack
    private fun project(): MapSheetViewState {
        val drive = driveHandOff.viewState.value
        val save = routeSaving.viewState.value
        val active = stack.value.active.configuration
        val planning = routePlanningChild()?.viewState?.value
        return componentState.value.copy(
            search = activePlaceSearch().viewState.value.search,
            startSearch = (active as? SheetStepConfig.StartSearch)?.let { StartSearch(closesPlanning = it.closesPlanning) },
            planning = planning?.planning,
            budgetChoice = planning?.budgetChoice ?: componentState.value.budgetChoice,
            distanceUnit = planning?.distanceUnit ?: componentState.value.distanceUnit,
            isFirstDriveExplained = drive.isFirstDriveExplained,
            driveDialog = drive.dialog,
            isSignedIn = save.isSignedIn,
            savedRoutes = save.savedRoutes,
            isSavingRoute = save.isSaving,
            nameDialog = save.nameDialog,
        )
    }

    // The start search is the step on top when it's open, and then it takes the map's picks and Back
    private fun isStartSearchOpen(): Boolean = stack.value.active.configuration is SheetStepConfig.StartSearch

    private fun isRoutePlanningOpen(): Boolean = routePlanningChild() != null

    // Pops everything above the root search: the start search, route planning, and a saved route's or the rating step's
    private fun popToRoot() {
        stepNavigation.navigate { listOf(it.first()) }
    }

    private fun placeSearches(): List<PlaceSearchComponent> =
        stack.value.items.map { (it.instance as SheetStepChild.PlaceSearch).component }

    // The topmost place search: the start search when it's open, else the destination search, even under route planning
    private fun activePlaceSearch(): PlaceSearchComponent =
        stack.value.items.asReversed().firstNotNullOf { (it.instance as? SheetStepChild.PlaceSearch)?.component }

    private fun rootPlaceSearch(): PlaceSearchComponent = (stack.value.items.first().instance as SheetStepChild.PlaceSearch).component

    // The route planning step, when it's in the stack, under the start search too
    private fun routePlanningChild(): RoutePlanningComponent? =
        stack.value.items.firstNotNullOfOrNull { (it.instance as? SheetStepChild.RoutePlanning)?.component }
}

// The name a saved route's step saves under: its saved route's name, or none for any other step
private fun savedRouteName(state: MapSheetViewState): String? =
    (state.oneRouteStep?.origin as? OneRouteOrigin.Saved)?.savedRoute?.name
