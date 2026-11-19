package com.syntey.spiriteddriving.feature.routes.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.drive.TripHistoryCache
import com.syntey.spiriteddriving.core.domain.drive.TripHistorySource
import com.syntey.spiriteddriving.feature.routes.tools.Constants
import org.koin.core.annotation.Factory

/** Adds the page after the kept drives to the trip history. Does nothing when the last page is kept. Throws what the source throws. */
@Factory
internal class LoadMoreTripHistoryUseCase(
    private val tripHistorySource: TripHistorySource,
    private val tripHistoryCache: TripHistoryCache,
) : UseCase<Unit, Unit> {

    override suspend fun build(args: Unit) {
        val cursor = tripHistoryCache.get()?.nextCursor ?: return
        tripHistoryCache.append(tripHistorySource.list(cursor = cursor, limit = Constants.TripHistory.PAGE_SIZE))
    }
}
