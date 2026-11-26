package com.syntey.spiriteddriving.data.persistence.database

import androidx.room3.Database
import androidx.room3.ConstructedBy
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor

@Database(entities = [CoveredPolygonEntity::class, CoverageMetaEntity::class], version = 1, exportSchema = true)
@ConstructedBy(PersistenceDatabaseConstructor::class)
internal abstract class PersistenceDatabase : RoomDatabase() {
    abstract fun coverageDao(): CoverageDao
}

// Room's compiler generates the actual for each target
@Suppress("KotlinNoActualForExpect")
internal expect object PersistenceDatabaseConstructor : RoomDatabaseConstructor<PersistenceDatabase> {
    override fun initialize(): PersistenceDatabase
}
