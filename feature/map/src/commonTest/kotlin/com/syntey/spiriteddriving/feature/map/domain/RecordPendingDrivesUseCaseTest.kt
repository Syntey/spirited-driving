package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.drive.Drive
import com.syntey.spiriteddriving.core.domain.drive.DriveNotRecordableException
import com.syntey.spiriteddriving.core.domain.drive.DriveRecorder
import com.syntey.spiriteddriving.core.domain.drive.FakeStartedDrives
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class RecordPendingDrivesUseCaseTest {

    private val at = Instant.parse("2026-11-13T10:00:00Z")

    @Test
    fun `records every unrecorded drive oldest first and marks it recorded`() = runTest {
        val startedDrives = FakeStartedDrives(listOf(drive("newest"), drive("older"), drive("recorded", recorded = true)))
        val recorder = ScriptedDriveRecorder()

        RecordPendingDrivesUseCase(startedDrives, recorder).build(Unit)

        assertEquals(listOf("older", "newest"), recorder.attempts)
        assertTrue(startedDrives.all().all { it.isRecorded })
    }

    @Test
    fun `drives that are recorded already are not sent again`() = runTest {
        val recorder = ScriptedDriveRecorder()

        RecordPendingDrivesUseCase(FakeStartedDrives(listOf(drive("recorded", recorded = true))), recorder).build(Unit)

        assertTrue(recorder.attempts.isEmpty())
    }

    @Test
    fun `a drive the backend won't take is removed and the rest are still recorded`() = runTest {
        val startedDrives = FakeStartedDrives(listOf(drive("newest"), drive("refused")))
        val recorder = ScriptedDriveRecorder(failures = mapOf("refused" to DriveNotRecordableException()))

        RecordPendingDrivesUseCase(startedDrives, recorder).build(Unit)

        assertEquals(listOf("newest"), startedDrives.all().map { it.id })
        assertTrue(startedDrives.all().single().isRecorded)
    }

    @Test
    fun `without a connection the drives after the failing one wait for the next try`() = runTest {
        val startedDrives = FakeStartedDrives(listOf(drive("newest"), drive("oldest")))
        val recorder = ScriptedDriveRecorder(failures = mapOf("oldest" to OfflineException(cause = null)))

        RecordPendingDrivesUseCase(startedDrives, recorder).build(Unit)

        assertEquals(listOf("oldest"), recorder.attempts)
        assertTrue(startedDrives.all().none { it.isRecorded })
    }

    @Test
    fun `a drive that fails otherwise stays unrecorded for the next try`() = runTest {
        val startedDrives = FakeStartedDrives(listOf(drive("failing")))
        val recorder = ScriptedDriveRecorder(failures = mapOf("failing" to IllegalStateException("server error")))

        RecordPendingDrivesUseCase(startedDrives, recorder).build(Unit)

        assertFalse(startedDrives.all().single().isRecorded)
    }

    @Test
    fun `a drive that fails otherwise doesn't stop the drives after it`() = runTest {
        val startedDrives = FakeStartedDrives(listOf(drive("newest"), drive("failing")))
        val recorder = ScriptedDriveRecorder(failures = mapOf("failing" to IllegalStateException("server error")))

        RecordPendingDrivesUseCase(startedDrives, recorder).build(Unit)

        assertEquals(listOf("failing", "newest"), recorder.attempts)
        assertEquals(listOf(true, false), startedDrives.all().map { it.isRecorded })
    }

    private fun drive(id: String, recorded: Boolean = false) = Drive(
        id = id,
        routeId = "route-1",
        startedAt = at + 1.minutes,
        plannedDuration = 30.minutes,
        isRecorded = recorded,
    )

    private class ScriptedDriveRecorder(private val failures: Map<String, Throwable> = emptyMap()) : DriveRecorder {
        val attempts = mutableListOf<String>()

        override suspend fun record(drive: Drive) {
            attempts += drive.id
            failures[drive.id]?.let { throw it }
        }
    }
}
