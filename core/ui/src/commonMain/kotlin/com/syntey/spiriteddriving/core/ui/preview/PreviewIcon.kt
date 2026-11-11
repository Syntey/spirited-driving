package com.syntey.spiriteddriving.core.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.syntey.spiriteddriving.core.ui.theme.Grid

/** A neutral stand-in icon for previews in :core:ui, which has no icons of its own. */
@Composable
internal fun previewIcon(): Painter = rememberVectorPainter(PreviewIconImage)

// A rounded square outline inset from the 24x24 box, so it sits where a Material icon's strokes do
private const val PREVIEW_VIEWPORT = 24f

private val PreviewIconImage: ImageVector = ImageVector.Builder(
    name = "PreviewIcon",
    defaultWidth = Grid.d6,
    defaultHeight = Grid.d6,
    viewportWidth = PREVIEW_VIEWPORT,
    viewportHeight = PREVIEW_VIEWPORT,
).addPath(
    pathData = addPathNodes("M8,5H16A3,3,0,0,1,19,8V16A3,3,0,0,1,16,19H8A3,3,0,0,1,5,16V8A3,3,0,0,1,8,5Z"),
    stroke = SolidColor(Color.Black),
    strokeLineWidth = 2f,
).build()
