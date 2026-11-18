package com.syntey.spiriteddriving.core.domain.drive

import com.syntey.spiriteddriving.core.domain.error.OfflineException
import com.syntey.spiriteddriving.core.domain.route.Route

/** The signed-in user's trip history on the backend. Every call throws [OfflineException] without a connection. */
interface TripHistorySource {

    /** One page of drives, newest first: the first page with a null [cursor], else the page after the one whose `nextCursor` it is. */
    suspend fun list(cursor: String?, limit: Int): PastDrivePage

    /**
     * The drive's full route, with a hand-off for at most [maxHandOffWaypoints] waypoints. Throws
     * [PastDriveNotFoundException] when it's gone.
     */
    suspend fun getRoute(driveId: String, maxHandOffWaypoints: Int): Route

    /** Removes all of the user's drives and their ratings. */
    suspend fun clear()
}
