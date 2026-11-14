package com.syntey.spiriteddriving.core.domain.drive

/**
 * [StartedDrives] that keeps [drives] in memory, with the same limit on the number kept.
 */
class FakeStartedDrives(drives: List<Drive> = emptyList()) : StartedDrives {

    var drives: List<Drive> = drives
        private set

    override suspend fun all(): List<Drive> = drives

    override suspend fun add(drive: Drive) {
        drives = (listOf(drive) + drives.filterNot { it.id == drive.id }).take(MAX_KEPT)
    }

    override suspend fun markRecorded(id: String) {
        drives = drives.map { if (it.id == id) it.copy(isRecorded = true) else it }
    }

    override suspend fun remove(id: String) {
        drives = drives.filterNot { it.id == id }
    }

    private companion object {
        const val MAX_KEPT = 100
    }
}
