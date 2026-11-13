package com.syntey.spiriteddriving.feature.profile.ui

import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileComponentFactory
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileScreen
import com.syntey.spiriteddriving.feature.profile.ui.profileScreen.ProfileScreenNavigation
import com.syntey.spiriteddriving.feature.profile.ui.settingsScreen.SettingsComponentFactory
import com.syntey.spiriteddriving.feature.profile.ui.settingsScreen.SettingsScreen
import com.syntey.spiriteddriving.feature.profile.ui.settingsScreen.SettingsScreenNavigation

/**
 * The screens this feature offers to the navigation hosts in `:shared`. Components stay internal, so each screen is
 * created here, behind its public interface.
 */
object ProfileScreens {

    fun profile(componentContext: AppComponentContext, navigation: ProfileScreenNavigation): ProfileScreen =
        ProfileComponentFactory.createComponent(componentContext, navigation)

    fun settings(componentContext: AppComponentContext, navigation: SettingsScreenNavigation): SettingsScreen =
        SettingsComponentFactory.createComponent(componentContext, navigation)
}
