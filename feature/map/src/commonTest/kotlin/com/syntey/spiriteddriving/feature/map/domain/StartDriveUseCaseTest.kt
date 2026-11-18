package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.drive.Drive
import com.syntey.spiriteddriving.core.domain.drive.DriveNotRecordableException
import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.DriveRecorder
import com.syntey.spiriteddriving.core.domain.drive.FakeOpenDriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.FakeStartedDrives
import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistoryCache
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.StartedDrives
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.platform.binding.DriveReminders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class StartDriveUseCaseTest {

    private val route = FakeRoutePlanner.Recorded.fastest
    private val otherRoute = FakeRoutePlanner.Recorded.funRoutes.first()
    private val funRoute = FakeRoutePlanner.Recorded.funRoutes.first()
    private val at = Instant.parse("2026-11-13T10:00:00Z")
    private val halfDuration = route.duration / 2

    @Test
    fun `a new drive is stored and recorded`() = runTest {
        val startedDrives = FakeStartedDrives()
        val recorder = ScriptedDriveRecorder()

        val drive = startDrive(route, at, startedDrives, recorder)

        assertTrue(drive.isRecorded)
        assertEquals(drive, startedDrives.all().single())
        assertEquals(listOf(drive.id), recorder.recorded.map { it.id })
    }

    @Test
    fun `a new drive keeps the route and the start and the planned duration`() = runTest {
        val drive = startDrive(route, at, FakeStartedDrives(), ScriptedDriveRecorder())

        assertEquals(route.id, drive.routeId)
        assertEquals(at, drive.startedAt)
        assertEquals(route.duration, drive.plannedDuration)
    }

    @Test
    fun `starting the same route again before half its duration continues the drive`() = runTest {
        val startedDrives = FakeStartedDrives()
        val recorder = ScriptedDriveRecorder()
        val first = startDrive(route, at, startedDrives, recorder)

        val second = startDrive(route, at + halfDuration - 1.minutes, startedDrives, recorder)

        assertEquals(first.id, second.id)
        assertEquals(1, startedDrives.all().size)
        assertEquals(1, recorder.recorded.size)
    }

    @Test
    fun `starting the same route at exactly half its duration starts a new drive`() = runTest {
        val startedDrives = FakeStartedDrives()
        val recorder = ScriptedDriveRecorder()
        val first = startDrive(route, at, startedDrives, recorder)

        val second = startDrive(route, at + halfDuration, startedDrives, recorder)

        assertNotEquals(first.id, second.id)
        assertEquals(2, startedDrives.all().size)
    }

    @Test
    fun `starting the same route after half its duration starts a new drive`() = runTest {
        val startedDrives = FakeStartedDrives()
        val recorder = ScriptedDriveRecorder()
        val first = startDrive(route, at, startedDrives, recorder)

        val second = startDrive(route, at + route.duration, startedDrives, recorder)

        assertNotEquals(first.id, second.id)
        assertEquals(2, recorder.recorded.size)
    }

    @Test
    fun `starting another route before half the duration starts a new drive`() = runTest {
        val startedDrives = FakeStartedDrives()
        val recorder = ScriptedDriveRecorder()
        val first = startDrive(route, at, startedDrives, recorder)

        val second = startDrive(otherRoute, at + 1.minutes, startedDrives, recorder)

        assertNotEquals(first.id, second.id)
        assertEquals(otherRoute.id, second.routeId)
    }

    @Test
    fun `a continued drive that isn't recorded yet is recorded`() = runTest {
        val unrecorded = Drive(id = "drive-1", routeId = route.id, startedAt = at, plannedDuration = route.duration)
        val startedDrives = FakeStartedDrives(listOf(unrecorded))
        val recorder = ScriptedDriveRecorder()

        val drive = startDrive(route, at + 1.minutes, startedDrives, recorder)

        assertEquals("drive-1", drive.id)
        assertTrue(drive.isRecorded)
        assertEquals(listOf("drive-1"), recorder.recorded.map { it.id })
        assertTrue(startedDrives.all().single().isRecorded)
    }

    @Test
    fun `a continued drive that is recorded already isn't sent again`() = runTest {
        val recorded = Drive(id = "drive-1", routeId = route.id, startedAt = at, plannedDuration = route.duration, isRecorded = true)
        val recorder = ScriptedDriveRecorder()

        val drive = startDrive(route, at + 1.minutes, FakeStartedDrives(listOf(recorded)), recorder)

        assertEquals(recorded, drive)
        assertTrue(recorder.recorded.isEmpty())
    }

    @Test
    fun `a drive the backend won't take is removed and the error is thrown`() = runTest {
        val startedDrives = FakeStartedDrives()
        val recorder = ScriptedDriveRecorder(failure = DriveNotRecordableException())

        assertFailsWith<DriveNotRecordableException> { startDrive(route, at, startedDrives, recorder) }

        assertTrue(startedDrives.all().isEmpty())
    }

    @Test
    fun `a drive that fails without a connection stays stored unrecorded and the error is thrown`() = runTest {
        val startedDrives = FakeStartedDrives()
        val recorder = ScriptedDriveRecorder(failure = OfflineException(cause = null))

        assertFailsWith<OfflineException> { startDrive(route, at, startedDrives, recorder) }

        val stored = startedDrives.all().single()
        assertFalse(stored.isRecorded)
    }

    @Test
    fun `a new drive on a fun route opens the question with the route and its number`() = runTest {
        val questions = FakeOpenDriveQuestion()
        val drive = startDrive(funRoute, at, FakeStartedDrives(), ScriptedDriveRecorder(), questions, routeNumber = 2)

        val question = assertNotNull(questions.question)
        assertEquals(drive.id, question.driveId)
        assertEquals(at, question.startedAt)
        assertEquals(2, question.routeNumber)
        assertEquals(funRoute.start.location, question.start)
        assertEquals(funRoute.destination, question.destination)
        assertEquals(funRoute.duration, question.duration)
        assertEquals(funRoute.distance, question.distance)
        assertEquals(funRoute.line, question.line)
        assertEquals(funRoute.attribution, question.attribution)
    }

    @Test
    fun `a drive on the fastest route drops the open question`() = runTest {
        val questions = FakeOpenDriveQuestion(question(driveId = "older"))

        startDrive(route, at, FakeStartedDrives(), ScriptedDriveRecorder(), questions, routeNumber = null)

        assertNull(questions.question)
    }

    @Test
    fun `a fun route schedules the notification after the hand-off with the destination's name`() = runTest {
        val reminders = FakePlatformDriveReminders()

        val drive = startDrive(funRoute, at, FakeStartedDrives(), ScriptedDriveRecorder(), reminders = reminders, routeNumber = 1)

        val expectedAt = at + funRoute.duration * 1.2
        assertEquals(
            listOf(FakePlatformDriveReminders.Scheduled(drive.id, funRoute.destination.name, expectedAt.toEpochMilliseconds())),
            reminders.scheduled,
        )
    }

    @Test
    fun `the fastest route schedules no notification and cancels the dropped question's`() = runTest {
        val reminders = FakePlatformDriveReminders()
        val questions = FakeOpenDriveQuestion(question(driveId = "older"))

        startDrive(route, at, FakeStartedDrives(), ScriptedDriveRecorder(), questions, reminders = reminders, routeNumber = null)

        assertTrue(reminders.scheduled.isEmpty())
        assertEquals(listOf("older"), reminders.cancelled)
    }

    @Test
    fun `a continued drive leaves the open question as it is`() = runTest {
        val startedDrives = FakeStartedDrives()
        val questions = FakeOpenDriveQuestion()
        startDrive(funRoute, at, startedDrives, ScriptedDriveRecorder(), questions, routeNumber = 1)
        val kept = questions.question

        startDrive(funRoute, at + 1.minutes, startedDrives, ScriptedDriveRecorder(), questions, routeNumber = 1)

        assertEquals(kept, questions.question)
    }

    @Test
    fun `a new drive on a fun route already rated on a kept drive opens no question`() = runTest {
        val rated = Drive(
            id = "rated",
            routeId = funRoute.id,
            startedAt = at - 1.minutes,
            plannedDuration = funRoute.duration,
            isRecorded = true,
            rating = Rating(Thumb.Down),
            isRatingSent = true,
        )
        val questions = FakeOpenDriveQuestion()

        startDrive(funRoute, at + funRoute.duration, FakeStartedDrives(listOf(rated)), ScriptedDriveRecorder(), questions, routeNumber = 1)

        assertNull(questions.question)
    }

    @Test
    fun `a drive keeps its route for Trip history, also when recording is offline`() = runTest {
        val startedDrives = FakeStartedDrives()
        val cache = FakeTripHistoryCache()

        assertFailsWith<OfflineException> {
            StartDriveUseCase(
                startedDrives,
                ScriptedDriveRecorder(failure = OfflineException(cause = null)),
                FakeOpenDriveQuestion(),
                cache,
                DriveReminders(FakePlatformDriveReminders()),
            ).build(StartDriveUseCase.Args(route = route, routeNumber = null, at = at))
        }

        assertEquals(route, cache.getRoute(startedDrives.all().single().id))
    }

    @Test
    fun `the question is kept when recording is offline`() = runTest {
        val questions = FakeOpenDriveQuestion()

        assertFailsWith<OfflineException> {
            startDrive(
                funRoute,
                at,
                FakeStartedDrives(),
                ScriptedDriveRecorder(failure = OfflineException(cause = null)),
                questions,
                routeNumber = 1,
            )
        }

        assertEquals(1, assertNotNull(questions.question).routeNumber)
    }

    private suspend fun startDrive(
        route: Route,
        at: Instant,
        startedDrives: StartedDrives,
        recorder: DriveRecorder,
        questions: FakeOpenDriveQuestion = FakeOpenDriveQuestion(),
        reminders: FakePlatformDriveReminders = FakePlatformDriveReminders(),
        routeNumber: Int? = null,
    ): Drive = StartDriveUseCase(startedDrives, recorder, questions, FakeTripHistoryCache(), DriveReminders(reminders)).build(
        StartDriveUseCase.Args(route = route, routeNumber = routeNumber, at = at),
    )

    private fun question(driveId: String) = DriveQuestion(
        driveId = driveId,
        startedAt = at,
        routeNumber = 1,
        start = funRoute.start.location,
        destination = funRoute.destination,
        duration = funRoute.duration,
        distance = funRoute.distance,
        line = funRoute.line,
        attribution = funRoute.attribution,
    )

    private class ScriptedDriveRecorder(private val failure: Throwable? = null) : DriveRecorder {
        val recorded = mutableListOf<Drive>()

        override suspend fun record(drive: Drive) {
            failure?.let { throw it }
            recorded += drive
        }

        override suspend fun remove(id: String) = Unit
    }
}
