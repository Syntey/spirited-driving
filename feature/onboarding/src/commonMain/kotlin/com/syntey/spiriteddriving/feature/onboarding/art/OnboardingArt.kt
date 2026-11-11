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
import androidx.compose.ui.unit.Density
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.DarkLightPreviews
import com.syntey.spiriteddriving.core.ui.theme.Grid

/**
 * Two light sports cars on a winding road, seen from above: a panorama of five pages that pans with [position], with the
 * cars driving along the road as it does. A day version goes with the light theme, a dusk one with the dark theme.
 * Decorative, so screen readers skip it.
 *
 * The panorama fills the height, and each page is centred, so a narrow screen crops both sides alike. The cars keep clear
 * of the controls at the top and the panel at the bottom: where the panel is taller than in the design, as on a short
 * screen or with large text, the panorama moves up, and shrinks only when moving up would take the cars under the
 * controls ([ArtFit]).
 *
 * @param position The page to show, read while drawing: 0 is the first page, 1.5 halfway between the second and the third.
 * @param controlsBottom The bottom edge of the controls at the top, in pixels from the top, read while drawing.
 * @param panelTop The top edge of the panel at the bottom, in pixels from the top, read while drawing.
 */
@Composable
internal fun OnboardingArt(
    position: () -> Float,
    controlsBottom: () -> Float,
    panelTop: () -> Float,
    modifier: Modifier = Modifier,
) {
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
                .graphicsLayer {
                    val fit = fit(size, controlsBottom(), panelTop())
                    translationX = -fit.leftEdge(size.width, position()) * fit.unit
                    translationY = -fit.shiftUp
                }
                .drawBehind {
                    val unit = fit(size, controlsBottom(), panelTop()).unit
                    scale(unit, pivot = Offset.Zero) { drawScenery(palette) }
                },
        )
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind { drawCars(palette, position(), fit(size, controlsBottom(), panelTop())) },
        )
    }
}

private fun DrawScope.drawCars(palette: ArtPalette, position: Float, fit: ArtFit) {
    val left = fit.leftEdge(size.width, position)
    val lead = Cars.Default.leadAt(position)
    val follower = Cars.Default.followerAt(position)
    withTransform({
        translate(0f, -fit.shiftUp)
        scale(fit.unit, fit.unit, pivot = Offset.Zero)
        translate(-left, 0f)
    }) {
        placed(follower.x + ArtRoad.SHADOW_X, follower.y + ArtRoad.SHADOW_Y, follower.degrees) { drawGr86Shadow(palette) }
        placed(follower.x, follower.y, follower.degrees) { drawGr86(palette) }
        placed(lead.x + ArtRoad.SHADOW_X, lead.y + ArtRoad.SHADOW_Y, lead.degrees) { drawMx5Shadow(palette) }
        placed(lead.x, lead.y, lead.degrees) { drawMx5(palette) }
    }
}

// A little room between the cars and the controls, so they don't touch.
private fun Density.fit(size: Size, controlsBottom: Float, panelTop: Float): ArtFit {
    val gap = Grid.d4.toPx()
    return ArtFit.of(height = size.height, clearTop = controlsBottom + gap, clearBottom = panelTop - gap)
}

@DarkLightPreviews
@Composable
private fun OnboardingArtPreview() = Showcase {
    OnboardingArt(
        position = { 2f },
        controlsBottom = { 0f },
        panelTop = { Float.POSITIVE_INFINITY },
        modifier = Modifier.fillMaxWidth().aspectRatio(390f / 844f),
    )
}
