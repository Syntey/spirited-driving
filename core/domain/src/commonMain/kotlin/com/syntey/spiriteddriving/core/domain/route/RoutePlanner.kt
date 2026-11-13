package com.syntey.spiriteddriving.core.domain.route

import com.syntey.spiriteddriving.core.domain.error.OfflineException

/**
 * Plans trips on the backend. Throws [OfflineException] without a connection and [OutsideCoverageException] when the start
 * or destination is outside the covered area.
 */
interface RoutePlanner {

    suspend fun plan(request: TripRequest): TripPlan
}
