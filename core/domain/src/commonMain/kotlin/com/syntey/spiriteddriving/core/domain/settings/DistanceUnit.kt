package com.syntey.spiriteddriving.core.domain.settings

/** How distances read. */
enum class DistanceUnit {
    Kilometres,
    Miles,
    ;

    companion object {
        /** The unit of road distances in [regionCode]: miles in the US and the UK and their territories, else kilometres. */
        fun of(regionCode: String?): DistanceUnit = if (regionCode?.uppercase() in MILES_REGIONS) Miles else Kilometres

        private val MILES_REGIONS = setOf("US", "PR", "GU", "VI", "AS", "MP", "GB", "IM", "JE", "GG")
    }
}
