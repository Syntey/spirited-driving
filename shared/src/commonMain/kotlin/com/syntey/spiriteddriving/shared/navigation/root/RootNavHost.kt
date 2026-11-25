package com.syntey.spiriteddriving.shared.navigation.root

import com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen.SavedRoutesScreen
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetScreen
import com.syntey.spiriteddriving.feature.onboarding.ui.onboardingScreen.OnboardingScreen
import com.syntey.spiriteddriving.feature.profile.ui.signInSheet.SignInSheetArgs
import com.syntey.spiriteddriving.feature.profile.ui.signInSheet.SignInSheetScreen
import com.syntey.spiriteddriving.shared.navigation.profile.ProfileNavHost
import com.arkivanov.decompose.router.slot.ChildSlot
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

interface RootNavHost {
    val slot: StateFlow<ChildSlot<RootConfig, RootChild>>

    /** Screens over the map, such as Profile. */
    val sheet: StateFlow<ChildSlot<RootSheetConfig, RootSheetChild>>
}

@Serializable
sealed interface RootConfig {

    @Serializable
    data object Onboarding : RootConfig

    @Serializable
    data object Map : RootConfig
}

@Serializable
sealed interface RootSheetConfig {

    @Serializable
    data object Profile : RootSheetConfig

    @Serializable
    data object SavedRoutes : RootSheetConfig

    @Serializable
    data class SignIn(val args: SignInSheetArgs) : RootSheetConfig
}

sealed interface RootSheetChild {

    data class Profile(val navHost: ProfileNavHost) : RootSheetChild

    data class SignIn(val screen: SignInSheetScreen) : RootSheetChild

    data class SavedRoutes(val screen: SavedRoutesScreen) : RootSheetChild
}

sealed interface RootChild {

    /**
     * Unique SwiftUI view identifier.
     * Each view that can get replaced in the slot (e.g. on sign in or sign out), must have a unique ID assigned to it.
     * (Otherwise, the view will lose the state and become unresponsive).
     */
    val iosViewId: String

    data class Onboarding(val screen: OnboardingScreen, override val iosViewId: String = Uuid.random().toString()) : RootChild

    data class Map(val screen: MapSheetScreen, override val iosViewId: String = Uuid.random().toString()) : RootChild
}
