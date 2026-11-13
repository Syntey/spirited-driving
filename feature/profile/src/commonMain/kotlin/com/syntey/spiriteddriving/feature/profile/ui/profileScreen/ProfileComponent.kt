package com.syntey.spiriteddriving.feature.profile.ui.profileScreen

import app.futured.arkitekt.annotation.GenerateFactory
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.ScreenComponent
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
@GenerateFactory
internal class ProfileComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam override val navigation: ProfileScreenNavigation,
) : ScreenComponent<ProfileViewState, Nothing, ProfileScreenNavigation>(
    componentContext = componentContext,
    defaultState = ProfileViewState,
),
    ProfileScreen,
    ProfileScreenNavigation by navigation,
    ProfileScreen.Actions {

    override val viewState: StateFlow<ProfileViewState> = componentState
    override val actions: ProfileScreen.Actions = this

    override fun onSettingsClick() = openSettings()
}
