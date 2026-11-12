package com.syntey.spiriteddriving.core.domain.route

import com.syntey.spiriteddriving.core.domain.geo.Length
import kotlin.time.Duration

/** How much longer than the fastest route a route may be, in time or in distance. */
sealed interface Budget {

    /** At most [extra] longer than the fastest route. */
    data class Time(val extra: Duration) : Budget

    /** At most [extra] longer than the fastest route. */
    data class Distance(val extra: Length) : Budget
}
