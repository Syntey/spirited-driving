package com.syntey.spiriteddriving.android.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.feature.profile.ui.feedbackScreen.FeedbackScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.settingsScreen.SettingsScreenUi
import com.syntey.spiriteddriving.shared.navigation.profile.ProfileChild
import com.syntey.spiriteddriving.shared.navigation.profile.ProfileNavHost
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

/** Profile and the screens pushed from it, full screen over the map. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileNavHostUi(
    navHost: ProfileNavHost,
    modifier: Modifier = Modifier,
) {
    val stack by navHost.stack.collectAsStateWithLifecycle()
    val title = when (stack.active.instance) {
        is ProfileChild.Profile -> MR.strings.profile_screen_title
        is ProfileChild.Settings -> MR.strings.settings_screen_title
        is ProfileChild.Feedback -> MR.strings.feedback_screen_title
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(title), style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = navHost::onBack) {
                        Icon(
                            painter = painterResource(MR.images.back),
                            contentDescription = stringResource(MR.strings.generic_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { innerPadding ->
        Children(
            stack = stack,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            animation = stackAnimation(slide()),
        ) { child ->
            when (val instance = child.instance) {
                is ProfileChild.Profile -> ProfileScreenUi(screen = instance.screen, modifier = Modifier.fillMaxSize())
                is ProfileChild.Settings -> SettingsScreenUi(screen = instance.screen, modifier = Modifier.fillMaxSize())
                is ProfileChild.Feedback -> FeedbackScreenUi(screen = instance.screen, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
