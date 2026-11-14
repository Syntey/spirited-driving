package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.RecentPlaces
import org.koin.core.annotation.Factory

/**
 * The places the user chose last, last used first.
 */
@Factory
internal class GetRecentPlacesUseCase(private val recentPlaces: RecentPlaces) : UseCase<Unit, List<Place>> {

    override suspend fun build(args: Unit) = recentPlaces.get()
}
