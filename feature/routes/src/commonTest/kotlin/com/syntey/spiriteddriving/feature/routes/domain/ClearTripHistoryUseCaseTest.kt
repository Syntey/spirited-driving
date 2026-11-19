package com.syntey.spiriteddriving.feature.routes.domain

import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.FakeOpenDriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistoryCache
import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistorySource
import com.syntey.spiriteddriving.core.domain.drive.PastDrivePage
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClearTripHistoryUseCaseTest {

    private val drive = FakeTripHistorySource.Recorded
    private val route = FakeRoutePlanner.Recorded.funRoutes.first()

    @Test
    fun `all drives are removed from the backend and the device keeps an empty page`() = runTest {
        val source = FakeTripHistorySource(drives = listOf(drive))
        val cache = FakeTripHistoryCache(page = PastDrivePage(drives = listOf(drive), nextCursor = null))

        useCase(source = source, cache = cache).build(Unit)

        assertTrue(source.drives.isEmpty())
        assertEquals(PastDrivePage(drives = emptyList(), nextCursor = null), cache.get())
    }

    @Test
    fun `clearing clears the open question and cancels its notification`() = runTest {
        val questions = FakeOpenDriveQuestion(question())
        val reminders = FakePlatformDriveReminders()

        useCase(questions = questions, reminders = reminders).build(Unit)

        assertNull(questions.question)
        assertEquals(listOf(drive.id), reminders.cancelled)
    }

    @Test
    fun `clearing without an open question cancels no notification`() = runTest {
        val reminders = FakePlatformDriveReminders()

        useCase(reminders = reminders).build(Unit)

        assertTrue(reminders.cancelled.isEmpty())
    }

    @Test
    fun `without a connection the history, the question and the notification stay as they were`() = runTest {
        val source = FakeTripHistorySource(drives = listOf(drive)).apply { error = OfflineException(null) }
        val kept = PastDrivePage(drives = listOf(drive), nextCursor = null)
        val cache = FakeTripHistoryCache(page = kept)
        val questions = FakeOpenDriveQuestion(question())
        val reminders = FakePlatformDriveReminders()

        assertFailsWith<OfflineException> {
            useCase(source = source, cache = cache, questions = questions, reminders = reminders).build(Unit)
        }

        assertEquals(kept, cache.get())
        assertEquals(drive.id, questions.question?.driveId)
        assertTrue(reminders.cancelled.isEmpty())
    }

    private fun useCase(
        source: FakeTripHistorySource = FakeTripHistorySource(drives = listOf(drive)),
        cache: FakeTripHistoryCache = FakeTripHistoryCache(),
        questions: FakeOpenDriveQuestion = FakeOpenDriveQuestion(),
        reminders: FakePlatformDriveReminders = FakePlatformDriveReminders(),
    ) = ClearTripHistoryUseCase(source, cache, questions, DriveReminders(reminders))

    private fun question() = DriveQuestion(
        driveId = drive.id,
        startedAt = FakeTripHistorySource.STARTED_AT,
        routeNumber = 1,
        start = route.start.location,
        destination = route.destination,
        duration = route.duration,
        distance = route.distance,
        line = route.line,
        attribution = route.attribution,
    )
}
