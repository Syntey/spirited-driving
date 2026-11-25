package com.syntey.spiriteddriving.data.persistence.database

import com.syntey.spiriteddriving.data.persistence.injection.PersistenceModule
import com.syntey.spiriteddriving.data.persistence.persistence.coverage.StoredPolygon
import com.syntey.spiriteddriving.data.persistence.persistence.geo.StoredPoint
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CoverageDaoTest : DatabaseTest() {

    @Test
    fun `a polygon with its holes reads back from the database`() = runTest {
        val json = PersistenceModule().provideJson()
        val dao = database().coverageDao()
        val polygon = StoredPolygon(
            outline = listOf(StoredPoint(1.0, 2.0), StoredPoint(1.0, 5.0), StoredPoint(4.0, 5.0)),
            holes = listOf(listOf(StoredPoint(2.0, 3.0), StoredPoint(2.5, 3.5))),
        )

        dao.insertPolygons(listOf(polygon.toEntity(json)))

        assertEquals(polygon, dao.polygons().single().toStored(json))
    }
}
