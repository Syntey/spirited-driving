package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.preview.previewIcon
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform

/** The colours of an [AppScaffold], taken from [AppScaffoldDefaultColors]. */
@Immutable
data class AppScaffoldColors(val containerColor: Color, val barColor: Color, val backContainerColor: Color)

/** The colours of [AppScaffold]: a solid bar, or a transparent one for a screen that draws a picture behind it. */
object AppScaffoldDefaultColors {

    @Composable
    fun solid(): AppScaffoldColors = AppScaffoldColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        barColor = MaterialTheme.colorScheme.surfaceContainerLow,
        backContainerColor = Color.Transparent,
    )

    /** For a screen that draws a picture behind the bar. */
    @Composable
    fun overArt(): AppScaffoldColors = AppScaffoldColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        barColor = Color.Transparent,
        backContainerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = BACK_ON_ART_ALPHA),
    )

    // Over a picture the back button needs its own background to stay readable
    private const val BACK_ON_ART_ALPHA = 0.85f
}

/**
 * The frame of a pushed screen: a back button and an optional title in a top bar, with the screen's content below it.
 *
 * Every pushed screen draws its content inside this. On Android it adds a top bar with the title and back button; on
 * iOS the SwiftUI navigation stack draws a native bar, so this shows only the content.
 *
 * @param title The bar's title, or null when the screen's picture is its heading.
 * @param back The back button; its label is what screen readers hear.
 * @param content The screen, given the padding below the bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String?,
    back: IconAction,
    modifier: Modifier = Modifier,
    colors: AppScaffoldColors = AppScaffoldDefaultColors.solid(),
    content: @Composable (PaddingValues) -> Unit,
) {
    if (AppTheme.platform == UiPlatform.Ios) {
        Box(modifier) { content(PaddingValues()) }
    } else {
        Scaffold(
            modifier = modifier,
            containerColor = colors.containerColor,
            topBar = {
                TopAppBar(
                    title = { title?.let { Text(text = it, style = MaterialTheme.typography.titleLarge) } },
                    navigationIcon = {
                        IconButton(
                            onClick = back.onClick,
                            colors = IconButtonDefaults.iconButtonColors(containerColor = colors.backContainerColor),
                        ) {
                            Icon(painter = back.icon, contentDescription = back.label)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.barColor,
                        scrolledContainerColor = colors.barColor,
                    ),
                )
            },
            content = content,
        )
    }
}

@ComponentPreviews
@Composable
private fun AppScaffoldSolidPreview() = Showcase {
    AppScaffold(
        title = "Settings",
        back = IconAction(icon = previewIcon(), label = "Back", onClick = {}),
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding))
    }
}

@ComponentPreviews
@Composable
private fun AppScaffoldIosPreview() = Showcase(platform = UiPlatform.Ios) {
    AppScaffold(
        title = "Settings",
        back = IconAction(icon = previewIcon(), label = "Back", onClick = {}),
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding))
    }
}

@ComponentPreviews
@Composable
private fun AppScaffoldOverArtPreview() = Showcase {
    AppScaffold(
        title = null,
        back = IconAction(icon = previewIcon(), label = "Back", onClick = {}),
        modifier = Modifier.fillMaxSize(),
        colors = AppScaffoldDefaultColors.overArt(),
    ) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer))
    }
}
