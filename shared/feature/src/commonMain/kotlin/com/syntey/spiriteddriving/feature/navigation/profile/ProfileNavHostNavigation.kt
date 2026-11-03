package com.syntey.spiriteddriving.feature.navigation.profile

import com.syntey.spiriteddriving.feature.ui.profileScreen.ProfileScreen
import com.syntey.spiriteddriving.feature.ui.profileScreen.ProfileScreenNavigation
import com.syntey.spiriteddriving.feature.ui.thirdScreen.ThirdComponent
import com.syntey.spiriteddriving.feature.ui.thirdScreen.ThirdScreenArgs
import com.syntey.spiriteddriving.feature.ui.thirdScreen.ThirdScreenNavigation
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew

internal interface ProfileNavHostNavigation :
    ProfileScreenNavigation,
    ThirdScreenNavigation {
    val stackNavigator: StackNavigation<ProfileConfig>
}

internal class ProfileNavHostNavigator(private val onNavigateToLogin: () -> Unit) : ProfileNavHostNavigation {
    override val stackNavigator = StackNavigation<ProfileConfig>()

    override fun ProfileScreen.navigateToLogin() = onNavigateToLogin()

    override fun ProfileScreen.navigateToThird(id: String) {
        stackNavigator.pushNew(ProfileConfig.Third(ThirdScreenArgs(id)))
    }

    override fun ThirdComponent.pop() {
        stackNavigator.pop()
    }
}
