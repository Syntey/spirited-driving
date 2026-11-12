package com.syntey.spiriteddriving.shared.navigation.root

import com.syntey.spiriteddriving.feature.map.tripPlanScreen.TripPlanScreenNavigation
import com.syntey.spiriteddriving.feature.onboarding.onboardingScreen.OnboardingScreen
import com.syntey.spiriteddriving.feature.onboarding.onboardingScreen.OnboardingScreenNavigation
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate

internal interface RootNavHostNavigation :
    OnboardingScreenNavigation,
    TripPlanScreenNavigation {
    val slotNavigator: SlotNavigation<RootConfig>
}

internal class RootNavHostNavigator : RootNavHostNavigation {
    override val slotNavigator: SlotNavigation<RootConfig> = SlotNavigation()

    override fun OnboardingScreen.finish() = slotNavigator.activate(RootConfig.Map)
}
