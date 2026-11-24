package com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.rating

import com.syntey.spiriteddriving.core.domain.drive.DriveQuestion
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.route.FakeRoutePlanner
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.feature.map.model.ui.RatingStep
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class RatingViewStateTest {

    @Test
    fun `the rating title names the destination`() {
        assertEquals(StringDesc.ResourceFormatted(MR.strings.drive_feedback_question, "Karlštejn Castle"), ratingViewState().title)
    }

    @Test
    fun `the rating title asks without a name for an unnamed destination`() {
        assertEquals(MR.strings.drive_feedback_question_unnamed.desc(), ratingViewState(destinationName = null).title)
    }

    @Test
    fun `the drive text joins the time with the planned duration and distance`() {
        val expected = StringDesc.ResourceFormatted(
            MR.strings.drive_feedback_drive,
            "Today, 14:20".desc(),
            StringDesc.ResourceFormatted(MR.strings.unit_hours_minutes, 1, 5),
            km("58"),
        )

        assertEquals(expected, ratingViewState().driveText)
    }

    @Test
    fun `there are no reason chips before a thumb`() {
        assertTrue(ratingViewState().reasonChips.isEmpty())
    }

    @Test
    fun `there is no thanks before a thumb`() {
        assertNull(ratingViewState().thanks)
    }

    @Test
    fun `a Good rating thanks for what stood out`() {
        assertEquals(MR.strings.drive_feedback_thanks_good.desc(), ratingViewState(Rating(Thumb.Up)).thanks)
    }

    @Test
    fun `a Not good rating asks what didn't work`() {
        assertEquals(MR.strings.drive_feedback_thanks_not_good.desc(), ratingViewState(Rating(Thumb.Down)).thanks)
    }

    @Test
    fun `a Good rating gives its five reason chips in order`() {
        val reasons = ratingViewState(Rating(Thumb.Up)).reasonChips.map { it.reason }

        assertEquals(
            listOf(
                RatingReason.CurvyRoads,
                RatingReason.NiceViews,
                RatingReason.LittleTraffic,
                RatingReason.SmoothSurface,
                RatingReason.WorthTheExtraTime,
            ),
            reasons,
        )
    }

    @Test
    fun `a Not good rating gives its eight reason chips in order`() {
        val reasons = ratingViewState(Rating(Thumb.Down)).reasonChips.map { it.reason }

        assertEquals(
            listOf(
                RatingReason.BoringRoads,
                RatingReason.TooManyTowns,
                RatingReason.TooNarrow,
                RatingReason.RoughSurface,
                RatingReason.HeavyTraffic,
                RatingReason.Roadworks,
                RatingReason.TookTooLong,
                RatingReason.MapsAppWentAnotherWay,
            ),
            reasons,
        )
    }

    @Test
    fun `the picked reason chip is selected`() {
        val chips = ratingViewState(Rating(Thumb.Up, setOf(RatingReason.NiceViews))).reasonChips

        assertEquals(listOf(RatingReason.NiceViews), chips.filter { it.isSelected }.map { it.reason })
    }

    @Test
    fun `a reason chip has the text of its reason`() {
        val chip = ratingViewState(Rating(Thumb.Up)).reasonChips.single { it.reason == RatingReason.NiceViews }

        assertEquals(MR.strings.reason_nice_views.desc(), chip.label)
    }
    private fun ratingViewState(rating: Rating? = null, destinationName: String? = "Karlštejn Castle"): RatingViewState {
        val route = FakeRoutePlanner.Recorded.funRoutes.first()
        val question = DriveQuestion(
            driveId = "drive",
            startedAt = Instant.parse("2026-11-16T13:20:00Z"),
            routeNumber = 1,
            start = route.start.location,
            destination = route.destination.copy(name = destinationName),
            duration = route.duration,
            distance = route.distance,
            line = route.line,
            attribution = route.attribution,
        )
        return RatingViewState(step = RatingStep(question = question, driveTime = "Today, 14:20".desc(), rating = rating))
    }

    private fun km(number: String) = StringDesc.ResourceFormatted(MR.strings.unit_kilometres, number)
}
