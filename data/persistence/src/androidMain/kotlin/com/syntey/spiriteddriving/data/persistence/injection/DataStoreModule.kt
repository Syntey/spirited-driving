package com.syntey.spiriteddriving.data.persistence.injection

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.syntey.spiriteddriving.data.persistence.tools.Constants
import okio.Path.Companion.toPath
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Module
actual class DataStoreModule actual constructor() : KoinComponent {

    private val context: Context by inject()

    @Single
    actual fun provideDataStore(): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
        produceFile = { context.filesDir.resolve(Constants.Files.SETTINGS_DATASTORE).absolutePath.toPath() },
    )
}
