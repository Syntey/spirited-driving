package com.syntey.spiriteddriving.core.domain.route

/** The trip's start or destination is outside the covered area, so it can't be planned. */
class OutsideCoverageException(cause: Throwable?) : RuntimeException("Outside the covered area", cause)
