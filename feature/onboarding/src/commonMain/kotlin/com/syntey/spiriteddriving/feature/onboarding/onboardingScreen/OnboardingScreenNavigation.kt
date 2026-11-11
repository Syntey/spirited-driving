package com.syntey.spiriteddriving.feature.onboarding.onboardingScreen

import app.futured.arkitekt.decompose.navigation.NavigationActions

interface OnboardingScreenNavigation : NavigationActions {

    /** First launch is done: the vignette is answered, and the app is ready to use. */
    fun OnboardingScreen.finish()
}
