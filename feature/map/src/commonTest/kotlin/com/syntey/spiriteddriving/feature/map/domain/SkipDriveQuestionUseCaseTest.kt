package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.FakeOpenDriveQuestion
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class SkipDriveQuestionUseCaseTest {

    private val route = FakeRoutePlanner.Recorded.funRoutes.first()

    @Test
    fun `skipping clears the question about that drive`() = runTest {
        val questions = FakeOpenDriveQuestion(
            DriveQuestion(
                driveId = "drive-1",
                startedAt = Instant.parse("2026-11-13T10:00:00Z"),
                routeNumber = 1,
                start = route.start.location,
                destination = route.destination,
                duration = route.duration,
                distance = route.distance,
                line = route.line,
                attribution = route.attribution,
            ),
        )

        SkipDriveQuestionUseCase(questions, DriveReminders(FakePlatformDriveReminders())).build("drive-1")

        assertNull(questions.question)
    }

    @Test
    fun `skipping cancels the drive's notification`() = runTest {
        val reminders = FakePlatformDriveReminders()

        SkipDriveQuestionUseCase(FakeOpenDriveQuestion(), DriveReminders(reminders)).build("drive-1")

        assertEquals(listOf("drive-1"), reminders.cancelled)
    }
}
