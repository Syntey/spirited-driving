package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.preview.previewIcon
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform

/** The colours of an [AndroidTopBar], taken from [AndroidTopBarDefaults]. */
@Immutable
data class AndroidTopBarColors(val containerColor: Color, val navigationContainerColor: Color)

/** The colours of [AndroidTopBar]: a solid bar, or a transparent one for a screen that draws a picture behind it. */
object AndroidTopBarDefaults {

    @Composable
    @ReadOnlyComposable
    fun solid(): AndroidTopBarColors = AndroidTopBarColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        navigationContainerColor = Color.Transparent,
    )

    /** For a screen that draws a picture behind the bar. */
    @Composable
    @ReadOnlyComposable
    fun overArt(): AndroidTopBarColors = AndroidTopBarColors(
        containerColor = Color.Transparent,
        navigationContainerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = NAVIGATION_ON_ART_ALPHA),
    )

    // Over a picture the navigation button needs its own background to stay readable
    private const val NAVIGATION_ON_ART_ALPHA = 0.85f
}

/**
 * A screen's navigation bar on Android, for the top bar of an [AppScaffold].
 *
 * Draws nothing on iOS: there the SwiftUI navigation stack draws a native bar, and the iOS screen sets its title and
 * toolbar buttons on it.
 *
 * @param title The bar's title, or null when the screen's picture is its heading.
 * @param navigation The button before the title, usually back; null on a root screen. Its label is what screen
 * readers hear.
 * @param actions The buttons at the end of the bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidTopBar(
    title: String?,
    modifier: Modifier = Modifier,
    navigation: IconAction? = null,
    colors: AndroidTopBarColors = AndroidTopBarDefaults.solid(),
    actions: @Composable RowScope.() -> Unit = {},
) {
    if (AppTheme.platform == UiPlatform.Ios) return
    TopAppBar(
        title = { title?.let { Text(text = it, style = MaterialTheme.typography.titleLarge) } },
        modifier = modifier,
        navigationIcon = {
            navigation?.let {
                IconButton(
                    onClick = it.onClick,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = colors.navigationContainerColor),
                ) {
                    Icon(painter = it.icon, contentDescription = it.label)
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.containerColor,
            scrolledContainerColor = colors.containerColor,
        ),
    )
}

@ComponentPreviews
@Composable
private fun AndroidTopBarPreview() = Showcase {
    AndroidTopBar(
        title = "Settings",
        navigation = IconAction(icon = previewIcon(), label = "Back", onClick = {}),
        actions = {
            IconButton(onClick = {}) { Icon(painter = previewIcon(), contentDescription = "Share") }
        },
    )
}

@ComponentPreviews
@Composable
private fun AndroidTopBarRootPreview() = Showcase {
    AndroidTopBar(title = "Profile")
}

@ComponentPreviews
@Composable
private fun AndroidTopBarOverArtPreview() = Showcase {
    Box(Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
        AndroidTopBar(
            title = null,
            navigation = IconAction(icon = previewIcon(), label = "Back", onClick = {}),
            colors = AndroidTopBarDefaults.overArt(),
        )
    }
}
