package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.preview.previewIcon
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform

// Google's branding guidelines: the light style is white with a grey outline, the dark style #131314 with a lighter outline.
// The colours are fixed by the guidelines, so they don't follow the theme.
private val googleLightContainer = Color(0xFFFFFFFF)
private val googleLightBorder = Color(0xFF747775)
private val googleLightText = Color(0xFF1F1F1F)
private val googleDarkContainer = Color(0xFF131314)
private val googleDarkBorder = Color(0xFF8E918F)
private val googleDarkText = Color(0xFFE3E3E3)

/**
 * The Continue with Google button, drawn to Google's branding guidelines in the app's light or dark style.
 *
 * [logo] is the multi-colour Google "G", never tinted. While [isLoading], a spinner takes its place. The button is read
 * by its [text]; the logo is decorative.
 */
@Composable
fun GoogleSignInButton(
    text: String,
    logo: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
) {
    val style = ButtonDefaultStyles.large()
    BaseButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        colors = googleButtonColors(enabled),
        style = style,
        enabled = enabled,
        isLoading = isLoading,
        leadingIcon = {
            Image(painter = logo, contentDescription = null, modifier = Modifier.size(style.iconSize))
        },
    )
}

@Composable
private fun googleButtonColors(enabled: Boolean): AppButtonColors {
    val isDark = AppTheme.customColors.isDark
    val container = if (isDark) googleDarkContainer else googleLightContainer
    val content = if (isDark) googleDarkText else googleLightText
    val border = if (isDark) googleDarkBorder else googleLightBorder
    return AppButtonColors(
        containerColor = container,
        contentColor = content,
        disabledContainerColor = container.copy(alpha = ButtonDefaultColors.DISABLED_CONTAINER_ALPHA),
        disabledContentColor = content.copy(alpha = ButtonDefaultColors.DISABLED_CONTENT_ALPHA),
        // BaseButton doesn't dim the outline, so it's dimmed here with the content
        borderColor = if (enabled) border else border.copy(alpha = ButtonDefaultColors.DISABLED_CONTENT_ALPHA),
    )
}

@ComponentPreviews
@Composable
private fun GoogleSignInButtonPreview() = Showcase {
    GoogleSignInButtonExamples()
}

@ComponentPreviews
@Composable
private fun GoogleSignInButtonIosPreview() = Showcase(platform = UiPlatform.Ios) {
    GoogleSignInButtonExamples()
}

@Composable
private fun GoogleSignInButtonExamples() {
    Column(Modifier.padding(Grid.d4), verticalArrangement = Arrangement.spacedBy(Grid.d2)) {
        GoogleSignInButton(
            text = "Continue with Google",
            logo = previewIcon(),
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
        GoogleSignInButton(
            text = "Continue with Google",
            logo = previewIcon(),
            onClick = {},
            isLoading = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
