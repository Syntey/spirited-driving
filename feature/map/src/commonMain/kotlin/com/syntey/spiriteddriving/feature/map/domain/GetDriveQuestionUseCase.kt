package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.drive.OpenDriveQuestion
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import com.syntey.spiriteddriving.feature.map.tools.Constants
import kotlin.time.Instant
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

/**
 * Tells how the open question is asked at [Instant]. Nothing is asked before half the planned duration has passed, as the
 * drive is likely not over; the rating step opens until [Constants.DriveFeedback.ASKS_UNTIL_AFTER_ARRIVAL] after the planned
 * arrival, then a reminder bubble shows until [Constants.DriveFeedback.REMINDS_FOR] after the hand-off. After that the
 * question is cleared, with its notification. Returns null when nothing is asked now, or when there is no question.
 */
@Factory
internal class GetDriveQuestionUseCase(
    private val openDriveQuestion: OpenDriveQuestion,
    @Provided private val driveReminders: DriveReminders,
) : UseCase<Instant, DrivePrompt?> {

    override suspend fun build(args: Instant): DrivePrompt? {
        val question = openDriveQuestion.get()
        return when {
            question == null || args < question.startedAt + question.duration / 2 -> null

            args <= question.startedAt + question.duration + Constants.DriveFeedback.ASKS_UNTIL_AFTER_ARRIVAL ->
                DrivePrompt(question, DrivePrompt.Kind.RatingStep)

            args < question.startedAt + Constants.DriveFeedback.REMINDS_FOR -> DrivePrompt(question, DrivePrompt.Kind.Bubble)

            else -> {
                openDriveQuestion.clear(question.driveId)
                driveReminders.cancel(question.driveId)
                null
            }
        }
    }
}
