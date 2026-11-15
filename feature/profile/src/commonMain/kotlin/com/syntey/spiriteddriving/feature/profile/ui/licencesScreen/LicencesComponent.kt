package com.syntey.spiriteddriving.feature.profile.ui.licencesScreen

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.crusecases.execute
import co.touchlab.kermit.Logger
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.syntey.spiriteddriving.core.resources.LegalPages
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.ScreenComponent
import com.syntey.spiriteddriving.feature.profile.domain.GetOpenSourceLibrariesUseCase
import com.syntey.spiriteddriving.feature.profile.domain.OpenWebPageUseCase
import com.syntey.spiriteddriving.feature.profile.model.OpenSourceLibrary
import com.syntey.spiriteddriving.feature.profile.model.ui.LibraryItem
import com.syntey.spiriteddriving.feature.profile.tools.Constants
import com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen.LicenceTextArgs
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
@GenerateFactory
internal class LicencesComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam override val navigation: LicencesScreenNavigation,
    private val getOpenSourceLibrariesUseCase: GetOpenSourceLibrariesUseCase,
    private val openWebPageUseCase: OpenWebPageUseCase,
) : ScreenComponent<LicencesViewState, Nothing, LicencesScreenNavigation>(
    componentContext = componentContext,
    defaultState = LicencesViewState(),
),
    LicencesScreen,
    LicencesScreenNavigation by navigation,
    LicencesScreen.Actions {

    private val logger = Logger.withTag("LicencesComponent")

    override val viewState: StateFlow<LicencesViewState> = componentState
    override val actions: LicencesScreen.Actions = this

    init {
        doOnCreate { loadLibraries() }
    }

    override fun onOpenStreetMapClick() = openWebPage(Constants.Licences.OPENSTREETMAP_COPYRIGHT_URL)

    override fun onLibraryClick(id: String, name: String) = openLicenceText(LicenceTextArgs(libraryId = id, name = name))

    override fun onIconsClick(name: String) =
        openLicenceText(LicenceTextArgs(libraryId = Constants.Licences.PHOSPHOR_ID, name = name))

    private fun openWebPage(url: String) = openWebPageUseCase.execute(OpenWebPageUseCase.Args(url)) {
        onError { error -> logger.e(error) { "Web page not opened" } }
    }

    private fun loadLibraries() = getOpenSourceLibrariesUseCase.execute {
        onSuccess { libraries -> componentState.update { it.copy(libraries = libraries.toItems()) } }
        onError { error -> logger.e(error) { "Open-source libraries not loaded" } }
    }

    private fun List<OpenSourceLibrary>.toItems() = map { library ->
        LibraryItem(
            id = library.id,
            name = library.name,
            licences = library.licences.joinToString(separator = ", ") { it.name },
        )
    }.toImmutableList()
}
