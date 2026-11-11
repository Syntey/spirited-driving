package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

/**
 * The search field, here as a button that opens the search, such as "Where to?" on the map's sheet. A capsule:
 * a translucent fill on iOS, a Material search bar on Android.
 */
@Composable
internal fun SearchField(
    placeholder: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = MapPlatformStyle.current.searchField
    val container = if (style.isTranslucent) {
        AppTheme.customColors.systemFill
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    Row(
        modifier = modifier
            .heightIn(min = style.height)
            .clip(CircleShape)
            .background(container)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = style.horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(style.iconSize),
            tint = if (style.isTranslucent) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.width(style.iconGap))
        Text(
            text = placeholder,
            style = style.textStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@ComponentPreviews
@Composable
private fun SearchFieldPreview() = Showcase {
    SearchField(
        placeholder = stringResource(MR.strings.map_search_placeholder),
        icon = painterResource(MR.images.search),
        onClick = {},
        modifier = Modifier.fillMaxWidth().padding(Grid.d4),
    )
}

@ComponentPreviews
@Composable
private fun SearchFieldIosPreview() = Showcase(platform = UiPlatform.Ios) {
    SearchField(
        placeholder = stringResource(MR.strings.map_search_placeholder),
        icon = painterResource(MR.images.search),
        onClick = {},
        modifier = Modifier.fillMaxWidth().padding(Grid.d4),
    )
}
