package com.syntey.spiriteddriving.data.persistence.database

import androidx.room3.Database
import androidx.room3.ConstructedBy
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor

/**
 * Until the first release the schema isn't exported and has no migrations: a schema change bumps [version] and the
 * builders drop every table, so the device fetches its backend copies again.
 */
@Database(
    entities = [
        CoveredPolygonEntity::class,
        CoverageMetaEntity::class,
        SavedRouteEntity::class,
        SavedRouteDetailEntity::class,
        SavedRoutesMetaEntity::class,
        PastDriveEntity::class,
        TripHistoryMetaEntity::class,
        PastDriveRouteEntity::class,
        UnsentRatingEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
@ConstructedBy(PersistenceDatabaseConstructor::class)
internal abstract class PersistenceDatabase : RoomDatabase() {
    abstract fun coverageDao(): CoverageDao

    abstract fun savedRoutesDao(): SavedRoutesDao

    abstract fun tripHistoryDao(): TripHistoryDao
}

// Room's compiler generates the actual for each target
@Suppress("KotlinNoActualForExpect")
internal expect object PersistenceDatabaseConstructor : RoomDatabaseConstructor<PersistenceDatabase> {
    override fun initialize(): PersistenceDatabase
}
