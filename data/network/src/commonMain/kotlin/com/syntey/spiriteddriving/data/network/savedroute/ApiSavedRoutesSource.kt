package com.syntey.spiriteddriving.data.network.savedroute

import com.syntey.spiriteddriving.core.domain.route.Route
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteNotFoundException
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoutesSource
import com.syntey.spiriteddriving.data.network.api.SpiritedDrivingApi
import com.syntey.spiriteddriving.data.network.dto.savedroute.RenameSavedRouteRequestDto
import com.syntey.spiriteddriving.data.network.dto.savedroute.SaveRouteRequestDto
import com.syntey.spiriteddriving.data.network.plan.toDomain
import com.syntey.spiriteddriving.data.network.result.NetworkError
import com.syntey.spiriteddriving.data.network.result.NetworkResult
import com.syntey.spiriteddriving.data.network.result.getOrThrowDomainError
import io.ktor.http.HttpStatusCode
import org.koin.core.annotation.Factory

@Factory
internal class ApiSavedRoutesSource(private val api: SpiritedDrivingApi) : SavedRoutesSource {

    override suspend fun list(): List<SavedRoute> =
        api.listSavedRoutes().getOrThrowDomainError().items.map { it.toDomain() }

    override suspend fun save(routeId: String, name: String): SavedRoute =
        api.saveRoute(SaveRouteRequestDto(routeId = routeId, name = name)).getOrThrowDomainError().toDomain()

    override suspend fun getRoute(id: SavedRouteId, maxHandOffWaypoints: Int): Route {
        val result = api.getSavedRoute(savedRouteId = id.value, maxHandOffWaypoints = maxHandOffWaypoints)
        result.throwIfNotFound()
        return result.getOrThrowDomainError().route.toDomain()
    }

    override suspend fun rename(id: SavedRouteId, name: String): SavedRoute {
        val result = api.renameSavedRoute(savedRouteId = id.value, request = RenameSavedRouteRequestDto(name = name))
        result.throwIfNotFound()
        return result.getOrThrowDomainError().toDomain()
    }

    override suspend fun delete(id: SavedRouteId) {
        api.deleteSavedRoute(savedRouteId = id.value).getOrThrowDomainError()
    }

    // Matched by status: the backend's 404 may come without a Problem code, which getOrThrowDomainError needs
    private fun NetworkResult<*>.throwIfNotFound() {
        val error = (this as? NetworkResult.Failure)?.error
        if (error is NetworkError.HttpError && error.statusCode == HttpStatusCode.NotFound.value) {
            throw SavedRouteNotFoundException(error)
        }
    }
}
