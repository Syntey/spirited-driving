package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import co.touchlab.kermit.Logger
import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.work.AppWork
import com.syntey.spiriteddriving.core.resources.datetime.driveTimeText
import com.syntey.spiriteddriving.core.ui.base.AppComponent
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.feature.map.domain.MarkNotDrivenUseCase
import com.syntey.spiriteddriving.feature.map.domain.RateDriveUseCase
import com.syntey.spiriteddriving.feature.map.domain.ShouldOfferReminderUseCase
import com.syntey.spiriteddriving.feature.map.domain.SkipDriveQuestionUseCase
import com.syntey.spiriteddriving.feature.map.domain.TurnOnRemindersUseCase
import com.syntey.spiriteddriving.feature.map.model.ui.RatingStep
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.TimeZone
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import kotlin.time.Clock

/**
 * The rating of a drive: the thumbs, the reasons, Not driven, the reminder offer and sending the rating. A step of the sheet's
 * stack, which replaces everything above the root search; closing it pops it.
 */
@Factory
@GenerateFactory
internal class RatingComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam private val host: MapSheetHost,
    @InjectedParam private val args: Args,
    private val appWork: AppWork,
    private val rateDriveUseCase: RateDriveUseCase,
    private val skipDriveQuestionUseCase: SkipDriveQuestionUseCase,
    private val markNotDrivenUseCase: MarkNotDrivenUseCase,
    private val shouldOfferReminderUseCase: ShouldOfferReminderUseCase,
    private val turnOnRemindersUseCase: TurnOnRemindersUseCase,
) : AppComponent<RatingViewState, Nothing>(
    componentContext = componentContext,
    defaultState = RatingViewState(
        step = RatingStep(
            question = args.question,
            driveTime = driveTimeText(args.question.startedAt, Clock.System.now(), TimeZone.currentSystemDefault()),
        ),
    ),
) {

    /** @property question The drive's question, which the rating answers. */
    data class Args(val question: DriveQuestion)

    private val logger = Logger.withTag("RatingComponent")

    // Sends run one at a time in the order of the taps, so the last rating is the one kept (a Mutex resumes waiters in order)
    private val sendLock = Mutex()

    val viewState: StateFlow<RatingViewState> = componentState

    // Tapping the thumb already picked changes nothing and sends nothing
    fun onThumbClick(thumb: Thumb) {
        val step = componentState.value.step
        if (step.rating?.thumb == thumb) return
        updateRating(Rating(thumb))
        // The reasons come below the thumbs, so a collapsed sheet opens to show them
        if (host.sheetHeight == SheetHeight.Collapsed) host.requestSheetHeight(SheetHeight.Half)
        sendRating()
        if (step.rating == null) offerReminder()
    }

    // Only the reasons of the picked thumb can be picked
    fun onReasonClick(reason: RatingReason) {
        val rating = componentState.value.step.rating ?: return
        if (reason.thumb != rating.thumb) return
        val reasons = if (reason in rating.reasons) rating.reasons - reason else rating.reasons + reason
        updateRating(Rating(rating.thumb, reasons))
        sendRating()
    }

    // Marked not driven, the question is already cleared, so the step closes without a skip
    fun onNotDriven() {
        val driveId = componentState.value.step.question.driveId
        // In AppWork, as the step pops right after and the removal has to be sent through that
        appWork.launch {
            sendLock.withLock {
                runCatching { markNotDrivenUseCase.build(driveId) }.onFailure { error ->
                    // Offline, the removal is sent later, so it isn't logged
                    if (error !is OfflineException) logger.e(error) { "Drive not marked as not driven" }
                }
            }
        }
        host.onStepClosed()
    }

    // Hidden at once, so the offer doesn't wait for the settings to open
    fun onReminderOfferClick() {
        updateStep { it.copy(showsReminderOffer = false) }
        // In AppWork, as the step may pop before the settings are opened
        appWork.launch {
            runCatching { turnOnRemindersUseCase.build(Unit) }.onFailure { error ->
                logger.e(error) { "Reminders not turned on" }
            }
        }
    }

    // A step closed without a thumb drops the drive's question, so it isn't asked about again
    fun close() {
        val step = componentState.value.step
        if (step.rating == null) skipDriveQuestion(step.question.driveId)
        host.onStepClosed()
    }

    // Asked after the first thumb only: the offer is for the user who has just rated a drive
    private fun offerReminder() = shouldOfferReminderUseCase.execute {
        onSuccess { shouldOffer -> if (shouldOffer) showReminderOffer() }
        onError { error -> logger.e(error) { "Reminder offer not read" } }
    }

    private fun showReminderOffer() = updateStep { it.copy(showsReminderOffer = true) }

    private fun updateRating(rating: Rating) = updateStep { it.copy(rating = rating) }

    // In AppWork, so a send isn't cancelled when the step closes right after a thumb; each send runs as it's made
    private fun sendRating() {
        val step = componentState.value.step
        val rating = step.rating ?: return
        appWork.launch {
            sendLock.withLock {
                runCatching { rateDriveUseCase.build(RateDriveUseCase.Args(driveId = step.question.driveId, rating = rating)) }
                    .onFailure { error ->
                        // Offline, the rating is sent later, so it isn't logged
                        if (error !is OfflineException) logger.e(error) { "Rating not sent" }
                    }
            }
        }
    }

    // In AppWork, as the step pops right after and the question has to be dropped through that
    private fun skipDriveQuestion(driveId: String) = appWork.launch {
        sendLock.withLock {
            runCatching { skipDriveQuestionUseCase.build(driveId) }.onFailure { error ->
                logger.e(error) { "Drive question not skipped" }
            }
        }
    }

    private fun updateStep(change: (RatingStep) -> RatingStep) {
        componentState.update { state -> state.copy(step = change(state.step)) }
    }
}
