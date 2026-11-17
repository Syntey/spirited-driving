package com.syntey.spiriteddriving.core.domain.drive

import kotlin.time.Duration
import kotlin.time.Instant

/**
 * A route handed off to the maps app.
 *
 * @property id Made by the app (a random UUID), so the drive can be referred to before the backend has it.
 * @property startedAt When the route was handed off, by the phone's clock.
 * @property plannedDuration The route's duration, from which the app tells when the drive is likely over.
 * @property isRecorded Whether the backend has the drive.
 * @property rating The user's rating; null while not rated.
 * @property isRatingSent Whether the backend has [rating] as it is now.
 * @property isNotDriven The user said they didn't drive it; it's kept until the backend has removed it.
 */
data class Drive(
    val id: String,
    val routeId: String,
    val startedAt: Instant,
    val plannedDuration: Duration,
    val isRecorded: Boolean = false,
    val rating: Rating? = null,
    val isRatingSent: Boolean = false,
    val isNotDriven: Boolean = false,
)
