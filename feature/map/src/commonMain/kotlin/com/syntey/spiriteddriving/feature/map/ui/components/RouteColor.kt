package com.syntey.spiriteddriving.feature.map.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.syntey.spiriteddriving.core.ui.theme.AppTheme

@Composable
@ReadOnlyComposable
internal fun routeColor(number: Int?): Color {
    val colors = AppTheme.customColors
    return when (number) {
        null -> colors.routeFastest
        1 -> colors.routeFun1
        2 -> colors.routeFun2
        else -> colors.routeFun3
    }
}
