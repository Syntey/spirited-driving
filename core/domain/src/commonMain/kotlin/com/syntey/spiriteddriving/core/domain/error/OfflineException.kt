package com.syntey.spiriteddriving.core.domain.error

/** The phone has no connection, so a call to the backend couldn't be made. */
class OfflineException(cause: Throwable?) : RuntimeException("No connection", cause)
