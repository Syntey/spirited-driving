package com.syntey.spiriteddriving.android.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.feature.profile.ui.aboutScreen.AboutScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.feedbackScreen.FeedbackScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.settingsScreen.SettingsScreenUi
import com.syntey.spiriteddriving.shared.navigation.profile.ProfileChild
import com.syntey.spiriteddriving.shared.navigation.profile.ProfileNavHost
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

// Over the Profile picture the back button needs its own background to stay readable
private const val BUTTON_ON_ART_ALPHA = 0.85f

/** Profile and the screens pushed from it, full screen over the map. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileNavHostUi(
    navHost: ProfileNavHost,
    modifier: Modifier = Modifier,
) {
    val stack by navHost.stack.collectAsStateWithLifecycle()
    val isProfile = stack.active.instance is ProfileChild.Profile
    val title = when (stack.active.instance) {
        is ProfileChild.Profile -> MR.strings.profile_screen_title
        is ProfileChild.Settings -> MR.strings.settings_screen_title
        is ProfileChild.Feedback -> MR.strings.feedback_screen_title
        is ProfileChild.About -> MR.strings.about_screen_title
    }
    val barColor = if (isProfile) Color.Transparent else MaterialTheme.colorScheme.surfaceContainerLow

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            TopAppBar(
                title = {
                    // The picture at the top of Profile is its only heading
                    if (!isProfile) Text(text = stringResource(title), style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(
                        onClick = navHost::onBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isProfile) {
                                MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = BUTTON_ON_ART_ALPHA)
                            } else {
                                Color.Transparent
                            },
                        ),
                    ) {
                        Icon(
                            painter = painterResource(MR.images.back),
                            contentDescription = stringResource(MR.strings.generic_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = barColor,
                    scrolledContainerColor = barColor,
                ),
            )
        },
    ) { innerPadding ->
        Children(
            stack = stack,
            modifier = Modifier.fillMaxSize(),
            animation = stackAnimation(slide()),
        ) { child ->
            when (val instance = child.instance) {
                // Profile draws behind the transparent top bar and keeps its own content clear of the insets
                is ProfileChild.Profile -> ProfileScreenUi(screen = instance.screen, modifier = Modifier.fillMaxSize())

                is ProfileChild.Settings -> SettingsScreenUi(
                    screen = instance.screen,
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                )

                is ProfileChild.Feedback -> FeedbackScreenUi(
                    screen = instance.screen,
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                )

                is ProfileChild.About -> AboutScreenUi(
                    screen = instance.screen,
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                )
            }
        }
    }
}
