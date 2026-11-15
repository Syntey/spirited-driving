package com.syntey.spiriteddriving.core.domain.drive

import com.syntey.spiriteddriving.core.domain.error.OfflineException

/**
 * Gives the signed-in user the drives recorded on this phone while signed out. Repeating it for the same drives is safe.
 * Throws [OfflineException] without a connection.
 */
interface DriveClaimer {

    suspend fun claim(driveIds: List<String>)
}
