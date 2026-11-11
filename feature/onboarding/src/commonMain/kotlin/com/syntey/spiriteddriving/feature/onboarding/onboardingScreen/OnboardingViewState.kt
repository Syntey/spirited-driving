package com.syntey.spiriteddriving.feature.onboarding.onboardingScreen

/**
 * [lastSwipeable] is the last page a swipe can reach. It's Location until the system prompt is answered, as swiping on from
 * there shows the prompt, and then Vignette.
 */
data class OnboardingViewState(
    val step: OnboardingStep = OnboardingStep.WhatItIs,
    val lastSwipeable: OnboardingStep = OnboardingStep.Location,
)
