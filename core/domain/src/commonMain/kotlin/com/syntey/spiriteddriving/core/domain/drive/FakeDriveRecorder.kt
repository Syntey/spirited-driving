package com.syntey.spiriteddriving.core.domain.drive

/**
 * [DriveRecorder] that records the drives it got and does nothing else.
 */
class FakeDriveRecorder : DriveRecorder {

    val recorded: List<Drive>
        field = mutableListOf<Drive>()

    override suspend fun record(drive: Drive) {
        recorded += drive
    }
}
