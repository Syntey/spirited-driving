package com.syntey.spiriteddriving.feature.routes.model.ui

import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import com.syntey.spiriteddriving.core.domain.settings.DistanceUnit
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.resources.measure.distanceText
import com.syntey.spiriteddriving.core.resources.measure.durationText
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SavedRouteRowTest {

    @Test
    fun `a row keeps the saved route's id name and fun level`() {
        val row = FakeSavedRoutesSource.Recorded.toRow(DistanceUnit.Kilometres)

        assertEquals("saved-1", row.id)
        assertEquals("Karlštejn Castle", row.name)
        assertEquals(FakeSavedRoutesSource.Recorded.funLevel, row.funLevel)
    }

    @Test
    fun `details show the start's area, the duration and the distance`() {
        val route = FakeSavedRoutesSource.Recorded
        val area = route.start.shortArea.orEmpty()

        assertEquals(
            StringDesc.ResourceFormatted(
                MR.strings.saved_routes_row,
                area,
                durationText(route.duration),
                distanceText(route.distance, DistanceUnit.Kilometres, isApproximate = false),
            ),
            route.toRow(DistanceUnit.Kilometres).details,
        )
    }

    @Test
    fun `a start without an area or name leaves the start out`() {
        val route = FakeSavedRoutesSource.Recorded.copy(start = FakeSavedRoutesSource.Recorded.start.copy(name = null, area = null))

        assertEquals(
            StringDesc.ResourceFormatted(
                MR.strings.saved_routes_row_no_start,
                durationText(route.duration),
                distanceText(route.distance, DistanceUnit.Kilometres, isApproximate = false),
            ),
            route.toRow(DistanceUnit.Kilometres).details,
        )
    }

    @Test
    fun `the distance follows the unit the user picked`() {
        val route = FakeSavedRoutesSource.Recorded

        assertNotEquals(route.toRow(DistanceUnit.Kilometres).details, route.toRow(DistanceUnit.Miles).details)
    }
}
