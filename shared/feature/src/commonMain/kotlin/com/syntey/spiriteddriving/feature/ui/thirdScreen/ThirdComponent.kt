package com.syntey.spiriteddriving.feature.ui.thirdScreen

import app.futured.arkitekt.annotation.GenerateFactory
import com.syntey.spiriteddriving.feature.ui.base.AppComponentContext
import com.syntey.spiriteddriving.feature.ui.base.ScreenComponent
import com.syntey.spiriteddriving.resources.*
import dev.icerock.moko.resources.format
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@GenerateFactory
@Factory
internal class ThirdComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam override val navigation: ThirdScreenNavigation,
    @InjectedParam args: ThirdScreenArgs,
) : ScreenComponent<ThirdViewState, Nothing, ThirdScreenNavigation>(
    componentContext = componentContext,
    defaultState = ThirdViewState(text = MR.strings.third_screen_text.format(args.id)),
),
    ThirdScreen,
    ThirdScreenNavigation by navigation,
    ThirdScreen.Actions {

    override val viewState: StateFlow<ThirdViewState> = componentState

    override val actions: ThirdScreen.Actions = this

    override fun onBack() = pop()
}
