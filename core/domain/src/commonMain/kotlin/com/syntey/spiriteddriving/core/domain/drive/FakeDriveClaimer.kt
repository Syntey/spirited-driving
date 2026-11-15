package com.syntey.spiriteddriving.core.domain.drive

/**
 * [DriveClaimer] that records the claims it got. Throws [failWith] instead of accepting them, when it's set.
 */
class FakeDriveClaimer : DriveClaimer {

    val claimed: List<List<String>>
        field = mutableListOf<List<String>>()

    var failWith: Throwable? = null

    override suspend fun claim(driveIds: List<String>) {
        claimed += driveIds
        failWith?.let { throw it }
    }
}
