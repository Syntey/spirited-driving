package com.syntey.spiriteddriving.feature.routes.model.ui

import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistorySource
import com.syntey.spiriteddriving.core.domain.drive.PastDrive
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class TripHistorySectionTest {

    @Test
    fun `drives from one month share a section under its header`() {
        val drives = listOf(
            FakeTripHistorySource.Recorded.copy(id = "a", startedAt = Instant.parse("2026-11-12T09:00:00Z")),
            FakeTripHistorySource.Recorded.copy(id = "b", startedAt = Instant.parse("2026-11-03T09:00:00Z")),
        )

        val sections = drives.toSections(NOW, PRAGUE, DistanceUnit.Kilometres)

        assertEquals(1, sections.size)
        assertEquals(listOf("a", "b"), sections.single().rows.map { it.id })
    }

    @Test
    fun `drives across a year boundary get a section for each month`() {
        val drives = listOf(
            FakeTripHistorySource.Recorded.copy(id = "jan", startedAt = Instant.parse("2026-01-05T09:00:00Z")),
            FakeTripHistorySource.Recorded.copy(id = "dec", startedAt = Instant.parse("2025-12-20T09:00:00Z")),
        )

        val now = Instant.parse("2026-01-10T09:00:00Z")
        val sections = drives.toSections(now, PRAGUE, DistanceUnit.Kilometres)

        assertEquals(listOf(listOf("jan"), listOf("dec")), sections.map { section -> section.rows.map { it.id } })
    }

    @Test
    fun `the month is the one in the time zone, not in UTC`() {
        val drives = listOf(
            FakeTripHistorySource.Recorded.copy(id = "new-year", startedAt = Instant.parse("2025-12-31T23:30:00Z")),
            FakeTripHistorySource.Recorded.copy(id = "january", startedAt = Instant.parse("2026-01-02T09:00:00Z")),
        )

        val sections = drives.toSections(NOW, PRAGUE, DistanceUnit.Kilometres)

        assertEquals(1, sections.size)
    }

    @Test
    fun `no drives make no sections`() {
        assertTrue(emptyList<PastDrive>().toSections(NOW, PRAGUE, DistanceUnit.Kilometres).isEmpty())
    }

    private companion object {
        val NOW: Instant = Instant.parse("2026-11-16T15:00:00Z")
        val PRAGUE: TimeZone = TimeZone.of("Europe/Prague")
    }
}
