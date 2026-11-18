package com.syntey.spiriteddriving.core.domain.drive

/** The drive doesn't exist any more, such as deleted on another phone. */
class PastDriveNotFoundException(cause: Throwable? = null) : RuntimeException("Drive not found", cause)
