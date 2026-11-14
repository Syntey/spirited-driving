package com.syntey.spiriteddriving.core.domain.drive

/** The backend won't take the drive, so retrying it can't succeed. */
class DriveNotRecordableException(cause: Throwable? = null) : RuntimeException("Drive can't be recorded", cause)
