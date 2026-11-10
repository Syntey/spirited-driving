package com.syntey.spiriteddriving.shared.navigation.root

import com.syntey.spiriteddriving.shared.navigation.signedIn.SignedInNavHostComponent
import com.syntey.spiriteddriving.shared.navigation.signedIn.SignedInNavHostNavigation
import com.syntey.spiriteddriving.feature.sample.loginScreen.LoginScreen
import com.syntey.spiriteddriving.feature.sample.loginScreen.LoginScreenNavigation
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate

internal interface RootNavHostNavigation :
    LoginScreenNavigation,
    SignedInNavHostNavigation {
    val slotNavigator: SlotNavigation<RootConfig>
}

internal class RootNavHostNavigator : RootNavHostNavigation {
    override val slotNavigator: SlotNavigation<RootConfig> = SlotNavigation()

    override fun LoginScreen.navigateToSignedIn() {
        slotNavigator.activate(RootConfig.SignedIn())
    }

    override fun SignedInNavHostComponent.navigateToLogin() {
        slotNavigator.activate(RootConfig.Login)
    }
}
