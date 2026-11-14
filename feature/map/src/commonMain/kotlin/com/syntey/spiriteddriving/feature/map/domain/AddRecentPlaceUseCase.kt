package com.syntey.spiriteddriving.feature.map.domain

import app.futured.arkitekt.crusecases.UseCase
import com.syntey.spiriteddriving.core.domain.place.Place
import com.syntey.spiriteddriving.core.domain.place.RecentPlaces
import org.koin.core.annotation.Factory
import com.syntey.spiriteddriving.feature.map.tools.Constants

/**
 * Puts [Place] first among the recent places, replacing the same place if it's kept, and keeps the last
 * [Constants.RecentPlaces.MAX]. Returns the new list.
 */
@Factory
internal class AddRecentPlaceUseCase(private val recentPlaces: RecentPlaces) : UseCase<Place, List<Place>> {

    override suspend fun build(args: Place): List<Place> = recentPlaces.update { current ->
        (listOf(args) + current.filterNot { it.isSameAs(args) }).take(Constants.RecentPlaces.MAX)
    }
}
