package com.syntey.spiriteddriving.feature.profile.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.ComponentPreviews
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import dev.icerock.moko.resources.compose.painterResource

/** Marks a row that opens a web page. Each platform has its own glyph: an arrow on iOS, open-in-new otherwise. */
@Composable
internal fun WebLinkIcon() {
    Icon(
        painter = painterResource(
            if (AppTheme.platform == UiPlatform.Ios) MR.images.arrow_up_right else MR.images.open_in_new,
        ),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(Grid.d5),
    )
}

@ComponentPreviews
@Composable
private fun WebLinkIconPreview() = Showcase {
    WebLinkIcon()
}
