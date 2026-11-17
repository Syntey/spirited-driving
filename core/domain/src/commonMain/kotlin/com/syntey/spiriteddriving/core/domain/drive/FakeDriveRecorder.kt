package com.syntey.spiriteddriving.core.domain.drive

/**
 * [DriveRecorder] that records the drives it got and the drives it removed, and does nothing else.
 */
class FakeDriveRecorder : DriveRecorder {

    val recorded: List<Drive>
        field = mutableListOf<Drive>()

    val removed: List<String>
        field = mutableListOf<String>()

    override suspend fun record(drive: Drive) {
        recorded += drive
    }

    override suspend fun remove(id: String) {
        removed += id
    }
}
