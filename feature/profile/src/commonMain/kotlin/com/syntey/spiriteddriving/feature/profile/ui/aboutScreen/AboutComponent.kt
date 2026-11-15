package com.syntey.spiriteddriving.feature.profile.ui.aboutScreen

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import co.touchlab.kermit.Logger
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.syntey.spiriteddriving.core.platform.binding.Platform
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.ScreenComponent
import com.syntey.spiriteddriving.feature.profile.domain.OpenWebPageUseCase
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
    private val openWebPageUseCase: OpenWebPageUseCase,
) : ScreenComponent<AboutViewState, Nothing, AboutScreenNavigation>(
    componentContext = componentContext,
    defaultState = AboutViewState(),
),
    AboutScreen,
    AboutScreenNavigation by navigation,
    AboutScreen.Actions {

    private val logger = Logger.withTag("AboutComponent")

    override val viewState: StateFlow<AboutViewState> = componentState
    override val actions: AboutScreen.Actions = this

    init {
        doOnCreate { showVersion() }
    }

    override fun onPrivacyPolicyClick() = openWebPage(LegalPages.PRIVACY_POLICY_URL)
    override fun onTermsClick() = openWebPage(LegalPages.TERMS_URL)
    override fun onLicencesClick() = openLicences()

    private fun openWebPage(url: String) = openWebPageUseCase.execute(OpenWebPageUseCase.Args(url)) {
        onError { error -> logger.e(error) { "Web page not opened" } }
    }

    private fun showVersion() {
        val version: StringDesc = StringDesc.ResourceFormatted(
            MR.strings.about_version_value,
            platform.appVersionName,
            platform.appBuildNumber,
        )
        componentState.update { it.copy(version = version) }
    }
}
