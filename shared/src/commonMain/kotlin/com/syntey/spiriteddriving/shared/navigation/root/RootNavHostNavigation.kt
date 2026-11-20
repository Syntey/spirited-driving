package com.syntey.spiriteddriving.shared.navigation.root

import com.syntey.spiriteddriving.core.domain.savedroute.SavedRouteId
import com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen.SavedRoutesScreen
import com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen.SavedRoutesScreenNavigation
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetScreen
import com.syntey.spiriteddriving.feature.map.ui.mapSheetScreen.MapSheetScreenNavigation
import com.syntey.spiriteddriving.feature.onboarding.ui.onboardingScreen.OnboardingScreen
import com.syntey.spiriteddriving.feature.onboarding.ui.onboardingScreen.OnboardingScreenNavigation
import com.syntey.spiriteddriving.feature.profile.model.SignInReason
import com.syntey.spiriteddriving.feature.profile.ui.signInSheet.SignInSheetArgs
import com.syntey.spiriteddriving.feature.profile.ui.signInSheet.SignInSheetScreen
import com.syntey.spiriteddriving.feature.profile.ui.signInSheet.SignInSheetScreenNavigation
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.dismiss

internal interface RootNavHostNavigation :
    OnboardingScreenNavigation,
    MapSheetScreenNavigation,
    SignInSheetScreenNavigation,
    SavedRoutesScreenNavigation {
    val slotNavigator: SlotNavigation<RootConfig>
    val sheetNavigator: SlotNavigation<RootSheetConfig>
}

/**
 * [onSignInFinished] tells the feature that opened the sign-in sheet, by its reason, whether the user signed in.
 */
internal class RootNavHostNavigator(
    private val onSignInFinished: (SignInReason, Boolean) -> Unit,
    private val onSavedRouteChosen: (SavedRouteId) -> Unit,
) : RootNavHostNavigation {
    override val slotNavigator: SlotNavigation<RootConfig> = SlotNavigation()
    override val sheetNavigator: SlotNavigation<RootSheetConfig> = SlotNavigation()

    override fun OnboardingScreen.finish() = slotNavigator.activate(RootConfig.Map)

    override fun MapSheetScreen.openProfile() = sheetNavigator.activate(RootSheetConfig.Profile)

    override fun SavedRoutesScreen.close() = sheetNavigator.dismiss()

    // The list closes, and the map shows the route on its own
    override fun SavedRoutesScreen.openOnMap(savedRouteId: SavedRouteId) {
        sheetNavigator.dismiss()
        onSavedRouteChosen(savedRouteId)
    }

    override fun MapSheetScreen.openSignInToSaveRoute() = openSignIn(SignInReason.SaveRoute)

    override fun MapSheetScreen.openSavedRoutes() = sheetNavigator.activate(RootSheetConfig.SavedRoutes)

    override fun MapSheetScreen.openSignInForSavedRoutes() = openSignIn(SignInReason.SavedRoutes)

    override fun SignInSheetScreen.finish(reason: SignInReason, isSignedIn: Boolean) {
        sheetNavigator.dismiss()
        onSignInFinished(reason, isSignedIn)
    }

    private fun openSignIn(reason: SignInReason) = sheetNavigator.activate(RootSheetConfig.SignIn(SignInSheetArgs(reason)))
}
