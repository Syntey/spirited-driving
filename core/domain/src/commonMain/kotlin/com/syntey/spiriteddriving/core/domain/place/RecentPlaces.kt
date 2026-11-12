package com.syntey.spiriteddriving.core.domain.place

/** The places the user chose last, kept on the device, last used first. */
interface RecentPlaces {

    /** The kept places, last used first; empty when there are none or they can't be read. */
    suspend fun get(): List<Place>

    /**
     * Replaces the kept places with what [transform] makes of them, in one step, so changes close together aren't lost.
     * Returns the new list.
     */
    suspend fun update(transform: (List<Place>) -> List<Place>): List<Place>
}
