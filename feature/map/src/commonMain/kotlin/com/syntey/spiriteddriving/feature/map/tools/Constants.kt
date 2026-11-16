@file:Suppress("MagicNumber")

package com.syntey.spiriteddriving.feature.map.tools

import com.syntey.spiriteddriving.core.domain.geo.GeoBounds
import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.miles
import com.syntey.spiriteddriving.core.domain.route.Budget
import com.syntey.spiriteddriving.core.domain.route.BudgetChoice
import com.syntey.spiriteddriving.core.domain.route.BudgetKind
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

internal object Constants {

    object Budgets {
        // Placeholders until the values are settled (route planning, open question 1)
        val TIME: List<Budget.Time> = listOf(15, 30, 45, 60, 90, 120).map { Budget.Time(it.minutes) }
        val DISTANCE_KILOMETRES: List<Budget.Distance> = listOf(10, 20, 30, 50, 75, 100).map { Budget.Distance(it.kilometres) }
        val DISTANCE_MILES: List<Budget.Distance> = listOf(5, 10, 20, 30, 50, 60).map { Budget.Distance(it.miles) }

        // Before the first pick: +30 min, with +30 km for the first switch to distance
        val DEFAULT_CHOICE = BudgetChoice(
            time = Budget.Time(30.minutes),
            distance = Budget.Distance(30.kilometres),
            kind = BudgetKind.Time,
        )
    }

    object Search {
        /** The shortest text that is searched for. */
        const val MIN_LENGTH = 2
        val TYPING_PAUSE = 300.milliseconds
        val SLOW_AFTER = 500.milliseconds
        const val MAX_MATCHING_RECENT_PLACES = 3
    }

    object RecentPlaces {
        const val MAX = 15
    }

    object Trip {
        // Google Maps takes 9 waypoints; Apple Maps' limit isn't documented, so it gets the same until measured on a device
        const val HAND_OFF_WAYPOINTS = 9
    }

    object Drive {
        /** Farther than this from the user, the sheet says how far away the route starts. */
        val START_ELSEWHERE_DISTANCE = 1.kilometres
    }

    object Map {
        // The launch region, by its corners
        val LAUNCH_REGION =
            GeoBounds(southWest = GeoPoint(latitude = 48.55, longitude = 12.09), northEast = GeoPoint(latitude = 51.06, longitude = 18.86))

        // About a kilometre each way around a destination's pin
        const val DESTINATION_MARGIN_LAT = 0.01
        const val DESTINATION_MARGIN_LON = 0.015

        // The smallest area a route overview frames, about a kilometre each way, as the destination's pin is framed
        const val MIN_ROUTE_LAT_SPAN = 0.02
        const val MIN_ROUTE_LON_SPAN = 0.03
    }
}
