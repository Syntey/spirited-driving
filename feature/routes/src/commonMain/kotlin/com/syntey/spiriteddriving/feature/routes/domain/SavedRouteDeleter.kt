package com.syntey.spiriteddriving.feature.routes.domain

import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoutesCache
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoutesSource
import com.syntey.spiriteddriving.core.domain.work.AppWork
import org.koin.core.annotation.Factory

/**
 * Sends the deletes of saved routes in [AppWork], for a list that closes while their Undo is open or while they're
 * being sent: they still go through. A delete that fails leaves the route, so the list shows it again.
 */
@Factory
internal class SavedRouteDeleter(
    private val appWork: AppWork,
    private val savedRoutesSource: SavedRoutesSource,
    private val savedRoutesCache: SavedRoutesCache,
) {

    fun deleteLater(ids: Collection<SavedRouteId>) {
        ids.forEach { id ->
            appWork.launch {
                savedRoutesSource.delete(id)
                savedRoutesCache.remove(id)
            }
        }
    }
}
