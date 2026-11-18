package com.syntey.spiriteddriving.core.domain.drive

/** One page of trip history, newest first. [nextCursor] is null on the last page. */
data class PastDrivePage(val drives: List<PastDrive>, val nextCursor: String?)
