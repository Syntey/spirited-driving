package com.syntey.spiriteddriving.data.persistence.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert

@Dao
internal interface CoverageDao {

    @Query("SELECT * FROM coverage_polygon ORDER BY id")
    suspend fun polygons(): List<CoveredPolygonEntity>

    @Insert
    suspend fun insertPolygons(polygons: List<CoveredPolygonEntity>)

    @Query("DELETE FROM coverage_polygon")
    suspend fun deletePolygons()

    @Query("SELECT * FROM coverage_meta WHERE id = ${CoverageMetaEntity.ROW_ID}")
    suspend fun meta(): CoverageMetaEntity?

    @Upsert
    suspend fun upsertMeta(meta: CoverageMetaEntity)

    /** Replaces the kept polygons and the version together, so a read never sees one without the other. */
    @Transaction
    suspend fun replace(polygons: List<CoveredPolygonEntity>, meta: CoverageMetaEntity) {
        deletePolygons()
        insertPolygons(polygons)
        upsertMeta(meta)
    }

    /** The version and the polygons read in one transaction, or `null` when no copy is kept. */
    @Transaction
    suspend fun read(): CoverageRows? {
        val meta = meta() ?: return null
        return CoverageRows(meta = meta, polygons = polygons())
    }
}
