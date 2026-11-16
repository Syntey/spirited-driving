package com.syntey.spiriteddriving.android.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.syntey.spiriteddriving.feature.profile.ui.aboutScreen.AboutScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.accountScreen.AccountScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.feedbackScreen.FeedbackScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen.LicenceTextScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.licencesScreen.LicencesScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.settingsScreen.SettingsScreenUi
import com.syntey.spiriteddriving.shared.navigation.profile.ProfileChild
import com.syntey.spiriteddriving.shared.navigation.profile.ProfileNavHost

/** Profile and the screens pushed from it, full screen over the map. */
@Composable
fun ProfileNavHostUi(
    navHost: ProfileNavHost,
    modifier: Modifier = Modifier,
) {
    val stack by navHost.stack.collectAsStateWithLifecycle()

    Children(
        stack = stack,
        modifier = modifier.fillMaxSize(),
        animation = stackAnimation(slide()),
    ) { child ->
        when (val instance = child.instance) {
            is ProfileChild.Profile -> ProfileScreenUi(screen = instance.screen, modifier = Modifier.fillMaxSize())

            is ProfileChild.Settings -> SettingsScreenUi(screen = instance.screen, modifier = Modifier.fillMaxSize())

            is ProfileChild.Feedback -> FeedbackScreenUi(screen = instance.screen, modifier = Modifier.fillMaxSize())

            is ProfileChild.About -> AboutScreenUi(screen = instance.screen, modifier = Modifier.fillMaxSize())

            is ProfileChild.Account -> AccountScreenUi(screen = instance.screen, modifier = Modifier.fillMaxSize())

            is ProfileChild.Licences -> LicencesScreenUi(screen = instance.screen, modifier = Modifier.fillMaxSize())

            is ProfileChild.LicenceText ->
                LicenceTextScreenUi(screen = instance.screen, modifier = Modifier.fillMaxSize())
        }
    }
}
