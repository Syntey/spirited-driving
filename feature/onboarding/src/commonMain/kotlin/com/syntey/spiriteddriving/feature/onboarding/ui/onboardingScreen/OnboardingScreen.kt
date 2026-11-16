package com.syntey.spiriteddriving.feature.onboarding.ui.onboardingScreen

import com.syntey.spiriteddriving.feature.onboarding.model.ui.OnboardingStep
import kotlinx.coroutines.flow.StateFlow

interface OnboardingScreen {
    val viewState: StateFlow<OnboardingViewState>
    val actions: Actions

    interface Actions {
        fun onNext()
        fun onSkip()

        /** The user swiped to another page. */
        fun onSwiped(step: OnboardingStep)

        /** The user swiped on from the last page a swipe can reach, [OnboardingViewState.lastSwipeable]. */
        fun onSwipedOn()

        /** Continue on the location page, which shows the system prompt. */
        fun onContinue()
        fun onVignetteAnswered(hasVignette: Boolean)

        /** Tapped Terms in the terms line on the first page. */
        fun onTermsClick()

        /** Tapped Privacy policy in the terms line on the first page. */
        fun onPrivacyPolicyClick()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onNext() = Unit
                override fun onSkip() = Unit
                override fun onSwiped(step: OnboardingStep) = Unit
                override fun onSwipedOn() = Unit
                override fun onContinue() = Unit
                override fun onVignetteAnswered(hasVignette: Boolean) = Unit
                override fun onTermsClick() = Unit
                override fun onPrivacyPolicyClick() = Unit
            }
        }
    }
}
