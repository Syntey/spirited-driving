package com.syntey.spiriteddriving.shared.navigation.root

import com.syntey.spiriteddriving.shared.navigation.signedIn.SignedInConfig
import com.syntey.spiriteddriving.shared.navigation.signedIn.SignedInNavHost
import com.syntey.spiriteddriving.feature.onboarding.onboardingScreen.OnboardingScreen
import com.syntey.spiriteddriving.feature.sample.loginScreen.LoginScreen
import com.arkivanov.decompose.router.slot.ChildSlot
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface RootNavHost {
    val slot: StateFlow<ChildSlot<RootConfig, RootChild>>
}

@Serializable
sealed interface RootConfig {

    @Serializable
    data object Onboarding : RootConfig

    @Serializable
    data object Login : RootConfig

    @Serializable
    data class SignedIn(val initialConfig: SignedInConfig = SignedInConfig.Home()) : RootConfig
}

sealed interface RootChild {

    /**
     * Unique SwiftUI view identifier.
     * Each view that can get replaced in the slot (e.g. on sign in or sign out), must have a unique ID assigned to it.
     * (Otherwise, the view will lose the state and become unresponsive).
     */
    val iosViewId: String

    data class Onboarding(val screen: OnboardingScreen, override val iosViewId: String = Uuid.random().toString()) : RootChild

    data class Login(val screen: LoginScreen, override val iosViewId: String = Uuid.random().toString()) : RootChild

    data class SignedIn(val navHost: SignedInNavHost, override val iosViewId: String = Uuid.random().toString()) : RootChild
}
