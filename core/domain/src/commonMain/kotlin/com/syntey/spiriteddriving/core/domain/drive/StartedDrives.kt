package com.syntey.spiriteddriving.core.domain.drive

/**
 * Drives started on this phone, kept on the device: to continue a drive, to record it once online, and, while signed out,
 * so the user can claim them after signing in.
 */
interface StartedDrives {

    /** Newest first. */
    suspend fun all(): List<Drive>

    /** Adds [drive] as the newest; keeps at most a fixed number, dropping the oldest. */
    suspend fun add(drive: Drive)

    suspend fun markRecorded(id: String)

    suspend fun remove(id: String)

    /** Sets the drive's rating, not yet sent. */
    suspend fun rate(id: String, rating: Rating)

    /** Marks [rating] as sent, unless the drive's rating changed meanwhile. */
    suspend fun markRatingSent(id: String, rating: Rating)

    /** Marks the drive as not driven, to be removed from the backend. */
    suspend fun markNotDriven(id: String)
}
