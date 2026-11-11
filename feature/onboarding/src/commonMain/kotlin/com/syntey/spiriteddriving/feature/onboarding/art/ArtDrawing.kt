// Each helper takes what its SVG element has, and only the generated scene calls them
@file:Suppress("LongParameterList")

package com.syntey.spiriteddriving.feature.onboarding.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser

// What the generated scene in OnboardingArtScene.kt draws with: the shapes of SVG it's made of, in the panorama's units.

internal fun svgPath(pathData: String): Path = PathParser().parsePathString(pathData).toPath()

internal fun DrawScope.circle(cx: Float, cy: Float, radius: Float, color: Color, alpha: Float = 1f) =
    drawCircle(color = color, radius = radius, center = Offset(cx, cy), alpha = alpha)

internal fun DrawScope.ellipse(cx: Float, cy: Float, rx: Float, ry: Float, color: Color) =
    drawOval(color = color, topLeft = Offset(cx - rx, cy - ry), size = Size(rx * 2, ry * 2))

internal fun DrawScope.ellipseOutline(cx: Float, cy: Float, rx: Float, ry: Float, color: Color, width: Float) =
    drawOval(color = color, topLeft = Offset(cx - rx, cy - ry), size = Size(rx * 2, ry * 2), style = Stroke(width))

internal fun DrawScope.roundRect(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color, alpha: Float = 1f) =
    drawRoundRect(color = color, topLeft = Offset(x, y), size = Size(width, height), cornerRadius = CornerRadius(radius), alpha = alpha)

internal fun DrawScope.fillPath(path: Path, color: Color, alpha: Float = 1f) = drawPath(path = path, color = color, alpha = alpha)

internal fun DrawScope.strokePath(
    path: Path,
    color: Color,
    width: Float,
    round: Boolean = false,
    dash: FloatArray? = null,
    alpha: Float = 1f,
) = drawPath(
    path = path,
    color = color,
    alpha = alpha,
    style = Stroke(
        width = width,
        cap = if (round) StrokeCap.Round else StrokeCap.Butt,
        join = if (round) StrokeJoin.Round else StrokeJoin.Miter,
        pathEffect = dash?.let { PathEffect.dashPathEffect(it) },
    ),
)

/**
 * SVG's `rotate(degrees pivotX pivotY)`.
 */
internal inline fun DrawScope.rotated(degrees: Float, pivotX: Float, pivotY: Float, block: DrawScope.() -> Unit) =
    rotate(degrees = degrees, pivot = Offset(pivotX, pivotY), block = block)

/**
 * SVG's `translate(x y) rotate(degrees)`: draws [block] turned by [degrees] around its own origin, which is moved to [x], [y].
 */
internal inline fun DrawScope.placed(x: Float, y: Float, degrees: Float, block: DrawScope.() -> Unit) = withTransform(
    transformBlock = {
        translate(x, y)
        rotate(degrees, pivot = Offset.Zero)
    },
    drawBlock = block,
)
