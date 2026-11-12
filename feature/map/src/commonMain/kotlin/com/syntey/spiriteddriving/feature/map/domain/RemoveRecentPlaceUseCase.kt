package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.RecentPlaces
import org.koin.core.annotation.Factory

/**
 * Removes [Place] from the recent places. Returns the new list.
 */
@Factory
internal class RemoveRecentPlaceUseCase(private val recentPlaces: RecentPlaces) : UseCase<Place, List<Place>> {

    override suspend fun build(args: Place): List<Place> = recentPlaces.update { current ->
        current.filterNot { it == args }
    }
}
