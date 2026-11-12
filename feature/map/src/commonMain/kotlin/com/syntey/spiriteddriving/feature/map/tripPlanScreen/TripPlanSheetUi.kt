package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.IconAction
import com.syntey.spiriteddriving.core.ui.components.NoteCard
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.feature.map.tripPlanScreen.TripPlanScreen.Actions.Companion.noOpActions
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

/**
 * The content of the map's sheet, shared by both platforms: the Android bottom sheet and the native iOS sheet host it.
 */
@Composable
fun TripPlanSheetUi(screen: TripPlanScreen, modifier: Modifier = Modifier) {
    val viewState by screen.viewState.collectAsStateWithLifecycle()
    Content(viewState = viewState, actions = screen.actions, modifier = modifier)
}

@Composable
private fun Content(viewState: TripPlanViewState, actions: TripPlanScreen.Actions, modifier: Modifier = Modifier) {
    val style = MapPlatformStyle.current
    Column(
        modifier = modifier.fillMaxWidth().padding(style.sheetContentPadding),
        verticalArrangement = Arrangement.spacedBy(style.sheetSpacing),
    ) {
        if (viewState.map.isOutsideCoveredArea) {
            NoteCard(
                title = stringResource(MR.strings.map_outside_title),
                text = stringResource(MR.strings.map_outside_text),
                icon = painterResource(MR.images.map),
            )
        }
        if (viewState.showsPreciseLocationNote) {
            NoteCard(
                title = stringResource(MR.strings.map_precise_title),
                text = stringResource(MR.strings.map_precise_text),
                icon = painterResource(style.preciseLocationIcon),
                actionLabel = stringResource(MR.strings.map_precise_action),
                onAction = actions::onPreciseLocationClick,
                close = IconAction(
                    icon = painterResource(MR.images.close),
                    label = stringResource(MR.strings.generic_close),
                    onClick = actions::onPreciseLocationNoteClose,
                ),
            )
        }
        SearchField(
            placeholder = stringResource(MR.strings.map_search_placeholder),
            icon = painterResource(MR.images.search),
            onClick = actions::onSearchClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@ComponentPreviews
@Composable
private fun TripPlanSheetPreview() = Showcase {
    Content(viewState = TripPlanViewState(), actions = noOpActions())
}

@ComponentPreviews
@Composable
private fun TripPlanSheetIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(viewState = TripPlanViewState(), actions = noOpActions())
}

@ComponentPreviews
@Composable
private fun TripPlanSheetPreciseLocationPreview() = Showcase {
    Content(viewState = TripPlanScreenPreviews.approximateViewState(), actions = noOpActions())
}

@ComponentPreviews
@Composable
private fun TripPlanSheetPreciseLocationIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(viewState = TripPlanScreenPreviews.approximateViewState(), actions = noOpActions())
}

@ComponentPreviews
@Composable
private fun TripPlanSheetOutsidePreview() = Showcase {
    Content(viewState = TripPlanScreenPreviews.outsideViewState(), actions = noOpActions())
}

@ComponentPreviews
@Composable
private fun TripPlanSheetOutsideIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(viewState = TripPlanScreenPreviews.outsideViewState(), actions = noOpActions())
}
