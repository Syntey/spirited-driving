package com.syntey.spiriteddriving.shared.navigation.root

import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.AppComponentFactory
import org.koin.core.component.KoinComponent

object RootNavHostFactory : KoinComponent {
    fun create(
        componentContext: AppComponentContext,
    ): RootNavHost = AppComponentFactory.createAppComponent<RootNavHostComponent>(componentContext)
}
