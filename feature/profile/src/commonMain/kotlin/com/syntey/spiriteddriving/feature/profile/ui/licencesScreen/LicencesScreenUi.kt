package com.syntey.spiriteddriving.feature.profile.ui.licencesScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.AppScaffold
import com.syntey.spiriteddriving.core.ui.components.AndroidTopBar
import com.syntey.spiriteddriving.core.ui.components.IconAction
import com.syntey.spiriteddriving.core.ui.components.ListGroup
import com.syntey.spiriteddriving.core.ui.components.ListRow
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.DarkLightPreviews
import com.syntey.spiriteddriving.core.ui.preview.ScreenPreviews
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.feature.profile.model.ui.LibraryItem
import com.syntey.spiriteddriving.feature.profile.ui.components.WebLinkIcon
import com.syntey.spiriteddriving.feature.profile.ui.licencesScreen.LicencesScreen.Actions.Companion.noOpActions
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun LicencesScreenUi(screen: LicencesScreen, modifier: Modifier = Modifier) {
    val viewState by screen.viewState.collectAsStateWithLifecycle()
    Content(viewState = viewState, actions = screen.actions, title = screen.title.localized(), modifier = modifier)
}

@Composable
private fun Content(
    viewState: LicencesViewState,
    actions: LicencesScreen.Actions,
    title: String?,
    modifier: Modifier = Modifier,
) {
    val phosphorName = stringResource(MR.strings.licences_phosphor)
    AppScaffold(
        topBar = {
            AndroidTopBar(
                title = title,
                navigation = IconAction(
                    icon = painterResource(MR.images.back),
                    label = stringResource(MR.strings.generic_back),
                    onClick = actions::onBack,
                ),
            )
        },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(top = Grid.d4),
            verticalArrangement = Arrangement.spacedBy(Grid.d6),
        ) {
            ListGroup(header = stringResource(MR.strings.licences_route_data)) {
                item { position ->
                    ListRow(
                        headline = stringResource(MR.strings.licences_osm),
                        supporting = stringResource(MR.strings.licences_osm_supporting),
                        trailing = { WebLinkIcon() },
                        onClick = actions::onOpenStreetMapClick,
                        position = position,
                    )
                }
            }
            if (viewState.libraries.isNotEmpty()) {
                ListGroup(header = stringResource(MR.strings.licences_libraries)) {
                    viewState.libraries.forEach { library ->
                        item { position ->
                            ListRow(
                                headline = library.name,
                                supporting = library.licences,
                                chevron = painterResource(MR.images.chevron_right),
                                onClick = { actions.onLibraryClick(library.id, library.name) },
                                position = position,
                            )
                        }
                    }
                }
            }
            ListGroup(header = stringResource(MR.strings.licences_icons)) {
                item { position ->
                    ListRow(
                        headline = phosphorName,
                        supporting = stringResource(MR.strings.licences_mit),
                        chevron = painterResource(MR.images.chevron_right),
                        onClick = { actions.onIconsClick(phosphorName) },
                        position = position,
                    )
                }
            }
        }
    }
}

private val sampleLibraries = persistentListOf(
    LibraryItem(id = "androidx.activity:activity", name = "Activity", licences = "Apache License 2.0"),
    LibraryItem(id = "io.insert-koin:koin-core", name = "Koin", licences = "Apache License 2.0"),
    LibraryItem(
        id = "org.jetbrains.kotlinx:kotlinx-collections-immutable",
        name = "collections",
        licences = "Apache License 2.0",
    ),
    LibraryItem(
        id = "androidx.compose.material3:material3",
        name = "Material 3",
        licences = "Apache License 2.0, MIT License",
    ),
)

@ScreenPreviews
@Composable
private fun LicencesScreenPreview() = Showcase {
    Content(
        viewState = LicencesViewState(libraries = sampleLibraries),
        actions = noOpActions(),
        title = stringResource(MR.strings.licences_screen_title),
    )
}

@DarkLightPreviews
@Composable
private fun LicencesScreenIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = LicencesViewState(libraries = sampleLibraries.take(2).toImmutableList()),
        actions = noOpActions(),
        title = stringResource(MR.strings.licences_screen_title),
    )
}
