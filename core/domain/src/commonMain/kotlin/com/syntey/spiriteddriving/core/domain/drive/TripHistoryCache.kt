package com.syntey.spiriteddriving.core.domain.drive

import com.syntey.spiriteddriving.core.domain.route.Route
import kotlinx.coroutines.flow.Flow

/**
 * The signed-in user's trip history kept on the device, as far as it was loaded, so the list shows at once and its drives
 * can be opened and driven offline. Ratings made in Trip history are kept until the backend has them. Cleared on every
 * account change.
 */
interface TripHistoryCache {

    /** The kept drives, newest first, now and after each change; `null` while nothing was kept yet or it can't be read. */
    fun observe(): Flow<PastDrivePage?>

    /** The kept drives, newest first, or `null` while nothing was kept yet or it can't be read. */
    suspend fun get(): PastDrivePage?

    /**
     * Keeps [page], the first page, in place of what's kept. Drops the full routes of drives no longer in it. A rating
     * not sent yet stays on its drive.
     */
    suspend fun replaceAll(page: PastDrivePage)

    /**
     * Adds [page], the page after the kept ones, at the end, and keeps its `nextCursor`. A drive already kept isn't added
     * twice. Unsent ratings stay on their drives, as in replaceAll.
     */
    suspend fun append(page: PastDrivePage)

    /** Removes the drive, its full route and its unsent rating. */
    suspend fun remove(id: String)

    /** Sets the kept drive's rating, and keeps it as not sent yet. */
    suspend fun rate(id: String, rating: Rating)

    /** The ratings not sent yet, by drive ID. */
    suspend fun unsentRatings(): Map<String, Rating>

    /** Marks [rating] as sent, unless the drive's rating changed meanwhile. */
    suspend fun markRatingSent(id: String, rating: Rating)

    /** The full route kept for the drive [id], or `null` when there's none or it can't be read. */
    suspend fun getRoute(id: String): Route?

    /**
     * Keeps [route] as the full route of the drive [id], also when the drive isn't in the kept list yet (a drive just
     * started on this phone).
     */
    suspend fun putRoute(id: String, route: Route)

    /** Removes everything kept. */
    suspend fun clear()
}
