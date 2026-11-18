package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoutesCache
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoutesSource
import com.syntey.spiriteddriving.core.domain.savedroute.freeNameFor
import com.syntey.spiriteddriving.core.domain.savedroute.hasName
import com.syntey.spiriteddriving.feature.map.tools.Constants
import org.koin.core.annotation.Factory

/**
 * Saves [Args.route] as a saved route. Named after the destination unless [Args.name] is given; without a given name, a
 * taken name asks for another one instead of saving. The full route is kept on the device, so it can be driven offline.
 */
@Factory
internal class SaveRouteUseCase(private val savedRoutesSource: SavedRoutesSource, private val savedRoutesCache: SavedRoutesCache) :
    UseCase<SaveRouteUseCase.Args, SaveRouteResult> {

    /** @property name The name typed in the name dialog; null to name the route after its destination. */
    data class Args(val route: Route, val name: String? = null)

    override suspend fun build(args: Args): SaveRouteResult {
        val known = savedRoutesCache.get() ?: savedRoutesSource.list().also { savedRoutesCache.replaceAll(it) }
        return known.firstOrNull { it.routeId == args.route.id }?.let { SaveRouteResult.Saved(it) } ?: saveNew(args, known)
    }

    private suspend fun saveNew(args: Args, known: List<SavedRoute>): SaveRouteResult {
        val name = args.name?.trim() ?: defaultName(args.route.destination)
        if (args.name == null && known.hasName(name)) {
            return SaveRouteResult.NameTaken(
                takenName = name,
                suggestion = known.freeNameFor(name, args.route.start.shortArea),
                takenNames = known.map { it.name },
            )
        }

        val saved = savedRoutesSource.save(args.route.id, name)
        savedRoutesCache.put(saved)
        savedRoutesCache.putRoute(saved.id, args.route)
        return SaveRouteResult.Saved(saved)
    }

    private fun defaultName(destination: Place): String =
        (destination.name ?: destination.area).orEmpty().trim().take(Constants.SavedRoutes.DEFAULT_NAME_MAX_LENGTH)
}
