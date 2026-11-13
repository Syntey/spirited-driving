package com.syntey.spiriteddriving.shared.navigation.profile

import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pushNew
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileScreen
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileScreenNavigation
import com.syntey.spiriteddriving.feature.profile.ui.settingsScreen.SettingsScreenNavigation

internal interface ProfileNavHostNavigation :
    ProfileScreenNavigation,
    SettingsScreenNavigation {
    val stackNavigator: StackNavigation<ProfileConfig>
}

internal class ProfileNavHostNavigator : ProfileNavHostNavigation {
    override val stackNavigator: StackNavigation<ProfileConfig> = StackNavigation()

    override fun ProfileScreen.openSettings() = stackNavigator.pushNew(ProfileConfig.Settings)
}
