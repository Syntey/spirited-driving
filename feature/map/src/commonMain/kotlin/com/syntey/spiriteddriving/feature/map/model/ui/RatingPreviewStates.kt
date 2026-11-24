package com.syntey.spiriteddriving.feature.map.model.ui

import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetViewState
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.rating.RatingScreen
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.rating.RatingViewState
import dev.icerock.moko.resources.desc.desc
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow

/** Sample states of the rating step, for its previews. */
internal object RatingPreviewStates {
    /** The drive's question: the rating step after a drive to Karlštejn Castle along the first fun route. */
    private fun question(): DriveQuestion {
        val route = FakeRoutePlanner.Recorded.funRoutes.first()
        return DriveQuestion(
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
    }

    /** The rating step with [rating] picked so far. */
    fun step(rating: Rating? = null, showsReminderOffer: Boolean = false): RatingStep = RatingStep(
        question = question(),
        driveTime = "Today, 14:20".desc(),
        rating = rating,
        showsReminderOffer = showsReminderOffer,
    )

    /** The rating step's own state, for the screen the sheet's UI talks to. */
    fun viewState(rating: Rating? = null, showsReminderOffer: Boolean = false): RatingViewState =
        RatingViewState(step = step(rating, showsReminderOffer))

    /** A rating screen that shows [viewState] and takes no taps. */
    fun screen(rating: Rating? = null, showsReminderOffer: Boolean = false): RatingScreen = object : RatingScreen {
        override val viewState: MutableStateFlow<RatingViewState> =
            MutableStateFlow(RatingPreviewStates.viewState(rating, showsReminderOffer))
        override val actions: RatingScreen.Actions = RatingScreen.Actions.noOpActions()
    }

    /** The sheet's state with the rating step on top, with [rating] picked so far. */
    fun ratingViewState(rating: Rating? = null, showsReminderOffer: Boolean = false): MapSheetViewState =
        MapSheetPreviewStates.viewState().copy(
            sheetHeight = SheetHeight.Half,
            sheetStep = step(rating, showsReminderOffer),
        )
}
