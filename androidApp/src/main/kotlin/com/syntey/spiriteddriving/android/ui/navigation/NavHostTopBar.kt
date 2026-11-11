package com.syntey.spiriteddriving.android.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.mirrorIn
import com.syntey.spiriteddriving.core.ui.theme.Grid
import dev.icerock.moko.resources.compose.painterResource

/**
 * Top bar of a navigation stack.
 *
 * Shared screens do not draw a top bar, because on iOS it is provided by native `NavigationStack`.
 * On Android, it is drawn by the navigation host instead.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavHostTopBar(
    title: String,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        painter = painterResource(MR.images.back),
                        contentDescription = null,
                        modifier = Modifier.size(Grid.d6).mirrorIn(LocalLayoutDirection.current),
                    )
                }
            }
        },
    )
}
