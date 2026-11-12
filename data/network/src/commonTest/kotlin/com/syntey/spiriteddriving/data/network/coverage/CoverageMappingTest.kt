package com.syntey.spiriteddriving.data.network.coverage

import com.syntey.spiriteddriving.core.domain.geo.GeoPoint
import com.syntey.spiriteddriving.core.domain.geo.GeoPolygon
import com.syntey.spiriteddriving.core.domain.settings.CountryCode
import com.syntey.spiriteddriving.data.network.dto.coverage.CoverageDto
import com.syntey.spiriteddriving.data.network.dto.geo.MultiPolygonDto
import kotlin.test.Test
import kotlin.test.assertEquals

class CoverageMappingTest {

    @Test
    fun `positions are longitude then latitude`() {
        val polygons = MultiPolygonDto(coordinates = listOf(listOf(listOf(listOf(12.5, 50.25)))))
            .toDomain()

        assertEquals(
            listOf(GeoPolygon(outline = listOf(GeoPoint(latitude = 50.25, longitude = 12.5)))),
            polygons,
        )
    }

    @Test
    fun `first ring is the outline and the others are holes`() {
        val polygons = MultiPolygonDto(
            coordinates = listOf(
                listOf(
                    listOf(listOf(0.0, 0.0), listOf(10.0, 0.0), listOf(0.0, 0.0)),
                    listOf(listOf(1.0, 1.0), listOf(2.0, 1.0), listOf(1.0, 1.0)),
                    listOf(listOf(3.0, 3.0), listOf(4.0, 3.0), listOf(3.0, 3.0)),
                ),
            ),
        ).toDomain()

        assertEquals(
            listOf(
                GeoPolygon(
                    outline = listOf(
                        GeoPoint(latitude = 0.0, longitude = 0.0),
                        GeoPoint(latitude = 0.0, longitude = 10.0),
                        GeoPoint(latitude = 0.0, longitude = 0.0),
                    ),
                    holes = listOf(
                        listOf(
                            GeoPoint(latitude = 1.0, longitude = 1.0),
                            GeoPoint(latitude = 1.0, longitude = 2.0),
                            GeoPoint(latitude = 1.0, longitude = 1.0),
                        ),
                        listOf(
                            GeoPoint(latitude = 3.0, longitude = 3.0),
                            GeoPoint(latitude = 3.0, longitude = 4.0),
                            GeoPoint(latitude = 3.0, longitude = 3.0),
                        ),
                    ),
                ),
            ),
            polygons,
        )
    }

    @Test
    fun `each polygon of a multipolygon maps to its own area`() {
        val polygons = MultiPolygonDto(
            coordinates = listOf(
                listOf(listOf(listOf(1.0, 2.0))),
                listOf(listOf(listOf(3.0, 4.0))),
            ),
        ).toDomain()

        assertEquals(
            listOf(
                GeoPolygon(outline = listOf(GeoPoint(latitude = 2.0, longitude = 1.0))),
                GeoPolygon(outline = listOf(GeoPoint(latitude = 4.0, longitude = 3.0))),
            ),
            polygons,
        )
    }

    @Test
    fun `vignette countries keep their order`() {
        val coverage = CoverageDto(
            area = MultiPolygonDto(coordinates = listOf(listOf(listOf(listOf(12.0, 50.0))))),
            vignetteCountries = listOf("CZ", "SK", "AT"),
        ).toDomain()

        assertEquals(listOf(CountryCode("CZ"), CountryCode("SK"), CountryCode("AT")), coverage.vignetteCountries)
    }
}
