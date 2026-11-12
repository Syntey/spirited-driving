package com.syntey.spiriteddriving.feature.onboarding.ui

import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.feature.onboarding.ui.onboardingScreen.OnboardingComponentFactory
import com.syntey.spiriteddriving.feature.onboarding.ui.onboardingScreen.OnboardingScreen
import com.syntey.spiriteddriving.feature.onboarding.ui.onboardingScreen.OnboardingScreenNavigation

/**
 * The screens this feature offers to the navigation hosts in `:shared`. Components stay internal, so each screen is
 * created here, behind its public interface.
 */
object OnboardingScreens {

    fun onboarding(componentContext: AppComponentContext, navigation: OnboardingScreenNavigation): OnboardingScreen =
        OnboardingComponentFactory.createComponent(componentContext, navigation)
}
