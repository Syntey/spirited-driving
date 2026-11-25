package com.syntey.spiriteddriving.data.persistence.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert

@Dao
internal interface CoverageDao {

    @Query("SELECT * FROM coverage_polygon")
    suspend fun polygons(): List<CoveredPolygonEntity>

    @Insert
    suspend fun insertPolygons(polygons: List<CoveredPolygonEntity>)

    @Query("DELETE FROM coverage_polygon")
    suspend fun deletePolygons()

    @Query("SELECT * FROM coverage_meta WHERE id = ${CoverageMetaEntity.ROW_ID}")
    suspend fun meta(): CoverageMetaEntity?

    @Upsert
    suspend fun upsertMeta(meta: CoverageMetaEntity)
}
