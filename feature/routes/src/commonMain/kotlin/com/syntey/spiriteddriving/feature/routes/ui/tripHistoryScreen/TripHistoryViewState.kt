package com.syntey.spiriteddriving.feature.routes.ui.tripHistoryScreen

import com.syntey.spiriteddriving.core.domain.drive.PastDrivePage
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.feature.routes.model.ui.RatingSheet
import com.syntey.spiriteddriving.feature.routes.model.ui.TripHistorySection
import com.syntey.spiriteddriving.feature.routes.model.ui.toSections
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

data class TripHistoryViewState(
    /** The kept drives, or `null` while nothing is kept. */
    val page: PastDrivePage? = null,
    val distanceUnit: DistanceUnit = DistanceUnit.Kilometres,
    val status: TripHistoryStatus = TripHistoryStatus.Loading,
    /** A page after the kept ones is on its way, so placeholder rows show at the end. */
    val isLoadingMore: Boolean = false,
    /** The time the rows' days are worked out from; set when the screen is created and refreshed. */
    val now: Instant = Instant.DISTANT_PAST,
    val timeZone: TimeZone = TimeZone.UTC,
    /** Drives deleted with their Undo still open: they're left out of [sections] until the message ends. */
    val deletingIds: ImmutableSet<String> = persistentSetOf(),
    /** The rating sheet over the list, while a drive is being rated; null otherwise. */
    val ratingSheet: RatingSheet? = null,
    val isClearConfirmationShown: Boolean = false,
    /** The history is being cleared on the backend, so a second confirmation is ignored. */
    val isClearing: Boolean = false,
) {
    /** The drives in sections by month, newest first, without the drives whose delete is still undoable. */
    val sections: ImmutableList<TripHistorySection>
        get() = page?.drives.orEmpty()
            .filterNot { it.id in deletingIds }
            .toSections(now, timeZone, distanceUnit)

    /** Whether nothing is kept. A drive whose delete is undoable still counts, so the list doesn't flash empty. */
    val isEmpty: Boolean
        get() = page?.drives.isNullOrEmpty()

    /** Whether the backend has a page after the kept ones. */
    val hasMore: Boolean
        get() = page?.nextCursor != null

    /** Whether the bar offers Clear trip history: there are drives to clear, and the list has loaded them. */
    val canClear: Boolean
        get() = status == TripHistoryStatus.Ready && !isEmpty
}
