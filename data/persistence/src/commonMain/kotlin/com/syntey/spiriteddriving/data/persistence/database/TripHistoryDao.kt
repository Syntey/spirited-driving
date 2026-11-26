package com.syntey.spiriteddriving.data.persistence.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert

@Dao
internal interface TripHistoryDao {

    @Query("SELECT * FROM past_drive ORDER BY position")
    suspend fun drives(): List<PastDriveEntity>

    @Query("SELECT * FROM trip_history_meta WHERE id = ${TripHistoryMetaEntity.ROW_ID}")
    suspend fun meta(): TripHistoryMetaEntity?

    // A drive already kept isn't added again
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDrives(drives: List<PastDriveEntity>)

    @Upsert
    suspend fun upsertMeta(meta: TripHistoryMetaEntity)

    @Query("DELETE FROM past_drive")
    suspend fun deleteDrives()

    @Query("DELETE FROM past_drive WHERE id = :id")
    suspend fun deleteDrive(id: String)

    @Query("DELETE FROM trip_history_meta")
    suspend fun deleteMeta()

    @Query("SELECT COALESCE(MAX(position) + 1, 0) FROM past_drive")
    suspend fun nextPosition(): Int

    /** The number of rated drives: 0 when the drive isn't kept. */
    @Query("UPDATE past_drive SET rating = :rating WHERE id = :id")
    suspend fun setRating(id: String, rating: String): Int

    @Query(
        "UPDATE past_drive SET rating = (SELECT rating FROM unsent_rating WHERE unsent_rating.driveId = past_drive.id) " +
            "WHERE id IN (SELECT driveId FROM unsent_rating)",
    )
    suspend fun applyUnsentRatings()

    @Query("SELECT * FROM past_drive_route WHERE driveId = :driveId")
    suspend fun route(driveId: String): PastDriveRouteEntity?

    @Upsert
    suspend fun upsertRoute(route: PastDriveRouteEntity)

    @Query("DELETE FROM past_drive_route WHERE driveId = :driveId")
    suspend fun deleteRoute(driveId: String)

    @Query("DELETE FROM past_drive_route WHERE driveId NOT IN (SELECT id FROM past_drive)")
    suspend fun deleteOrphanedRoutes()

    @Query("SELECT * FROM unsent_rating")
    suspend fun unsentRatings(): List<UnsentRatingEntity>

    @Upsert
    suspend fun upsertUnsentRating(rating: UnsentRatingEntity)

    @Query("DELETE FROM unsent_rating WHERE driveId = :driveId")
    suspend fun deleteUnsentRatingOf(driveId: String)

    @Query("DELETE FROM unsent_rating WHERE driveId = :driveId AND rating = :rating")
    suspend fun deleteUnsentRating(driveId: String, rating: String)

    @Query("DELETE FROM unsent_rating")
    suspend fun deleteUnsentRatings()

    /** The kept drives in order and the cursor, or `null` when no Trip history is kept. */
    @Transaction
    suspend fun read(): KeptTripHistory? {
        val meta = meta() ?: return null
        return KeptTripHistory(drives = drives(), nextCursor = meta.nextCursor)
    }

    /** Replaces the kept drives and the cursor, and drops the full routes of drives no longer in them. */
    @Transaction
    suspend fun replaceAll(drives: List<PastDriveEntity>, nextCursor: String?) {
        deleteDrives()
        insertDrives(drives)
        upsertMeta(TripHistoryMetaEntity(nextCursor = nextCursor))
        applyUnsentRatings()
        deleteOrphanedRoutes()
    }

    /** Adds the drives at the end and keeps the cursor. The entities' positions are ignored: the DAO picks them. */
    @Transaction
    suspend fun append(drives: List<PastDriveEntity>, nextCursor: String?) {
        val start = nextPosition()
        insertDrives(drives.mapIndexed { index, drive -> drive.copy(position = start + index) })
        upsertMeta(TripHistoryMetaEntity(nextCursor = nextCursor))
        applyUnsentRatings()
    }

    /** Shows the rating on the kept drive and keeps it as not sent yet. Nothing happens without a kept drive. */
    @Transaction
    suspend fun rate(id: String, rating: String) {
        if (setRating(id, rating) == 0) return
        upsertUnsentRating(UnsentRatingEntity(driveId = id, rating = rating))
    }

    /** Removes the drive with its full route and its unsent rating. */
    @Transaction
    suspend fun remove(id: String) {
        deleteDrive(id)
        deleteRoute(id)
        deleteUnsentRatingOf(id)
    }

    /** Removes everything. With no drives left every full route is orphaned, so the orphan query drops them all. */
    @Transaction
    suspend fun clear() {
        deleteDrives()
        deleteOrphanedRoutes()
        deleteUnsentRatings()
        deleteMeta()
    }
}
