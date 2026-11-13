package com.syntey.spiriteddriving.shared.navigation.root

import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanScreen
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.TripPlanScreenNavigation
import com.syntey.spiriteddriving.feature.onboarding.ui.onboardingScreen.OnboardingScreen
import com.syntey.spiriteddriving.feature.onboarding.ui.onboardingScreen.OnboardingScreenNavigation
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate

internal interface RootNavHostNavigation :
    OnboardingScreenNavigation,
    TripPlanScreenNavigation {
    val slotNavigator: SlotNavigation<RootConfig>
    val sheetNavigator: SlotNavigation<RootSheetConfig>
}

internal class RootNavHostNavigator : RootNavHostNavigation {
    override val slotNavigator: SlotNavigation<RootConfig> = SlotNavigation()
    override val sheetNavigator: SlotNavigation<RootSheetConfig> = SlotNavigation()

    override fun OnboardingScreen.finish() = slotNavigator.activate(RootConfig.Map)

    override fun TripPlanScreen.openProfile() = sheetNavigator.activate(RootSheetConfig.Profile)
}
