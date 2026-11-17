package com.syntey.spiriteddriving.core.domain.drive

/** The one [DriveQuestion] kept on the device, if any. */
interface OpenDriveQuestion {

    suspend fun get(): DriveQuestion?

    /** Keeps [question], replacing the one kept. */
    suspend fun set(question: DriveQuestion)

    /** Drops the kept question if it's about [driveId], so ending an older question never drops a newer one. */
    suspend fun clear(driveId: String)
}
