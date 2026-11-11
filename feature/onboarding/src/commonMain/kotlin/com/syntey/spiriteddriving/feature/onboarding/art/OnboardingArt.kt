package com.syntey.spiriteddriving.feature.onboarding.art

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.DarkLightPreviews

/**
 * Two light sports cars on a winding road, seen from above: a panorama of five pages that pans with [position], with the
 * cars driving along the road as it does. A day version goes with the light theme, a dusk one with the dark theme.
 * Decorative, so screen readers skip it.
 *
 * The panorama is scaled to fill the height, which keeps the cars above the panel at the bottom on every screen, and each
 * page is centred, so a narrow screen crops both sides alike.
 *
 * @param position The page to show, read while drawing: 0 is the first page, 1.5 halfway between the second and the third.
 */
@Composable
internal fun OnboardingArt(position: () -> Float, modifier: Modifier = Modifier) {
    val palette = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) ArtPalette.Dusk else ArtPalette.Day
    Box(
        modifier = modifier
            .clipToBounds()
            .background(palette.land)
            .clearAndSetSemantics {},
    ) {
        // Drawn once and moved as a whole layer, so panning doesn't draw the scenery again.
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationX = -size.leftEdge(position()) * size.unit }
                .drawBehind { scale(size.unit, pivot = Offset.Zero) { drawScenery(palette) } },
        )
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind { drawCars(palette, position()) },
        )
    }
}

private fun DrawScope.drawCars(palette: ArtPalette, position: Float) {
    val unit = size.unit
    val left = size.leftEdge(position)
    val lead = Cars.Default.leadAt(position)
    val follower = Cars.Default.followerAt(position)
    withTransform({
        scale(unit, unit, pivot = Offset.Zero)
        translate(-left, 0f)
    }) {
        placed(follower.x + ArtRoad.SHADOW_X, follower.y + ArtRoad.SHADOW_Y, follower.degrees) { drawGr86Shadow(palette) }
        placed(follower.x, follower.y, follower.degrees) { drawGr86(palette) }
        placed(lead.x + ArtRoad.SHADOW_X, lead.y + ArtRoad.SHADOW_Y, lead.degrees) { drawMx5Shadow(palette) }
        placed(lead.x, lead.y, lead.degrees) { drawMx5(palette) }
    }
}

// Pixels per unit of the panorama.
private val Size.unit: Float get() = height / ArtRoad.PAGE_HEIGHT

// The panorama's x at the left edge of the screen, in its units.
private fun Size.leftEdge(position: Float): Float {
    val visibleWidth = width / unit
    return ArtRoad.PAGE_WIDTH * position + (ArtRoad.PAGE_WIDTH - visibleWidth) / 2
}

@DarkLightPreviews
@Composable
private fun OnboardingArtPreview() = Showcase {
    OnboardingArt(position = { 2f }, modifier = Modifier.fillMaxWidth().aspectRatio(390f / 844f))
}
