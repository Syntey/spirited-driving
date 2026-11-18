package com.syntey.spiriteddriving.feature.routes.ui.tripHistoryScreen

import app.futured.arkitekt.crusecases.execute
import app.futured.arkitekt.annotation.GenerateFactory
import co.touchlab.kermit.Logger
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.ScreenComponent
import com.syntey.spiriteddriving.feature.routes.domain.ClearTripHistoryUseCase
import com.syntey.spiriteddriving.feature.routes.domain.DeletePastDriveUseCase
import com.syntey.spiriteddriving.feature.routes.domain.LoadMoreTripHistoryUseCase
import com.syntey.spiriteddriving.feature.routes.domain.ObserveDistanceUnitUseCase
import com.syntey.spiriteddriving.feature.routes.domain.ObserveTripHistoryUseCase
import com.syntey.spiriteddriving.feature.routes.domain.RatePastDriveUseCase
import com.syntey.spiriteddriving.feature.routes.domain.RefreshTripHistoryUseCase
import com.syntey.spiriteddriving.feature.routes.model.ui.toRatingSheet
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlin.time.Clock
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
@GenerateFactory
internal class TripHistoryComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam override val navigation: TripHistoryScreenNavigation,
    private val observeTripHistoryUseCase: ObserveTripHistoryUseCase,
    private val observeDistanceUnitUseCase: ObserveDistanceUnitUseCase,
    private val refreshTripHistoryUseCase: RefreshTripHistoryUseCase,
    private val loadMoreTripHistoryUseCase: LoadMoreTripHistoryUseCase,
    private val deletePastDriveUseCase: DeletePastDriveUseCase,
    private val clearTripHistoryUseCase: ClearTripHistoryUseCase,
    private val ratePastDriveUseCase: RatePastDriveUseCase,
) : ScreenComponent<TripHistoryViewState, TripHistoryUiEvent, TripHistoryScreenNavigation>(
    componentContext = componentContext,
    defaultState = TripHistoryViewState(),
),
    TripHistoryScreen,
    TripHistoryScreenNavigation by navigation,
    TripHistoryScreen.Actions {

    private val logger = Logger.withTag("TripHistoryComponent")

    override val viewState: StateFlow<TripHistoryViewState> = componentState
    override val title: StringDesc = MR.strings.trip_history_title.desc()
    override val actions: TripHistoryScreen.Actions = this

    init {
        doOnCreate {
            observeTripHistory()
            observeDistanceUnit()
            refresh()
        }
    }

    override fun onDriveClick(id: String) = openOnMap(id)

    override fun onPlanNewRoutesClick(id: String) = planNewRoutes(id)

    override fun onRateClick(id: String) {
        val state = componentState.value
        val drive = state.page?.drives?.firstOrNull { it.id == id } ?: return
        componentState.update {
            it.copy(ratingSheet = drive.toRatingSheet(state.now, state.timeZone, state.distanceUnit))
        }
    }

    override fun onRatingThumbClick(thumb: Thumb) {
        val rating = componentState.value.ratingSheet?.rating
        // Tapping the thumb already picked changes nothing and sends nothing
        if (rating?.thumb == thumb) return
        rateSheet(Rating(thumb))
    }

    // Only the reasons of the picked thumb can be picked
    override fun onRatingReasonClick(reason: RatingReason) {
        val rating = componentState.value.ratingSheet?.rating ?: return
        if (reason.thumb != rating.thumb) return
        val reasons = if (reason in rating.reasons) rating.reasons - reason else rating.reasons + reason
        rateSheet(Rating(rating.thumb, reasons))
    }

    override fun onRatingSheetClose() {
        componentState.update { it.copy(ratingSheet = null) }
    }

    override fun onDeleteClick(id: String) {
        componentState.update { it.copy(deletingIds = (it.deletingIds + id).toImmutableSet()) }
        sendUiEvent(TripHistoryUiEvent.DriveDeleted(id))
    }

    override fun onDeleteUndo(id: String) = restoreRow(id)

    override fun onDeleteMessageGone(id: String) = deleteDrive(id)

    override fun onClearClick() {
        componentState.update { it.copy(isClearConfirmationShown = true) }
    }

    override fun onClearConfirm() {
        if (componentState.value.isClearing) return
        componentState.update { it.copy(isClearConfirmationShown = false, isClearing = true) }
        clearHistory()
    }

    override fun onClearDismiss() {
        componentState.update { it.copy(isClearConfirmationShown = false) }
    }

    override fun onLoadMore() {
        val state = componentState.value
        if (state.isLoadingMore || !state.hasMore) return
        componentState.update { it.copy(isLoadingMore = true) }
        loadMore()
    }

    override fun onRetry() {
        componentState.update { it.copy(status = TripHistoryStatus.Loading) }
        refresh()
    }

    override fun onClose() = pop()

    private fun observeTripHistory() = observeTripHistoryUseCase.execute {
        onNext { page ->
            componentState.update { state ->
                state.copy(
                    page = page,
                    status = if (page != null) TripHistoryStatus.Ready else state.status,
                )
            }
        }
        onError { error -> logger.e(error) { "Trip history not observed" } }
    }

    private fun observeDistanceUnit() = observeDistanceUnitUseCase.execute {
        onNext { unit -> componentState.update { it.copy(distanceUnit = unit) } }
        onError { error -> logger.e(error) { "Distance unit not observed" } }
    }

    // Keeps what's shown when there is something: a refresh that fails then changes nothing
    private fun refresh() {
        updateClock()
        refreshTripHistoryUseCase.execute {
            onSuccess { componentState.update { state -> state.copy(status = TripHistoryStatus.Ready) } }
            onError { error ->
                if (componentState.value.page == null) {
                    val status = if (error is OfflineException) TripHistoryStatus.Offline else TripHistoryStatus.Failed
                    componentState.update { state -> state.copy(status = status) }
                }
                // Offline is expected here, so it isn't logged; the list loads again when the user tries
                if (error !is OfflineException) logger.e(error) { "Trip history not refreshed" }
            }
        }
    }

    private fun deleteDrive(id: String) = deletePastDriveUseCase.execute(id) {
        onSuccess { restoreRow(id) }
        onError { error ->
            // The row comes back where it was, and the message says why it's still there
            restoreRow(id)
            if (error is OfflineException) {
                sendUiEvent(TripHistoryUiEvent.ChangesOffline)
            } else {
                logger.e(error) { "Trip history drive not deleted" }
                sendUiEvent(TripHistoryUiEvent.DeleteFailed)
            }
        }
    }

    private fun clearHistory() = clearTripHistoryUseCase.execute {
        onSuccess { componentState.update { state -> state.copy(isClearing = false) } }
        onError { error ->
            componentState.update { state -> state.copy(isClearing = false) }
            if (error is OfflineException) {
                sendUiEvent(TripHistoryUiEvent.ChangesOffline)
            } else {
                logger.e(error) { "Trip history not cleared" }
                sendUiEvent(TripHistoryUiEvent.ClearFailed)
            }
        }
    }

    // Each change is kept and sent at once, as the rating step does. Each send cancels the one before, so the latest rating is
    // the one kept.
    private fun rateSheet(rating: Rating) {
        val sheet = componentState.value.ratingSheet ?: return
        componentState.update { it.copy(ratingSheet = it.ratingSheet?.copy(rating = rating)) }
        ratePastDriveUseCase.execute(RatePastDriveUseCase.Args(driveId = sheet.driveId, rating = rating)) {
            // Offline, the rating is sent later, so it isn't logged
            onError { error -> if (error !is OfflineException) logger.e(error) { "Trip history drive not rated" } }
        }
    }

    private fun restoreRow(id: String) {
        componentState.update { it.copy(deletingIds = (it.deletingIds - id).toImmutableSet()) }
    }

    private fun loadMore() = loadMoreTripHistoryUseCase.execute {
        onSuccess { componentState.update { state -> state.copy(isLoadingMore = false) } }
        onError { error ->
            componentState.update { state -> state.copy(isLoadingMore = false) }
            if (error !is OfflineException) logger.e(error) { "Trip history page not loaded" }
        }
    }

    // The days of the rows are worked out from the time the list was last loaded
    private fun updateClock() {
        componentState.update { it.copy(now = Clock.System.now(), timeZone = TimeZone.currentSystemDefault()) }
    }
}
