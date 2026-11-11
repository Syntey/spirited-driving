// The controllers below are named for the Swift side that calls them, hence the PascalCase.
@file:Suppress("FunctionNaming", "ktlint:standard:function-naming")

package com.syntey.spiriteddriving.feature.onboarding

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.syntey.spiriteddriving.core.ui.fullScreenViewController
import com.syntey.spiriteddriving.feature.onboarding.onboardingScreen.OnboardingScreen
import com.syntey.spiriteddriving.feature.onboarding.onboardingScreen.OnboardingScreenUi
import platform.UIKit.UIViewController

// The illustration fills the whole screen, so the screen keeps its controls clear of the system bars itself.
fun OnboardingScreenUiController(screen: OnboardingScreen): UIViewController = fullScreenViewController {
    OnboardingScreenUi(screen = screen, modifier = Modifier.fillMaxSize())
}
