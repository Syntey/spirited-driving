package com.syntey.spiriteddriving.data.persistence.database

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert

@Dao
internal interface StartedDrivesDao {

    @Query("SELECT * FROM started_drive ORDER BY position DESC")
    suspend fun drives(): List<StartedDriveEntity>

    @Query("SELECT COALESCE(MAX(position) + 1, 0) FROM started_drive")
    suspend fun nextPosition(): Int

    @Upsert
    suspend fun upsert(drive: StartedDriveEntity)

    @Query("DELETE FROM started_drive WHERE id NOT IN (SELECT id FROM started_drive ORDER BY position DESC LIMIT :maxKept)")
    suspend fun keepNewest(maxKept: Int)

    @Query("UPDATE started_drive SET isRecorded = 1 WHERE id = :id")
    suspend fun markRecorded(id: String)

    @Query("DELETE FROM started_drive WHERE id = :id")
    suspend fun remove(id: String)

    @Query("UPDATE started_drive SET rating = :rating, isRatingSent = 0 WHERE id = :id")
    suspend fun rate(id: String, rating: String)

    // One statement, so the comparison and the update can't be split by another write
    @Query("UPDATE started_drive SET isRatingSent = 1 WHERE id = :id AND rating = :rating")
    suspend fun markRatingSent(id: String, rating: String)

    @Query("UPDATE started_drive SET isNotDriven = 1 WHERE id = :id")
    suspend fun markNotDriven(id: String)

    /**
     * Puts the drive on top, replacing a kept one with the same id, and drops the oldest added beyond [maxKept]. The
     * entity's position is ignored: the DAO picks it.
     */
    @Transaction
    suspend fun add(drive: StartedDriveEntity, maxKept: Int) {
        upsert(drive.copy(position = nextPosition()))
        keepNewest(maxKept)
    }
}
