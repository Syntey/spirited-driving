package com.syntey.spiriteddriving.shared.navigation.root

import com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen.SavedRoutesScreen
import com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen.SavedRoutesScreenNavigation
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanScreen
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanScreenNavigation
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
    TripPlanScreenNavigation,
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
    private val onSavedRouteChosen: (String) -> Unit,
) : RootNavHostNavigation {
    override val slotNavigator: SlotNavigation<RootConfig> = SlotNavigation()
    override val sheetNavigator: SlotNavigation<RootSheetConfig> = SlotNavigation()

    // Only one sign-in sheet is open at a time, and it's the one opened last that finishes
    private var signInReason: SignInReason? = null

    override fun OnboardingScreen.finish() = slotNavigator.activate(RootConfig.Map)

    override fun TripPlanScreen.openProfile() = sheetNavigator.activate(RootSheetConfig.Profile)

    override fun SavedRoutesScreen.close() = sheetNavigator.dismiss()

    // The list closes, and the map shows the route on its own
    override fun SavedRoutesScreen.openOnMap(savedRouteId: String) {
        sheetNavigator.dismiss()
        onSavedRouteChosen(savedRouteId)
    }

    override fun TripPlanScreen.openSignInToSaveRoute() = openSignIn(SignInReason.SaveRoute)

    override fun TripPlanScreen.openSavedRoutes() = sheetNavigator.activate(RootSheetConfig.SavedRoutes)

    override fun TripPlanScreen.openSignInForSavedRoutes() = openSignIn(SignInReason.SavedRoutes)

    override fun SignInSheetScreen.finish(isSignedIn: Boolean) {
        val reason = requireNotNull(signInReason) { "The sign-in sheet is opened with a reason" }
        sheetNavigator.dismiss()
        onSignInFinished(reason, isSignedIn)
    }

    private fun openSignIn(reason: SignInReason) {
        signInReason = reason
        sheetNavigator.activate(RootSheetConfig.SignIn(SignInSheetArgs(reason)))
    }
}
