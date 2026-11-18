package com.syntey.spiriteddriving.core.domain.drive

import com.syntey.spiriteddriving.core.domain.route.Route
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * [TripHistoryCache] that keeps everything in memory, with the same rules for unsent ratings and full routes.
 */
class FakeTripHistoryCache(page: PastDrivePage? = null) : TripHistoryCache {

    private val kept = MutableStateFlow(page)

    var fullRoutes: Map<String, Route> = emptyMap()
        private set

    private var unsent: Map<String, Rating> = emptyMap()

    override fun observe(): Flow<PastDrivePage?> = kept

    override suspend fun get(): PastDrivePage? = kept.value

    override suspend fun replaceAll(page: PastDrivePage) {
        kept.value = PastDrivePage(page.drives.map { it.withUnsent() }, page.nextCursor)
        val ids = page.drives.mapTo(mutableSetOf()) { it.id }
        fullRoutes = fullRoutes.filterKeys { it in ids }
    }

    override suspend fun append(page: PastDrivePage) {
        val current = kept.value
        val keptIds = current?.drives.orEmpty().mapTo(mutableSetOf()) { it.id }
        val added = page.drives.filterNot { it.id in keptIds }
        kept.value = PastDrivePage(
            drives = (current?.drives.orEmpty() + added).map { it.withUnsent() },
            nextCursor = page.nextCursor,
        )
    }

    override suspend fun remove(id: String) {
        kept.value = kept.value?.let { page -> page.copy(drives = page.drives.filterNot { it.id == id }) }
        fullRoutes = fullRoutes - id
        unsent = unsent - id
    }

    override suspend fun rate(id: String, rating: Rating) {
        val current = kept.value ?: return
        if (current.drives.none { it.id == id }) return
        kept.value = current.copy(drives = current.drives.map { if (it.id == id) it.copy(rating = rating) else it })
        unsent = unsent + (id to rating)
    }

    override suspend fun unsentRatings(): Map<String, Rating> = unsent

    override suspend fun markRatingSent(id: String, rating: Rating) {
        if (unsent[id] == rating) unsent = unsent - id
    }

    override suspend fun getRoute(id: String): Route? = fullRoutes[id]

    override suspend fun putRoute(id: String, route: Route) {
        fullRoutes = fullRoutes + (id to route)
    }

    override suspend fun clear() {
        kept.value = null
        fullRoutes = emptyMap()
        unsent = emptyMap()
    }

    private fun PastDrive.withUnsent(): PastDrive = unsent[id]?.let { copy(rating = it) } ?: this
}
