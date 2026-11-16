package com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import co.touchlab.kermit.Logger
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.ScreenComponent
import com.syntey.spiriteddriving.feature.profile.domain.GetLicencesUseCase
import com.syntey.spiriteddriving.feature.profile.domain.OpenWebPageUseCase
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
@GenerateFactory
internal class LicenceTextComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam override val navigation: LicenceTextScreenNavigation,
    @InjectedParam private val args: LicenceTextArgs,
    private val getLicencesUseCase: GetLicencesUseCase,
    private val openWebPageUseCase: OpenWebPageUseCase,
) : ScreenComponent<LicenceTextViewState, Nothing, LicenceTextScreenNavigation>(
    componentContext = componentContext,
    defaultState = LicenceTextViewState(),
),
    LicenceTextScreen,
    LicenceTextScreenNavigation by navigation,
    LicenceTextScreen.Actions {

    private val logger = Logger.withTag("LicenceTextComponent")

    override val title: StringDesc = args.name.desc()
    override val viewState: StateFlow<LicenceTextViewState> = componentState
    override val actions: LicenceTextScreen.Actions = this

    init {
        doOnCreate { loadLicences() }
    }

    override fun onBackClick() = pop()
    override fun onLicenceLinkClick(url: String) = openWebPage(url)

    private fun openWebPage(url: String) = openWebPageUseCase.execute(OpenWebPageUseCase.Args(url)) {
        onError { error -> logger.e(error) { "Web page not opened" } }
    }

    private fun loadLicences() = getLicencesUseCase.execute(GetLicencesUseCase.Args(args.libraryId)) {
        onSuccess { licences -> componentState.update { it.copy(licences = licences) } }
        onError { error -> logger.e(error) { "Licence text not loaded" } }
    }
}
