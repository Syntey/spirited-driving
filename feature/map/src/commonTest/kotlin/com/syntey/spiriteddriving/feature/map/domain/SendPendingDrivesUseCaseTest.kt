package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.domain.drive.Drive
import com.syntey.spiriteddriving.core.domain.drive.DriveNotRatableException
import com.syntey.spiriteddriving.core.domain.drive.DriveNotRecordableException
import com.syntey.spiriteddriving.core.domain.drive.DriveRecorder
import com.syntey.spiriteddriving.core.domain.drive.FakeDriveRater
import com.syntey.spiriteddriving.core.domain.drive.FakeStartedDrives
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.error.OfflineException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class SendPendingDrivesUseCaseTest {

    private val at = Instant.parse("2026-11-13T10:00:00Z")
    private val rating = Rating(Thumb.Up)

    @Test
    fun `records every unrecorded drive oldest first and marks it recorded`() = runTest {
        val startedDrives = FakeStartedDrives(listOf(drive("newest"), drive("older"), drive("recorded", recorded = true)))
        val recorder = ScriptedDriveRecorder()

        useCase(startedDrives, recorder).build(Unit)

        assertEquals(listOf("older", "newest"), recorder.attempts)
        assertTrue(startedDrives.all().all { it.isRecorded })
    }

    @Test
    fun `drives that are recorded already are not sent again`() = runTest {
        val recorder = ScriptedDriveRecorder()

        useCase(FakeStartedDrives(listOf(drive("recorded", recorded = true))), recorder).build(Unit)

        assertTrue(recorder.attempts.isEmpty())
    }

    @Test
    fun `a drive the backend won't take is removed and the rest are still recorded`() = runTest {
        val startedDrives = FakeStartedDrives(listOf(drive("newest"), drive("refused")))
        val recorder = ScriptedDriveRecorder(failures = mapOf("refused" to DriveNotRecordableException()))

        useCase(startedDrives, recorder).build(Unit)

        assertEquals(listOf("newest"), startedDrives.all().map { it.id })
        assertTrue(startedDrives.all().single().isRecorded)
    }

    @Test
    fun `without a connection the drives after the failing one wait for the next try`() = runTest {
        val startedDrives = FakeStartedDrives(listOf(drive("newest"), drive("oldest")))
        val recorder = ScriptedDriveRecorder(failures = mapOf("oldest" to OfflineException(cause = null)))

        useCase(startedDrives, recorder).build(Unit)

        assertEquals(listOf("oldest"), recorder.attempts)
        assertTrue(startedDrives.all().none { it.isRecorded })
    }

    @Test
    fun `a drive that fails otherwise stays unrecorded for the next try`() = runTest {
        val startedDrives = FakeStartedDrives(listOf(drive("failing")))
        val recorder = ScriptedDriveRecorder(failures = mapOf("failing" to IllegalStateException("server error")))

        useCase(startedDrives, recorder).build(Unit)

        assertFalse(startedDrives.all().single().isRecorded)
    }

    @Test
    fun `a drive that fails otherwise doesn't stop the drives after it`() = runTest {
        val startedDrives = FakeStartedDrives(listOf(drive("newest"), drive("failing")))
        val recorder = ScriptedDriveRecorder(failures = mapOf("failing" to IllegalStateException("server error")))

        useCase(startedDrives, recorder).build(Unit)

        assertEquals(listOf("failing", "newest"), recorder.attempts)
        assertEquals(listOf(true, false), startedDrives.all().map { it.isRecorded })
    }

    @Test
    fun `a recorded drive with an unsent rating gets it sent and marked sent`() = runTest {
        val startedDrives = FakeStartedDrives(listOf(drive("rated", recorded = true, rating = rating)))
        val rater = FakeDriveRater()

        useCase(startedDrives, ScriptedDriveRecorder(), rater).build(Unit)

        assertEquals(listOf("rated" to rating), rater.rated)
        assertTrue(startedDrives.all().single().isRatingSent)
    }

    @Test
    fun `a recorded drive that is not driven is removed on the backend and locally`() = runTest {
        val startedDrives = FakeStartedDrives(listOf(drive("gone", recorded = true, isNotDriven = true)))
        val recorder = ScriptedDriveRecorder()

        useCase(startedDrives, recorder).build(Unit)

        assertEquals(listOf("gone"), recorder.removed)
        assertTrue(startedDrives.all().isEmpty())
    }

    @Test
    fun `an unrecorded drive that is not driven is removed locally without any backend call`() = runTest {
        val startedDrives = FakeStartedDrives(listOf(drive("gone", isNotDriven = true)))
        val recorder = ScriptedDriveRecorder()
        val rater = FakeDriveRater()

        useCase(startedDrives, recorder, rater).build(Unit)

        assertTrue(recorder.attempts.isEmpty())
        assertTrue(recorder.removed.isEmpty())
        assertTrue(rater.rated.isEmpty())
        assertTrue(startedDrives.all().isEmpty())
    }

    @Test
    fun `a drive whose rating is not ratable is marked sent`() = runTest {
        val startedDrives = FakeStartedDrives(listOf(drive("refused", recorded = true, rating = rating)))
        val rater = FakeDriveRater().apply { failWith = DriveNotRatableException() }

        useCase(startedDrives, ScriptedDriveRecorder(), rater).build(Unit)

        assertTrue(startedDrives.all().single().isRatingSent)
    }

    private fun useCase(
        startedDrives: FakeStartedDrives,
        recorder: ScriptedDriveRecorder,
        rater: FakeDriveRater = FakeDriveRater(),
    ) = SendPendingDrivesUseCase(startedDrives, DriveSender(startedDrives, recorder, rater))

    private fun drive(
        id: String,
        recorded: Boolean = false,
        rating: Rating? = null,
        isNotDriven: Boolean = false,
    ) = Drive(
        id = id,
        routeId = "route-1",
        startedAt = at + 1.minutes,
        plannedDuration = 30.minutes,
        isRecorded = recorded,
        rating = rating,
        isNotDriven = isNotDriven,
    )

    private class ScriptedDriveRecorder(private val failures: Map<String, Throwable> = emptyMap()) : DriveRecorder {
        val attempts = mutableListOf<String>()
        val removed = mutableListOf<String>()

        override suspend fun record(drive: Drive) {
            attempts += drive.id
            failures[drive.id]?.let { throw it }
        }

        override suspend fun remove(id: String) {
            removed += id
        }
    }
}
