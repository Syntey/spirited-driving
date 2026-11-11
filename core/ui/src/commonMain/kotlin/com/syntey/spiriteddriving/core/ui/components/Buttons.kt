package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.preview.previewIcon
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.ButtonStyle
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform

/**
 * Every text button in the app, with a colour from [ButtonDefaultColors] and a size from [ButtonDefaultStyles].
 *
 * While [isLoading], the [CarSpinner] takes the place of [leadingIcon], or sits over the label when there's no leading icon.
 * The width and colours stay the same and taps are ignored.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: AppButtonColors = ButtonDefaultColors.primary(),
    style: ButtonStyle = ButtonDefaultStyles.large(),
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: Painter? = null,
    trailingIcon: Painter? = null,
) {
    BaseButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        colors = colors,
        style = style,
        shape = shape,
        enabled = enabled,
        isLoading = isLoading,
        leadingIcon = leadingIcon?.let { icon -> { ButtonIcon(icon, style) } },
        trailingIcon = trailingIcon?.let { icon -> { ButtonIcon(icon, style) } },
    )
}

/**
 * The button with its icons as slots, for an icon that must not be tinted, such as a brand logo on a sign-in button.
 */
@Composable
fun BaseButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: AppButtonColors = ButtonDefaultColors.primary(),
    style: ButtonStyle = ButtonDefaultStyles.large(),
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    Button(
        onClick = { if (!isLoading) onClick() },
        modifier = modifier.heightIn(min = style.height),
        enabled = enabled,
        shape = shape,
        contentPadding = style.contentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.containerColor,
            contentColor = colors.contentColor,
            disabledContainerColor = colors.disabledContainerColor,
            disabledContentColor = colors.disabledContentColor,
        ),
        border = if (colors.borderColor != Color.Transparent) BorderStroke(Grid.d0_25, colors.borderColor) else null,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Grid.d2)) {
            if (leadingIcon != null) {
                Crossfade(targetState = isLoading, label = "leadingIcon") { loading ->
                    if (loading) CarSpinner(Modifier.size(style.iconSize)) else leadingIcon()
                }
                ButtonLabel(text = text, style = style)
            } else {
                // The label keeps its size while loading, so the spinner centres over it without changing the width
                Crossfade(targetState = isLoading, label = "label") { loading ->
                    Box(contentAlignment = Alignment.Center) {
                        ButtonLabel(text = text, style = style, modifier = Modifier.alpha(if (loading) 0f else 1f))
                        if (loading) CarSpinner(Modifier.size(style.iconSize))
                    }
                }
            }
            trailingIcon?.invoke()
        }
    }
}

/** The colours of an [AppButton], taken from [ButtonDefaultColors]. */
@Immutable
data class AppButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val borderColor: Color,
)

/**
 * The colours of [AppButton]. Filled buttons disable to a faint container, text and error buttons to none.
 */
object ButtonDefaultColors {

    /** A screen's main action, such as Drive, Done or Next. */
    @Composable
    fun primary(): AppButtonColors = filled(
        container = MaterialTheme.colorScheme.primary,
        content = MaterialTheme.colorScheme.onPrimary,
    )

    /** A secondary action, such as Save, Try again or Rate. */
    @Composable
    fun tonal(): AppButtonColors = filled(
        container = MaterialTheme.colorScheme.secondaryContainer,
        content = MaterialTheme.colorScheme.onSecondaryContainer,
    )

    /** Accepting a hint, such as Turn on. */
    @Composable
    fun tertiary(): AppButtonColors = filled(
        container = MaterialTheme.colorScheme.tertiary,
        content = MaterialTheme.colorScheme.onTertiary,
    )

    /** A quiet action, such as "I didn't drive it". */
    @Composable
    fun text(): AppButtonColors = unfilled(MaterialTheme.colorScheme.primary)

    /** A destructive action in a Material dialog. */
    @Composable
    fun error(): AppButtonColors = unfilled(MaterialTheme.colorScheme.error)

    @Composable
    private fun filled(container: Color, content: Color) = AppButtonColors(
        containerColor = container,
        contentColor = content,
        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTAINER_ALPHA),
        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA),
        borderColor = Color.Transparent,
    )

    /**
     * A toggle button, such as a rating thumb: picked, it's on tertiaryContainer; otherwise it looks like an unselected chip,
     * outlined on Android and with a tinted fill on iOS.
     */
    @Composable
    fun toggle(selected: Boolean): AppButtonColors {
        val scheme = MaterialTheme.colorScheme
        val (container, content, border) = when {
            selected -> Triple(scheme.tertiaryContainer, scheme.onTertiaryContainer, Color.Transparent)
            AppTheme.platformStyle.chip.isOutlined -> Triple(Color.Transparent, scheme.onSurface, scheme.outline)
            else -> Triple(scheme.surfaceContainerHighest, scheme.onSurface, Color.Transparent)
        }
        return AppButtonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = scheme.onSurface.copy(alpha = DISABLED_CONTAINER_ALPHA),
            disabledContentColor = scheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA),
            borderColor = border,
        )
    }

    @Composable
    private fun unfilled(content: Color) = AppButtonColors(
        containerColor = Color.Transparent,
        contentColor = content,
        disabledContainerColor = Color.Transparent,
        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA),
        borderColor = Color.Transparent,
    )

    // Material's disabled alphas, so disabled buttons look like the rest of Material
    private const val DISABLED_CONTAINER_ALPHA = 0.12f
    private const val DISABLED_CONTENT_ALPHA = 0.38f
}

/**
 * The sizes of [AppButton]: each one is a platform's own height and type for that role.
 */
object ButtonDefaultStyles {

    @Composable
    @ReadOnlyComposable
    fun large(): ButtonStyle = AppTheme.platformStyle.buttonLarge

    @Composable
    @ReadOnlyComposable
    fun medium(): ButtonStyle = AppTheme.platformStyle.buttonMedium

    @Composable
    @ReadOnlyComposable
    fun small(): ButtonStyle = AppTheme.platformStyle.buttonSmall

    /** A toggle button with an icon, such as a rating thumb. */
    @Composable
    @ReadOnlyComposable
    fun toggle(): ButtonStyle = AppTheme.platformStyle.buttonToggle
}

@Composable
private fun ButtonIcon(icon: Painter, style: ButtonStyle) {
    Icon(painter = icon, contentDescription = null, modifier = Modifier.size(style.iconSize))
}

@Composable
private fun ButtonLabel(text: String, style: ButtonStyle, modifier: Modifier = Modifier) {
    Text(text = text, modifier = modifier, style = style.textStyle, textAlign = TextAlign.Center)
}

@ComponentPreviews
@Composable
private fun ButtonColorsPreview() = Showcase {
    Column(Modifier.padding(Grid.d4), verticalArrangement = Arrangement.spacedBy(Grid.d2)) {
        AppButton(text = "Drive", onClick = {}, colors = ButtonDefaultColors.primary(), modifier = Modifier.fillMaxWidth())
        AppButton(text = "Save", onClick = {}, colors = ButtonDefaultColors.tonal(), modifier = Modifier.fillMaxWidth())
        AppButton(text = "Turn on", onClick = {}, colors = ButtonDefaultColors.tertiary(), modifier = Modifier.fillMaxWidth())
        AppButton(text = "I didn't drive it", onClick = {}, colors = ButtonDefaultColors.text(), modifier = Modifier.fillMaxWidth())
        AppButton(text = "Delete", onClick = {}, colors = ButtonDefaultColors.error(), modifier = Modifier.fillMaxWidth())
        AppButton(
            text = "Drive",
            onClick = {},
            enabled = false,
            colors = ButtonDefaultColors.primary(),
            modifier = Modifier.fillMaxWidth(),
        )
        AppButton(
            text = "Save",
            onClick = {},
            enabled = false,
            colors = ButtonDefaultColors.tonal(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@ComponentPreviews
@Composable
private fun ButtonSizesPreview() = Showcase {
    Column(Modifier.padding(Grid.d4), verticalArrangement = Arrangement.spacedBy(Grid.d2)) {
        AppButton(text = "Large", onClick = {}, style = ButtonDefaultStyles.large(), modifier = Modifier.fillMaxWidth())
        AppButton(
            text = "Large with icon",
            onClick = {},
            style = ButtonDefaultStyles.large(),
            leadingIcon = previewIcon(),
            modifier = Modifier.fillMaxWidth(),
        )
        AppButton(text = "Medium", onClick = {}, colors = ButtonDefaultColors.tonal(), style = ButtonDefaultStyles.medium())
        AppButton(
            text = "Medium with icon",
            onClick = {},
            colors = ButtonDefaultColors.tonal(),
            style = ButtonDefaultStyles.medium(),
            leadingIcon = previewIcon(),
        )
        AppButton(text = "Small", onClick = {}, colors = ButtonDefaultColors.tonal(), style = ButtonDefaultStyles.small())
        AppButton(
            text = "Small with icon",
            onClick = {},
            colors = ButtonDefaultColors.tonal(),
            style = ButtonDefaultStyles.small(),
            leadingIcon = previewIcon(),
        )
    }
}

@ComponentPreviews
@Composable
private fun ButtonLoadingPreview() = Showcase {
    Column(Modifier.padding(Grid.d4), verticalArrangement = Arrangement.spacedBy(Grid.d2)) {
        AppButton(text = "Drive", onClick = {}, isLoading = true, modifier = Modifier.fillMaxWidth())
        AppButton(
            text = "Try again",
            onClick = {},
            colors = ButtonDefaultColors.tonal(),
            isLoading = true,
            leadingIcon = previewIcon(),
        )
        AppButton(
            text = "Rate",
            onClick = {},
            colors = ButtonDefaultColors.tonal(),
            style = ButtonDefaultStyles.small(),
            isLoading = true,
        )
    }
}

@ComponentPreviews
@Composable
private fun ButtonIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Column(Modifier.padding(Grid.d4), verticalArrangement = Arrangement.spacedBy(Grid.d2)) {
        AppButton(text = "Drive", onClick = {}, modifier = Modifier.fillMaxWidth())
        AppButton(
            text = "Try again",
            onClick = {},
            colors = ButtonDefaultColors.tonal(),
            style = ButtonDefaultStyles.medium(),
            leadingIcon = previewIcon(),
        )
        AppButton(text = "Rate", onClick = {}, colors = ButtonDefaultColors.tonal(), style = ButtonDefaultStyles.small())
        AppButton(text = "Drive", onClick = {}, isLoading = true, modifier = Modifier.fillMaxWidth())
    }
}
