package com.syntey.spiriteddriving.core.domain.place

/**
 * [RecentPlaces] that keeps [places] in memory.
 */
class FakeRecentPlaces(places: List<Place> = emptyList()) : RecentPlaces {

    var places: List<Place> = places
        private set

    override suspend fun get(): List<Place> = places

    override suspend fun update(transform: (List<Place>) -> List<Place>): List<Place> {
        places = transform(places)
        return places
    }
}
