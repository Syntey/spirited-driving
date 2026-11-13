package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.RoutePlanner
import com.syntey.spiriteddriving.core.domain.route.TripPlan
import com.syntey.spiriteddriving.core.domain.route.TripRequest
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.settings.VignetteSettings
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import com.syntey.spiriteddriving.feature.map.tools.Constants

/**
 * Plans the routes from [Args.start] to [Args.destination] within [Args.budget]. Throws `OfflineException` without a
 * connection.
 */
@Factory
internal class PlanTripUseCase(private val routePlanner: RoutePlanner, private val vignetteSettings: VignetteSettings) :
    UseCase<PlanTripUseCase.Args, TripPlan> {

    data class Args(val start: Place, val destination: Place, val budget: Budget)

    override suspend fun build(args: Args): TripPlan = routePlanner.plan(
        TripRequest(
            start = args.start,
            destination = args.destination,
            budget = args.budget,
            vignettes = vignetteSettings.observeVignettes().first(),
            maxHandOffWaypoints = Constants.Trip.HAND_OFF_WAYPOINTS,
        ),
    )
}
