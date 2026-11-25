package com.syntey.spiriteddriving.data.persistence.injection

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.syntey.spiriteddriving.data.persistence.database.PersistenceDatabase
import com.syntey.spiriteddriving.data.persistence.tools.DATABASE_FILENAME
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@Module
internal actual class DatabaseModule actual constructor() {

    @OptIn(ExperimentalForeignApi::class)
    @Single
    actual fun provideDatabase(): PersistenceDatabase {
        val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )

        return Room.databaseBuilder<PersistenceDatabase>(
            name = requireNotNull(documentDirectory).path + "/$DATABASE_FILENAME",
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}
