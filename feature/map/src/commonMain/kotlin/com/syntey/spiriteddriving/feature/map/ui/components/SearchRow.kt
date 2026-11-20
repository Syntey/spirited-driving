package com.syntey.spiriteddriving.feature.map.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.AppButton
import com.syntey.spiriteddriving.core.ui.components.ButtonDefaultColors
import com.syntey.spiriteddriving.core.ui.components.ButtonDefaultStyles
import com.syntey.spiriteddriving.core.ui.components.IconAction
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetScreen
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetViewState
import com.syntey.spiriteddriving.feature.map.model.ui.SheetHeight
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

@Composable
internal fun SearchRow(viewState: MapSheetViewState, actions: MapSheetScreen.Actions) {
    val style = MapPlatformStyle.current
    val isOpen = viewState.sheetHeight != SheetHeight.Collapsed
    val isStartStep = viewState.startSearch != null
    val back = IconAction(
        icon = painterResource(MR.images.back),
        label = stringResource(MR.strings.generic_close),
        onClick = actions::onSearchClose,
    )
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Grid.d2)) {
        // The field keeps its own copy of the text and only takes an empty text from outside, so the start step needs a fresh
        // field: the destination's text would stay in the old one, and the start field must be focused as it opens.
        key(isStartStep) {
            SearchField(
                text = viewState.search.text,
                onTextChange = actions::onSearchTextChange,
                placeholder = stringResource(
                    if (isStartStep) MR.strings.search_start_placeholder else MR.strings.map_search_placeholder,
                ),
                icon = painterResource(MR.images.search),
                clear = IconAction(
                    icon = painterResource(MR.images.close),
                    label = stringResource(MR.strings.search_clear),
                    onClick = actions::onSearchClear,
                ),
                onFocus = actions::onSearchFocus,
                back = back.takeIf { isOpen && style.closesSearchWithBackArrow },
                isLoading = viewState.search.showsProgress,
                modifier = Modifier.weight(1f),
                requestsFocus = isStartStep,
            )
        }
        // Cancel, not an X: the sheet stays and only collapses, and Cancel is how search fields close on iOS
        if (isOpen && !style.closesSearchWithBackArrow) {
            AppButton(
                text = stringResource(MR.strings.generic_cancel),
                onClick = actions::onSearchClose,
                colors = ButtonDefaultColors.text(),
                style = ButtonDefaultStyles.small(),
            )
        }
    }
}
