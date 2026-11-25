package com.syntey.spiriteddriving.feature.routes.model.ui

import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistorySource
import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.drive.Rating
import com.syntey.spiriteddriving.core.domain.drive.RatingReason
import com.syntey.spiriteddriving.core.domain.drive.Thumb
import com.syntey.spiriteddriving.core.domain.geo.Length.Companion.kilometres
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.resources.datetime.driveTimeText
import com.syntey.spiriteddriving.core.resources.measure.durationText
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

class RatingSheetTest {

    @Test
    fun `the title asks about the route to the destination`() {
        val sheet = drive().toRatingSheet(NOW, PRAGUE, DistanceUnit.Kilometres)

        assertEquals(StringDesc.ResourceFormatted(MR.strings.drive_feedback_question, "Křivoklát Castle"), sheet.title)
    }

    @Test
    fun `a destination without a name asks about the route alone`() {
        val sheet = drive(destinationName = null).toRatingSheet(NOW, PRAGUE, DistanceUnit.Kilometres)

        assertEquals(MR.strings.drive_feedback_question_unnamed.desc(), sheet.title)
    }

    @Test
    fun `the subtitle is the drive's time, its duration and its distance`() {
        val sheet = drive().toRatingSheet(NOW, PRAGUE, DistanceUnit.Kilometres)

        assertEquals(
            StringDesc.ResourceFormatted(
                MR.strings.drive_feedback_drive,
                driveTimeText(STARTED_AT, NOW, PRAGUE),
                durationText(DURATION),
                distanceText(DISTANCE, DistanceUnit.Kilometres, isApproximate = false),
            ),
            sheet.subtitle,
        )
    }

    @Test
    fun `a drive keeps its rating in the sheet`() {
        val sheet = drive(rating = Rating(Thumb.Down)).toRatingSheet(NOW, PRAGUE, DistanceUnit.Kilometres)

        assertEquals(Rating(Thumb.Down), sheet.rating)
    }

    @Test
    fun `no thanks and no reasons before a thumb is picked`() {
        val sheet = RatingSheet(driveId = "drive", title = "".desc(), subtitle = "".desc())

        assertNull(sheet.thanks)
        assertTrue(sheet.reasonChips.isEmpty())
    }

    @Test
    fun `a good rating thanks the user and offers the good reasons`() {
        val sheet = sheetWith(Rating(Thumb.Up, setOf(RatingReason.NiceViews)))

        assertEquals(MR.strings.drive_feedback_thanks_good.desc(), sheet.thanks)
        assertEquals(RatingReason.of(Thumb.Up), sheet.reasonChips.map { it.reason })
        assertEquals(setOf(RatingReason.NiceViews), sheet.reasonChips.filter { it.isSelected }.map { it.reason }.toSet())
    }

    @Test
    fun `a not good rating asks what didn't work and offers the not good reasons`() {
        val sheet = sheetWith(Rating(Thumb.Down, setOf(RatingReason.Roadworks)))

        assertEquals(MR.strings.drive_feedback_thanks_not_good.desc(), sheet.thanks)
        assertEquals(RatingReason.of(Thumb.Down), sheet.reasonChips.map { it.reason })
        assertEquals(listOf(RatingReason.Roadworks), sheet.reasonChips.filter { it.isSelected }.map { it.reason })
    }

    @Test
    fun `a reason chip has the reason's label`() {
        val chip = sheetWith(Rating(Thumb.Up)).reasonChips.first()

        assertEquals(chip.reason.label(), chip.label)
    }

    private fun sheetWith(rating: Rating) = RatingSheet(driveId = "drive", title = "".desc(), subtitle = "".desc(), rating = rating)

    private fun drive(destinationName: String? = "Křivoklát Castle", rating: Rating? = null): PastDrive =
        FakeTripHistorySource.Recorded.copy(
            startedAt = STARTED_AT,
            destination = FakeTripHistorySource.Recorded.destination.copy(name = destinationName),
            duration = DURATION,
            distance = DISTANCE,
            rating = rating,
        )

    private companion object {
        val NOW: Instant = Instant.parse("2026-11-16T15:00:00Z")
        val STARTED_AT: Instant = Instant.parse("2026-11-15T15:40:00Z")
        val DURATION = 80.minutes
        val DISTANCE = 74.kilometres
        val PRAGUE = TimeZone.of("Europe/Prague")
    }
}
