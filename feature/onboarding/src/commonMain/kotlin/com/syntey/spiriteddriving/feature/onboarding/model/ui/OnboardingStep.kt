package com.syntey.spiriteddriving.feature.onboarding.model.ui

/**
 * The pages of First launch, in order.
 */
enum class OnboardingStep {
    WhatItIs,
    Budget,
    HandOff,
    Location,
    Vignette,
    ;

    /**
     * The intro pages have page dots and can be skipped. Location and Vignette are the questions after them.
     */
    val isIntro: Boolean get() = this <= HandOff

    val next: OnboardingStep get() = entries[(ordinal + 1).coerceAtMost(entries.lastIndex)]

    val previous: OnboardingStep get() = entries[(ordinal - 1).coerceAtLeast(0)]
}
