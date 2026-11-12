package com.syntey.spiriteddriving.feature.onboarding.model.ui

import com.syntey.spiriteddriving.feature.onboarding.ui.onboardingScreen.OnboardingViewState
internal object OnboardingPreviewStates {
    fun viewState(step: OnboardingStep = OnboardingStep.WhatItIs) = OnboardingViewState(step = step)
}
