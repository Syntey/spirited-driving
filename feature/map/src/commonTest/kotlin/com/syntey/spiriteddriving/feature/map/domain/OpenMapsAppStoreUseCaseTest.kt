package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.platform.binding.MapsApp
import com.syntey.spiriteddriving.core.platform.binding.MapsAppKind
import com.syntey.spiriteddriving.core.platform.binding.MapsAppListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformMapsApp
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class OpenMapsAppStoreUseCaseTest {

    @Test
    fun `opens the maps app's store page through the binding`() = runTest {
        val mapsApp = StorePlatformMapsApp()

        OpenMapsAppStoreUseCase(MapsApp(mapsApp)).build(Unit)

        assertTrue(mapsApp.storeOpened)
    }

    private class StorePlatformMapsApp : PlatformMapsApp {
        var storeOpened = false

        override val kind: MapsAppKind = MapsAppKind.GoogleMaps

        override fun open(link: String, listener: MapsAppListener) = Unit

        override fun openStore() {
            storeOpened = true
        }
    }
}
