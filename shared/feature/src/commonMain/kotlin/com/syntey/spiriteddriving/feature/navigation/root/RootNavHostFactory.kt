package com.syntey.spiriteddriving.feature.navigation.root

import com.syntey.spiriteddriving.feature.ui.base.AppComponentContext
import com.syntey.spiriteddriving.feature.ui.base.AppComponentFactory
import org.koin.core.component.KoinComponent

object RootNavHostFactory : KoinComponent {
    fun create(
        componentContext: AppComponentContext,
    ): RootNavHost = AppComponentFactory.createAppComponent<RootNavHostComponent>(componentContext)
}
