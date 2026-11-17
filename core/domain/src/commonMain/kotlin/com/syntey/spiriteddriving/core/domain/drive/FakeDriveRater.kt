package com.syntey.spiriteddriving.core.domain.drive

/**
 * [DriveRater] that records the ratings it got. Throws [failWith] instead of accepting them, when it's set.
 */
class FakeDriveRater : DriveRater {

    val rated: List<Pair<String, Rating>>
        field = mutableListOf<Pair<String, Rating>>()

    var failWith: Throwable? = null

    override suspend fun rate(driveId: String, rating: Rating) {
        rated += driveId to rating
        failWith?.let { throw it }
    }
}
