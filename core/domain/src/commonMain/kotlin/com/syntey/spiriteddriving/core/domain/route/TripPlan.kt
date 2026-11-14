package com.syntey.spiriteddriving.core.domain.route

import kotlin.time.Instant

/**
 * The routes planned for a [TripRequest].
 *
 * @property expiresAt Until when the routes can be driven or saved by their IDs.
 * @property fastest The fastest route, which the budget is measured against.
 * @property funRoutes Fun routes that fit the budget, most fun first. Empty when none fits.
 * @property closestFunRouteBudget Only when [funRoutes] is empty: the smallest budget, of the requested type, that the
 * closest fun route needs. Null when no fun route fits within the largest budget the backend accepts.
 */
data class TripPlan(val expiresAt: Instant, val fastest: Route, val funRoutes: List<Route>, val closestFunRouteBudget: Budget?)
