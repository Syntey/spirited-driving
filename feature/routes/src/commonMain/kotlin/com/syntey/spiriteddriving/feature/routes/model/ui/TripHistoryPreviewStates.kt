package com.syntey.spiriteddriving.feature.routes.model.ui

import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistorySource
import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.drive.PastDrivePage
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.route.RouteKind
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.feature.routes.ui.tripHistoryScreen.TripHistoryStatus
import com.syntey.spiriteddriving.feature.routes.ui.tripHistoryScreen.TripHistoryViewState
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

internal object TripHistoryPreviewStates {

    val NOW: Instant = Instant.parse("2026-11-16T15:00:00Z")
    private val TIME_ZONE = TimeZone.of("Europe/Prague")

    private val smichov = FakeTripHistorySource.Recorded.start.copy(name = "Vinohradská 12", area = "Smíchov, Prague")

    private val drives: List<PastDrive> = listOf(
        // Today, rated
        FakeTripHistorySource.Recorded.copy(
            id = "rated",
            startedAt = Instant.parse("2026-11-16T13:20:00Z"),
            start = smichov,
            rating = Rating(Thumb.Up),
        ),
        // Within the last week, not rated yet: Rate
        FakeTripHistorySource.Recorded.copy(
            id = "rate",
            startedAt = Instant.parse("2026-11-14T10:05:00Z"),
            start = smichov,
        ),
        // The fastest route: no end
        FakeTripHistorySource.Recorded.copy(
            id = "fastest",
            startedAt = Instant.parse("2026-11-02T08:40:00Z"),
            kind = RouteKind.Fastest,
            start = smichov.copy(area = "Karlín"),
        ),
        // Last year's month: a fun drive older than a week, no end
        FakeTripHistorySource.Recorded.copy(
            id = "old",
            startedAt = Instant.parse("2025-10-20T14:20:00Z"),
            start = smichov.copy(area = null, name = null),
        ),
    )

    fun viewState(
        status: TripHistoryStatus = TripHistoryStatus.Ready,
        page: PastDrivePage? = PastDrivePage(drives = drives, nextCursor = "4"),
        isLoadingMore: Boolean = false,
        distanceUnit: DistanceUnit = DistanceUnit.Kilometres,
    ) = TripHistoryViewState(
        page = page,
        distanceUnit = distanceUnit,
        status = status,
        isLoadingMore = isLoadingMore,
        now = NOW,
        timeZone = TIME_ZONE,
    )
}
