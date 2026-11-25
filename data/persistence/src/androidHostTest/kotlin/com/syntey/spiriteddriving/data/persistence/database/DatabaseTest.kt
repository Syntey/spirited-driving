package com.syntey.spiriteddriving.data.persistence.database

import androidx.room.Room
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

// Robolectric provides the Context the Android builder needs. The bundled SQLite driver ships only Android native
// libraries, so this runs on the framework driver, which Robolectric backs with a host SQLite.
@RunWith(RobolectricTestRunner::class)
internal actual abstract class DatabaseTest actual constructor() {

    actual fun database(): PersistenceDatabase =
        Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication()) { PersistenceDatabaseConstructor.initialize() }
            .build()
}
