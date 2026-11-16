package com.syntey.spiriteddriving.feature.profile.ui.profileScreen

import app.futured.arkitekt.annotation.GenerateFactory
import com.syntey.spiriteddriving.core.platform.binding.Platform
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.ScreenComponent
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Provided

@Factory
@GenerateFactory
internal class ProfileComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam override val navigation: ProfileScreenNavigation,
    @Provided private val platform: Platform,
) : ScreenComponent<ProfileViewState, ProfileUiEvent, ProfileScreenNavigation>(
    componentContext = componentContext,
    defaultState = ProfileViewState,
),
    ProfileScreen,
    ProfileScreenNavigation by navigation,
    ProfileScreen.Actions {

    override val viewState: StateFlow<ProfileViewState> = componentState
    override val title: StringDesc = MR.strings.profile_screen_title.desc()
    override val actions: ProfileScreen.Actions = this

    override fun onBack() = close()
    override fun onSettingsClick() = openSettings()
    override fun onRateClick() = sendUiEvent(ProfileUiEvent.OpenWebPage(platform.storeReviewUrl))
    override fun onSendFeedbackClick() = openFeedback()
    override fun onAboutClick() = openAbout()
}
