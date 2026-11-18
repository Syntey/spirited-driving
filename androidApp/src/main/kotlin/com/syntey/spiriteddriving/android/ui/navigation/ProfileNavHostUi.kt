package com.syntey.spiriteddriving.android.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.router.slot.ChildSlot
import com.syntey.spiriteddriving.core.ui.components.DragHandle
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.feature.profile.ui.aboutScreen.AboutScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.accountScreen.AccountScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.feedbackScreen.FeedbackScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.licenceTextScreen.LicenceTextScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.licencesScreen.LicencesScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.settingsScreen.SettingsScreenUi
import com.syntey.spiriteddriving.feature.profile.ui.signInSheet.SignInSheetScreen
import com.syntey.spiriteddriving.feature.profile.ui.signInSheet.SignInSheetScreenUi
import com.syntey.spiriteddriving.feature.routes.ui.tripHistoryScreen.TripHistoryScreenUi
import com.syntey.spiriteddriving.shared.navigation.profile.ProfileChild
import com.syntey.spiriteddriving.shared.navigation.profile.ProfileNavHost
import com.syntey.spiriteddriving.shared.navigation.profile.ProfileSheetChild
import com.syntey.spiriteddriving.shared.navigation.profile.ProfileSheetConfig

/** Profile and the screens pushed from it, full screen over the map, with its sign-in sheet over them. */
@Composable
fun ProfileNavHostUi(
    navHost: ProfileNavHost,
    modifier: Modifier = Modifier,
) {
    val stack by navHost.stack.collectAsStateWithLifecycle()
    val sheet: ChildSlot<ProfileSheetConfig, ProfileSheetChild> by navHost.sheet.collectAsStateWithLifecycle()

    Box(modifier.fillMaxSize()) {
        Children(
            stack = stack,
            modifier = Modifier.fillMaxSize(),
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

                is ProfileChild.TripHistory -> TripHistoryScreenUi(screen = instance.screen, modifier = Modifier.fillMaxSize())
            }
        }
        // A bottom sheet over the screen it was opened from, as the root's sign-in sheet is on Android
        when (val sheetInstance = sheet.child?.instance) {
            is ProfileSheetChild.SignIn -> SignInBottomSheet(screen = sheetInstance.screen)
            null -> Unit
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignInBottomSheet(screen: SignInSheetScreen) {
    ModalBottomSheet(
        onDismissRequest = { screen.actions.onClose() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = Grid.d8, topEnd = Grid.d8),
        dragHandle = { DragHandle() },
    ) {
        SignInSheetScreenUi(screen = screen)
    }
}
