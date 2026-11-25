package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.platform.binding.MapsAppKind
import com.syntey.spiriteddriving.core.platform.binding.MapsAppListener
import com.syntey.spiriteddriving.core.platform.binding.MapsAppResult
import com.syntey.spiriteddriving.core.platform.binding.PlatformMapsApp

internal class FakePlatformMapsApp(override val kind: MapsAppKind, private val result: MapsAppResult) : PlatformMapsApp {
    val links = mutableListOf<String>()

    override fun open(link: String, listener: MapsAppListener) {
        links += link
        listener.onResult(result)
    }

    override fun openStore() = Unit
}
