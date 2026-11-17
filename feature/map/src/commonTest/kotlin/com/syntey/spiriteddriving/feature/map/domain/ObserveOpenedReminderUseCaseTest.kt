package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.FakeOpenDriveQuestion
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class ObserveOpenedReminderUseCaseTest {

    private val at = Instant.parse("2026-11-13T10:00:00Z")
    private val route = FakeRoutePlanner.Recorded.funRoutes.first()

    @Test
    fun `a tap on the open drive's notification emits its question`() = runTest {
        val reminders = FakePlatformDriveReminders()
        val opened = mutableListOf<DriveQuestion>()
        val collecting = launch(UnconfinedTestDispatcher(testScheduler)) {
            useCase(reminders, FakeOpenDriveQuestion(question("drive-1"))).build(Unit).collect { opened += it }
        }

        reminders.tap("drive-1")
        runCurrent()
        collecting.cancel()

        assertEquals(listOf("drive-1"), opened.map { it.driveId })
    }

    @Test
    fun `a tap on an older drive's notification emits nothing`() = runTest {
        val reminders = FakePlatformDriveReminders()
        val opened = mutableListOf<DriveQuestion>()
        val collecting = launch(UnconfinedTestDispatcher(testScheduler)) {
            useCase(reminders, FakeOpenDriveQuestion(question("drive-2"))).build(Unit).collect { opened += it }
        }

        reminders.tap("drive-1")
        runCurrent()
        collecting.cancel()

        assertEquals(emptyList<DriveQuestion>(), opened)
    }

    private fun useCase(reminders: FakePlatformDriveReminders, questions: FakeOpenDriveQuestion) =
        ObserveOpenedReminderUseCase(DriveReminders(reminders), questions)

    private fun question(driveId: String) = DriveQuestion(
        driveId = driveId,
        startedAt = at,
        routeNumber = 1,
        start = route.start.location,
        destination = route.destination,
        duration = route.duration,
        distance = route.distance,
        line = route.line,
        attribution = route.attribution,
    )
}
