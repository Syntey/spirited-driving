package com.syntey.spiriteddriving.data.network.plan

import com.syntey.spiriteddriving.core.domain.route.RoutePlanner
import com.syntey.spiriteddriving.core.domain.route.TripPlan
import com.syntey.spiriteddriving.core.domain.route.TripRequest
import com.syntey.spiriteddriving.data.network.api.SpiritedDrivingApi
import com.syntey.spiriteddriving.data.network.result.NetworkError
import com.syntey.spiriteddriving.data.network.result.getOrThrowDomainError
import org.koin.core.annotation.Factory

@Factory
internal class ApiRoutePlanner(private val api: SpiritedDrivingApi) : RoutePlanner {

    override suspend fun plan(request: TripRequest): TripPlan =
        api.planTrip(request.toDto()).getOrThrowDomainError(NetworkError.HttpError::toPlanError).toDomain()
}
