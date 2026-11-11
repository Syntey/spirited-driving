package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid

/**
 * The app's progress indicator, the same on both platforms: a car seen from above, spinning like a drift. Each turn
 * springs to the next with a little overshoot. With reduced motion it pulses in place instead. Screen readers hear it
 * as in progress.
 */
@Composable
fun CarSpinner(modifier: Modifier = Modifier, color: Color = LocalContentColor.current) {
    val reduceMotion = AppTheme.reduceMotion
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(reduceMotion) {
        if (reduceMotion) {
            rotation.snapTo(0f)
        } else {
            while (true) {
                rotation.animateTo(
                    rotation.value + FULL_TURN,
                    spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow, visibilityThreshold = 1f),
                )
                rotation.snapTo(rotation.value % FULL_TURN)
            }
        }
    }
    // Only with reduced motion: otherwise nothing reads the pulse, and its animation would tick every frame for nothing
    val pulse = if (reduceMotion) {
        rememberInfiniteTransition(label = "carSpinnerPulse").animateFloat(
            initialValue = PULSE_MIN_ALPHA,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(PULSE_DURATION_MILLIS), RepeatMode.Reverse),
            label = "carSpinnerPulseAlpha",
        )
    } else {
        null
    }

    // Rotation and alpha are read in the draw phase, so the spinner doesn't recompose on every frame
    Canvas(modifier = modifier.progressSemantics().size(Grid.d6)) {
        if (reduceMotion) {
            drawCar(color.copy(alpha = color.alpha * (pulse?.value ?: 1f)))
        } else {
            rotate(degrees = rotation.value, pivot = center) { drawCar(color) }
        }
    }
}

// The body is one rounded rect with the two windows cut out of it, so the windows show whatever is under the button.
private fun DrawScope.drawCar(color: Color) {
    val length = size.height * CarShape.LENGTH
    val width = size.height * CarShape.WIDTH
    val body = Rect(
        left = (size.width - width) / 2f,
        top = (size.height - length) / 2f,
        right = (size.width + width) / 2f,
        bottom = (size.height + length) / 2f,
    )

    val carPath = Path().apply {
        fillType = PathFillType.EvenOdd
        addRoundRect(RoundRect(body, CornerRadius(width * CarShape.CORNER)))
        addWindow(
            body = body,
            fromFraction = CarShape.WINDSCREEN_FRONT,
            toFraction = CarShape.WINDSCREEN_BACK,
            fromInsetFraction = CarShape.WINDSCREEN_FRONT_INSET,
            toInsetFraction = CarShape.WINDSCREEN_BACK_INSET,
        )
        addWindow(
            body = body,
            fromFraction = CarShape.REAR_WINDOW_FRONT,
            toFraction = CarShape.REAR_WINDOW_BACK,
            fromInsetFraction = CarShape.REAR_WINDOW_FRONT_INSET,
            toInsetFraction = CarShape.REAR_WINDOW_BACK_INSET,
        )
    }
    drawPath(path = carPath, color = color)
}

// A trapezoid across the body between two fractions of its length. Each edge is inset by a fraction of the width.
private fun Path.addWindow(
    body: Rect,
    fromFraction: Float,
    toFraction: Float,
    fromInsetFraction: Float,
    toInsetFraction: Float,
) {
    val fromY = body.top + body.height * fromFraction
    val toY = body.top + body.height * toFraction
    val fromInset = body.width * fromInsetFraction
    val toInset = body.width * toInsetFraction
    moveTo(body.left + fromInset, fromY)
    lineTo(body.right - fromInset, fromY)
    lineTo(body.right - toInset, toY)
    lineTo(body.left + toInset, toY)
    close()
}

private const val FULL_TURN = 360f
private const val PULSE_MIN_ALPHA = 0.35f
private const val PULSE_DURATION_MILLIS = 800

// The car's proportions as fractions of the spinner's height, from the nose (top) to the tail, and of the body's width.
private object CarShape {
    const val LENGTH = 0.9f
    const val WIDTH = 0.46f
    const val CORNER = 0.35f
    const val WINDSCREEN_FRONT = 0.22f
    const val WINDSCREEN_BACK = 0.38f
    const val WINDSCREEN_FRONT_INSET = 0.2f
    const val WINDSCREEN_BACK_INSET = 0.12f
    const val REAR_WINDOW_FRONT = 0.70f
    const val REAR_WINDOW_BACK = 0.80f
    const val REAR_WINDOW_FRONT_INSET = 0.12f
    const val REAR_WINDOW_BACK_INSET = 0.2f
}

@ComponentPreviews
@Composable
private fun CarSpinnerPreview() = Showcase {
    Row(
        Modifier.padding(Grid.d4),
        horizontalArrangement = Arrangement.spacedBy(Grid.d4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CarSpinner(Modifier.size(Grid.d4_5), color = MaterialTheme.colorScheme.onSurface)
        CarSpinner(Modifier.size(Grid.d6), color = MaterialTheme.colorScheme.onSurface)
        CarSpinner(Modifier.size(Grid.d12), color = MaterialTheme.colorScheme.onSurface)
    }
}

@ComponentPreviews
@Composable
private fun CarSpinnerOnPrimaryPreview() = Showcase {
    Box(
        Modifier
            .padding(Grid.d4)
            .background(MaterialTheme.colorScheme.primary)
            .padding(Grid.d2),
    ) {
        CarSpinner(Modifier.size(Grid.d6), color = MaterialTheme.colorScheme.onPrimary)
    }
}
