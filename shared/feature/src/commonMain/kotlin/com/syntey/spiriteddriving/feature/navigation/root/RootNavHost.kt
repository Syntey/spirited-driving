package com.syntey.spiriteddriving.feature.navigation.root

import com.syntey.spiriteddriving.feature.navigation.signedIn.SignedInConfig
import com.syntey.spiriteddriving.feature.navigation.signedIn.SignedInNavHost
import com.syntey.spiriteddriving.feature.ui.loginScreen.LoginScreen
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
    data object Login : RootConfig

    @Serializable
    data class SignedIn(val initialConfig: SignedInConfig = SignedInConfig.Home()) : RootConfig
}

@OptIn(ExperimentalUuidApi::class)
sealed interface RootChild {

    /**
     * Unique SwiftUI view identifier.
     * Each view that can get replaced in the slot (e.g. on sign in or sign out), must have a unique ID assigned to it.
     * (Otherwise, the view will lose the state and become unresponsive).
     */
    val iosViewId: String

    data class Login(val screen: LoginScreen, override val iosViewId: String = Uuid.random().toString()) : RootChild

    data class SignedIn(val navHost: SignedInNavHost, override val iosViewId: String = Uuid.random().toString()) : RootChild
}
