package com.syntey.spiriteddriving.data.persistence.injection

import com.syntey.spiriteddriving.data.persistence.database.CoverageDao
import com.syntey.spiriteddriving.data.persistence.database.PersistenceDatabase
import com.syntey.spiriteddriving.data.persistence.database.SavedRoutesDao
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
internal expect class DatabaseModule() {

    @Single
    fun provideDatabase(): PersistenceDatabase
}

@Module
internal class DaoModule {

    @Single
    fun provideCoverageDao(database: PersistenceDatabase): CoverageDao = database.coverageDao()

    @Single
    fun provideSavedRoutesDao(database: PersistenceDatabase): SavedRoutesDao = database.savedRoutesDao()
}
