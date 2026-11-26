package com.syntey.spiriteddriving.data.persistence.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert

@Dao
internal interface SavedRoutesDao {

    @Query("SELECT * FROM saved_route ORDER BY position")
    suspend fun routes(): List<SavedRouteEntity>

    @Query("SELECT * FROM saved_routes_meta WHERE id = ${SavedRoutesMetaEntity.ROW_ID}")
    suspend fun meta(): SavedRoutesMetaEntity?

    @Insert
    suspend fun insertRoutes(routes: List<SavedRouteEntity>)

    @Upsert
    suspend fun upsertRoute(route: SavedRouteEntity)

    @Upsert
    suspend fun upsertMeta(meta: SavedRoutesMetaEntity)

    @Query("DELETE FROM saved_route")
    suspend fun deleteRoutes()

    @Query("DELETE FROM saved_route WHERE id = :id")
    suspend fun deleteRoute(id: String)

    @Query("DELETE FROM saved_routes_meta")
    suspend fun deleteMeta()

    @Query("SELECT position FROM saved_route WHERE id = :id")
    suspend fun positionOf(id: String): Int?

    @Query("SELECT COALESCE(MAX(position) + 1, 0) FROM saved_route")
    suspend fun nextPosition(): Int

    @Query("SELECT * FROM saved_route_detail WHERE savedRouteId = :savedRouteId")
    suspend fun detail(savedRouteId: String): SavedRouteDetailEntity?

    @Upsert
    suspend fun upsertDetail(detail: SavedRouteDetailEntity)

    @Query("DELETE FROM saved_route_detail WHERE savedRouteId = :savedRouteId")
    suspend fun deleteDetail(savedRouteId: String)

    @Query("DELETE FROM saved_route_detail")
    suspend fun deleteDetails()

    @Query("DELETE FROM saved_route_detail WHERE savedRouteId NOT IN (SELECT id FROM saved_route)")
    suspend fun deleteOrphanedDetails()

    /** The kept list in order, or `null` when no list is kept. */
    @Transaction
    suspend fun read(): List<SavedRouteEntity>? {
        meta() ?: return null
        return routes()
    }

    /** Replaces the kept list and drops the full routes of saved routes no longer in it. */
    @Transaction
    suspend fun replaceAll(routes: List<SavedRouteEntity>) {
        deleteRoutes()
        insertRoutes(routes)
        upsertMeta(SavedRoutesMetaEntity())
        deleteOrphanedDetails()
    }

    /** Replaces the saved route in place, or appends it, and starts a kept list. The entity's position is ignored. */
    @Transaction
    suspend fun put(route: SavedRouteEntity) {
        val position = positionOf(route.id) ?: nextPosition()
        upsertRoute(route.copy(position = position))
        upsertMeta(SavedRoutesMetaEntity())
    }

    @Transaction
    suspend fun remove(id: String) {
        deleteRoute(id)
        deleteDetail(id)
    }

    @Transaction
    suspend fun clear() {
        deleteRoutes()
        deleteDetails()
        deleteMeta()
    }
}
