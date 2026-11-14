package com.syntey.spiriteddriving.feature.profile.ui.aboutScreen

import app.futured.arkitekt.annotation.GenerateFactory
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.syntey.spiriteddriving.core.platform.binding.Platform
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.ScreenComponent
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Provided

@Factory
@GenerateFactory
internal class AboutComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam override val navigation: AboutScreenNavigation,
    @Provided private val platform: Platform,
) : ScreenComponent<AboutViewState, AboutUiEvent, AboutScreenNavigation>(
    componentContext = componentContext,
    defaultState = AboutViewState(),
),
    AboutScreen,
    AboutScreenNavigation by navigation,
    AboutScreen.Actions {

    override val viewState: StateFlow<AboutViewState> = componentState
    override val actions: AboutScreen.Actions = this

    init {
        doOnCreate { showVersion() }
    }

    override fun onPrivacyPolicyClick() = sendUiEvent(AboutUiEvent.OpenWebPage(LegalPages.PRIVACY_POLICY_URL))
    override fun onTermsClick() = sendUiEvent(AboutUiEvent.OpenWebPage(LegalPages.TERMS_URL))

    private fun showVersion() {
        val version: StringDesc = StringDesc.ResourceFormatted(
            MR.strings.about_version_value,
            platform.appVersionName,
            platform.appBuildNumber,
        )
        componentState.update { it.copy(version = version) }
    }
}
