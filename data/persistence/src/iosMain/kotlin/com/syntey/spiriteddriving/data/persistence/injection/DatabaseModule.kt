package com.syntey.spiriteddriving.data.persistence.injection

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.syntey.spiriteddriving.data.persistence.database.PersistenceDatabase
import com.syntey.spiriteddriving.data.persistence.tools.Constants
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
internal actual class DatabaseModule actual constructor() {

    @Single
    actual fun provideDatabase(): PersistenceDatabase = Room.databaseBuilder<PersistenceDatabase>(
        name = documentsPath(Constants.Files.DATABASE),
    )
        .setDriver(BundledSQLiteDriver())
        .build()
}
