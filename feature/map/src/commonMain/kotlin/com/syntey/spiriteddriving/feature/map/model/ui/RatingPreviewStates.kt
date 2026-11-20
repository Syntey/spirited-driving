package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetViewState
import dev.icerock.moko.resources.desc.desc
import kotlin.time.Instant

/** Sample view states of the rating step, for its previews. */
internal object RatingPreviewStates {
    /** The rating step after a drive to Karlštejn Castle along the first fun route, with [rating] picked so far. */
    fun ratingViewState(rating: Rating? = null, showsReminderOffer: Boolean = false): MapSheetViewState {
        val route = FakeRoutePlanner.Recorded.funRoutes.first()
        val question = DriveQuestion(
            driveId = "preview",
            startedAt = Instant.parse("2026-11-16T13:20:00Z"),
            routeNumber = 1,
            start = route.start.location,
            destination = route.destination,
            duration = route.duration,
            distance = route.distance,
            line = route.line,
            attribution = route.attribution,
        )
        return MapSheetPreviewStates.viewState().copy(
            sheetHeight = SheetHeight.Half,
            sheetStep = RatingStep(
                question = question,
                driveTime = "Today, 14:20".desc(),
                rating = rating,
                showsReminderOffer = showsReminderOffer,
            ),
        )
    }
}
