package com.syntey.spiriteddriving.feature.profile.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.DarkLightPreviews
import dev.icerock.moko.resources.compose.painterResource

/**
 * The road from first launch, the hairpin with both cars, at the top of Profile, edge to edge behind the status and
 * navigation bars, fading into the background at the bottom: day in light theme, dusk in dark. Decorative.
 */
@Composable
internal fun ProfileHeader(modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(HEADER_ASPECT_RATIO),
    ) {
        Image(
            painter = painterResource(if (isDark) MR.images.profile_header_dusk else MR.images.profile_header_day),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // The road fades into the screen's background instead of ending at an edge
        Spacer(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        FADE_START to Color.Transparent,
                        1f to MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                ),
        )
    }
}

// The crop's own shape, 420 × 270, so the cars stay whole at any width
private const val HEADER_ASPECT_RATIO = 420f / 270f

// The road fades into the screen's background instead of ending at an edge
private const val FADE_START = 0.75f

@DarkLightPreviews
@Composable
private fun ProfileHeaderPreview() = Showcase {
    ProfileHeader()
}
