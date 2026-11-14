package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.feature.map.tripPlanScreen.TripPlanScreen.Actions.Companion.noOpActions

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchOfflinePreview() = Showcase {
    Content(
        viewState = TripPlanScreenPreviews.offlineViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchOfflineIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanScreenPreviews.offlineViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchFailedPreview() = Showcase {
    Content(
        viewState = TripPlanScreenPreviews.failedViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchFailedIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanScreenPreviews.failedViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchNoResultsPreview() = Showcase {
    Content(
        viewState = TripPlanScreenPreviews.noResultsViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchNoResultsIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanScreenPreviews.noResultsViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchSlowPreview() = Showcase {
    Content(
        viewState = TripPlanScreenPreviews.slowSearchViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchSlowIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanScreenPreviews.slowSearchViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchDistancesPreview() = Showcase {
    Content(
        viewState = TripPlanScreenPreviews.searchDistancesViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}

@ComponentPreviews
@Composable
private fun TripPlanSheetSearchDistancesIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(
        viewState = TripPlanScreenPreviews.searchDistancesViewState(),
        actions = noOpActions(),
        onCollapsedHeightChange = {},
        modifier = Modifier.height(Grid.d100),
    )
}
