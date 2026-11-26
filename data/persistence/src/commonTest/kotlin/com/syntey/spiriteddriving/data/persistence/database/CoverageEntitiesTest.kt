package com.syntey.spiriteddriving.data.persistence.database

import com.syntey.spiriteddriving.data.persistence.injection.PersistenceModule
import com.syntey.spiriteddriving.data.persistence.persistence.coverage.StoredPolygon
import com.syntey.spiriteddriving.data.persistence.persistence.geo.StoredPoint
import kotlin.test.Test
import kotlin.test.assertEquals

class CoverageEntitiesTest {

    private val json = PersistenceModule().provideJson()

    private val polygon = StoredPolygon(
        outline = listOf(
            StoredPoint(latitude = 10.0, longitude = 20.0),
            StoredPoint(latitude = 10.0, longitude = 30.0),
            StoredPoint(latitude = 20.0, longitude = 30.0),
            StoredPoint(latitude = 20.0, longitude = 20.0),
        ),
        holes = listOf(
            listOf(
                StoredPoint(latitude = 12.0, longitude = 22.0),
                StoredPoint(latitude = 12.0, longitude = 25.0),
                StoredPoint(latitude = 15.0, longitude = 25.0),
            ),
        ),
    )

    @Test
    fun `bounding box comes from the outline`() {
        val entity = polygon.toEntity(json)

        assertEquals(10.0, entity.minLatitude)
        assertEquals(20.0, entity.maxLatitude)
        assertEquals(20.0, entity.minLongitude)
        assertEquals(30.0, entity.maxLongitude)
    }

    @Test
    fun `outline and holes survive a round trip through the entity`() {
        assertEquals(polygon, polygon.toEntity(json).toStored(json))
    }
}
