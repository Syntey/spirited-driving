package com.syntey.spiriteddriving.data.persistence.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

internal actual abstract class DatabaseTest actual constructor() {

    actual fun database(): PersistenceDatabase = Room.inMemoryDatabaseBuilder { PersistenceDatabaseConstructor.initialize() }
        .setDriver(BundledSQLiteDriver())
        .build()
}
