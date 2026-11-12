package com.syntey.spiriteddriving.core.domain.route

import com.syntey.spiriteddriving.core.domain.error.OfflineException

/**
 * Plans trips on the backend. Throws [OfflineException] without a connection.
 */
interface RoutePlanner {

    suspend fun plan(request: TripRequest): TripPlan
}
