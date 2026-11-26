package com.syntey.spiriteddriving.data.persistence.injection

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.syntey.spiriteddriving.data.persistence.tools.Constants
import okio.Path.Companion.toPath
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
actual class DataStoreModule {

    @Single
    actual fun provideDataStore(): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
        produceFile = { documentsPath(Constants.Files.SETTINGS_DATASTORE).toPath() },
    )
}
