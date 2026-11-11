package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.core.resources.*
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

@Suppress("UnusedParameter") // The sheet's steps read it as they're added
@Composable
private fun Content(viewState: TripPlanViewState, actions: TripPlanScreen.Actions, modifier: Modifier = Modifier) {
    SearchField(
        placeholder = stringResource(MR.strings.map_search_placeholder),
        icon = painterResource(MR.images.search),
        onClick = actions::onSearchClick,
        modifier = modifier.fillMaxWidth().padding(MapPlatformStyle.current.sheetContentPadding),
    )
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
