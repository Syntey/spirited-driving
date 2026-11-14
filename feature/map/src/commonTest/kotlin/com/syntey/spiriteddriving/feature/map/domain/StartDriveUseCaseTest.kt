package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.drive.Drive
import com.syntey.spiriteddriving.core.domain.drive.DriveNotRecordableException
import com.syntey.spiriteddriving.core.domain.drive.DriveRecorder
import com.syntey.spiriteddriving.core.domain.drive.FakeStartedDrives
import com.syntey.spiriteddriving.core.domain.drive.StartedDrives
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.domain.route.Route
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class StartDriveUseCaseTest {

    private val route = FakeRoutePlanner.Recorded.fastest
    private val otherRoute = FakeRoutePlanner.Recorded.funRoutes.first()
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

    private suspend fun startDrive(route: Route, at: Instant, startedDrives: StartedDrives, recorder: DriveRecorder): Drive =
        StartDriveUseCase(startedDrives, recorder).build(StartDriveUseCase.Args(route = route, at = at))

    private class ScriptedDriveRecorder(private val failure: Throwable? = null) : DriveRecorder {
        val recorded = mutableListOf<Drive>()

        override suspend fun record(drive: Drive) {
            failure?.let { throw it }
            recorded += drive
        }
    }
}
