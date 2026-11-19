package com.syntey.spiriteddriving.core.domain.savedroute

/** The saved route doesn't exist any more, such as deleted on another phone. */
class SavedRouteNotFoundException(cause: Throwable? = null) : RuntimeException("Saved route not found", cause)
