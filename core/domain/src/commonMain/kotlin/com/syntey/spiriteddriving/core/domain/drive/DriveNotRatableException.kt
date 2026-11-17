package com.syntey.spiriteddriving.core.domain.drive

/** The backend won't take the rating, so retrying it can't succeed. */
class DriveNotRatableException(cause: Throwable? = null) : RuntimeException("Drive can't be rated", cause)
