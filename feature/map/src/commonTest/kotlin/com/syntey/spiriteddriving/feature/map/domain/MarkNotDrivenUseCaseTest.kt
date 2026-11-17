package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.drive.Drive
import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.DriveRecorder
import com.syntey.spiriteddriving.core.domain.drive.FakeDriveRater
import com.syntey.spiriteddriving.core.domain.drive.FakeDriveRecorder
import com.syntey.spiriteddriving.core.domain.drive.FakeOpenDriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.FakeStartedDrives
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class MarkNotDrivenUseCaseTest {

    private val at = Instant.parse("2026-11-13T10:00:00Z")
    private val route = FakeRoutePlanner.Recorded.funRoutes.first()

    @Test
    fun `a recorded drive is removed on the backend and locally`() = runTest {
        val startedDrives = FakeStartedDrives(listOf(drive()))
        val recorder = FakeDriveRecorder()

        useCase(startedDrives, recorder = recorder).build("drive-1")

        assertEquals(listOf("drive-1"), recorder.removed)
        assertTrue(startedDrives.all().isEmpty())
    }

    @Test
    fun `without a connection the drive stays marked not driven and the error is thrown`() = runTest {
        val startedDrives = FakeStartedDrives(listOf(drive()))

        assertFailsWith<OfflineException> {
            useCase(startedDrives, recorder = FailingRemovalRecorder()).build("drive-1")
        }

        assertTrue(startedDrives.all().single().isNotDriven)
    }

    @Test
    fun `marking not driven clears the question about the drive`() = runTest {
        val questions = FakeOpenDriveQuestion(question())

        useCase(FakeStartedDrives(listOf(drive())), questions = questions).build("drive-1")

        assertNull(questions.question)
    }

    @Test
    fun `marking not driven cancels the drive's notification`() = runTest {
        val reminders = FakePlatformDriveReminders()

        useCase(FakeStartedDrives(listOf(drive())), reminders = reminders).build("drive-1")

        assertEquals(listOf("drive-1"), reminders.cancelled)
    }

    private fun useCase(
        startedDrives: FakeStartedDrives,
        recorder: DriveRecorder = FakeDriveRecorder(),
        questions: FakeOpenDriveQuestion = FakeOpenDriveQuestion(),
        reminders: FakePlatformDriveReminders = FakePlatformDriveReminders(),
    ) = MarkNotDrivenUseCase(questions, startedDrives, DriveSender(startedDrives, recorder, FakeDriveRater()), DriveReminders(reminders))

    private fun drive() = Drive(
        id = "drive-1",
        routeId = route.id,
        startedAt = at,
        plannedDuration = route.duration,
        isRecorded = true,
    )

    private fun question() = DriveQuestion(
        driveId = "drive-1",
        startedAt = at,
        routeNumber = 1,
        start = route.start.location,
        destination = route.destination,
        duration = route.duration,
        distance = route.distance,
        line = route.line,
        attribution = route.attribution,
    )

    private class FailingRemovalRecorder : DriveRecorder {
        override suspend fun record(drive: Drive) = Unit

        override suspend fun remove(id: String) = throw OfflineException(cause = null)
    }
}
