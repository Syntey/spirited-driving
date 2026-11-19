package com.syntey.spiriteddriving.core.platform.binding

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class DriveRemindersTest {

    @Test
    fun `isOn reports whether the phone delivers the reminders`() = runTest {
        val fake = FakePlatformDriveReminders(isOn = true, canOffer = false)

        assertTrue(DriveReminders(fake).isOn())
    }

    @Test
    fun `canOffer reports whether the app may ask for the permission`() = runTest {
        val fake = FakePlatformDriveReminders(isOn = false, canOffer = true)

        assertTrue(DriveReminders(fake).canOffer())
    }

    @Test
    fun `schedule passes the time as epoch milliseconds`() {
        val fake = FakePlatformDriveReminders(isOn = true, canOffer = true)

        DriveReminders(fake).schedule("drive-1", "Karlštejn", Instant.fromEpochMilliseconds(1_763_550_000_000))

        assertEquals(Triple("drive-1", "Karlštejn", 1_763_550_000_000L), fake.scheduled)
    }

    @Test
    fun `opened emits the IDs of tapped reminders`() = runTest {
        val fake = FakePlatformDriveReminders(isOn = true, canOffer = true)
        val received = mutableListOf<String>()
        val job = launch { DriveReminders(fake).opened.collect { received += it } }
        runCurrent()

        fake.tap("drive-1")
        runCurrent()
        fake.tap("drive-2")
        runCurrent()

        assertEquals(listOf("drive-1", "drive-2"), received)
        job.cancelAndJoin()
    }

    @Test
    fun `stopping the collection removes the open listener`() = runTest {
        val fake = FakePlatformDriveReminders(isOn = true, canOffer = true)
        val job = launch { DriveReminders(fake).opened.collect { } }
        runCurrent()

        job.cancelAndJoin()

        assertNull(fake.openListener)
    }
}

private class FakePlatformDriveReminders(private val isOn: Boolean, private val canOffer: Boolean) : PlatformDriveReminders {

    var scheduled: Triple<String, String?, Long>? = null
        private set

    var openListener: DriveReminderOpenListener? = null
        private set

    override fun isOn(listener: DriveRemindersListener) = listener.onResult(isOn)

    override fun openSettings() = Unit

    override fun schedule(driveId: String, destinationName: String?, atEpochMillis: Long) {
        scheduled = Triple(driveId, destinationName, atEpochMillis)
    }

    override fun cancel(driveId: String) = Unit

    override fun setOpenListener(listener: DriveReminderOpenListener?) {
        openListener = listener
    }

    override fun canOffer(listener: DriveRemindersListener) = listener.onResult(canOffer)

    fun tap(driveId: String) {
        checkNotNull(openListener).onOpen(driveId)
    }
}
