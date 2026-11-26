package com.syntey.spiriteddriving.data.persistence.injection

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.syntey.spiriteddriving.data.persistence.database.PersistenceDatabase
import com.syntey.spiriteddriving.data.persistence.tools.Constants
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Module
internal actual class DatabaseModule actual constructor() : KoinComponent {

    private val context: Context by inject()

    @Single
    actual fun provideDatabase(): PersistenceDatabase = Room.databaseBuilder<PersistenceDatabase>(
        context = context,
        name = Constants.Files.DATABASE,
    )
        .setDriver(BundledSQLiteDriver())
        .build()
}
