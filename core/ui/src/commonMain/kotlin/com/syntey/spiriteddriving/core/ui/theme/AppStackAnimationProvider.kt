package com.syntey.spiriteddriving.core.ui.theme

import com.syntey.spiriteddriving.core.ui.tools.Constants
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimationProvider
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation

/**
 * Stack animation provides animation for all stacks, not necessary to implement fallback animation in children components.
 */
object AppStackAnimationProvider : StackAnimationProvider {
    override fun <C : Any, T : Any> provide(): StackAnimation<C, T> = stackAnimation(Constants.Navigation.STACK_ANIMATION_CROSS_FADE_SPEC)
}
