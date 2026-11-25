package com.syntey.spiriteddriving.data.persistence.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.syntey.spiriteddriving.data.persistence.persistence.coverage.StoredPolygon
import com.syntey.spiriteddriving.data.persistence.persistence.geo.StoredPoint
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * One polygon of the covered area. The bounding box is written now so a later query by viewport needs no migration.
 * Points are JSON text, never a row each.
 */
@Entity(tableName = "coverage_polygon")
internal data class CoveredPolygonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val minLatitude: Double,
    val maxLatitude: Double,
    val minLongitude: Double,
    val maxLongitude: Double,
    val outline: String,
    val holes: String,
)

/** The single row that records the version of the kept covered area. Its existence means a copy is kept. */
@Entity(tableName = "coverage_meta")
internal data class CoverageMetaEntity(@PrimaryKey val id: Int = ROW_ID, val version: String?) {
    companion object {
        const val ROW_ID = 1
    }
}

private val pointsSerializer = ListSerializer(StoredPoint.serializer())
private val holesSerializer = ListSerializer(pointsSerializer)

/** The polygon's points go through the module's [Json], so they're stored in the same format as before. */
internal fun StoredPolygon.toEntity(json: Json): CoveredPolygonEntity {
    val allPoints = outline + holes.flatten()
    return CoveredPolygonEntity(
        minLatitude = allPoints.minOf { it.latitude },
        maxLatitude = allPoints.maxOf { it.latitude },
        minLongitude = allPoints.minOf { it.longitude },
        maxLongitude = allPoints.maxOf { it.longitude },
        outline = json.encodeToString(pointsSerializer, outline),
        holes = json.encodeToString(holesSerializer, holes),
    )
}

internal fun CoveredPolygonEntity.toStored(json: Json): StoredPolygon = StoredPolygon(
    outline = json.decodeFromString(pointsSerializer, outline),
    holes = json.decodeFromString(holesSerializer, holes),
)
