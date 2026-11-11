// Durations, easing and the rubber band are the ones tuned in the First launch prototype
@file:Suppress("MagicNumber")

package com.syntey.spiriteddriving.feature.onboarding.onboardingScreen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Where the pages are while they move, as a fractional step: 1.5 is halfway between the second and the third page. The
 * illustration, the panel's pages, the dots and Skip all follow it. It settles on the component's step, and follows the
 * user's finger while they drag.
 */
@Stable
internal class PagePosition(step: OnboardingStep) {

    private val settled = Animatable(step.ordinal.toFloat())
    private var dragged: Float? by mutableStateOf(null)
    private var unbounded = 0f
    private var dragStart = 0
    private var lastReachable = 0

    val value: Float get() = dragged ?: settled.value

    /** The page nearest to [value], whose buttons show. */
    val nearest: OnboardingStep get() = OnboardingStep.entries[value.roundToInt().coerceIn(0, OnboardingStep.entries.lastIndex)]

    /**
     * How much Skip and the page dots show, from 1 to 0. They belong to the intro, and fade out over the second half of the
     * way from its last page to Location.
     */
    val introShown: Float get() = ((INTRO_GONE - value) / INTRO_FADE).coerceIn(0f, 1f)

    /** Starts a drag that can reach the pages up to [lastSwipeable], or up to the page it starts on if that's later. */
    suspend fun startDrag(lastSwipeable: OnboardingStep) {
        settled.stop()
        unbounded = settled.value
        dragStart = unbounded.roundToInt()
        lastReachable = maxOf(dragStart, lastSwipeable.ordinal)
        dragged = unbounded
    }

    /**
     * Moves by [pages], positive towards later pages. Before the first page, and past the last page the drag can reach, it
     * pulls back like a rubber band.
     */
    fun dragBy(pages: Float) {
        unbounded += pages
        dragged = when {
            unbounded < 0f -> -rubberBand(-unbounded)
            unbounded > lastReachable -> lastReachable + rubberBand(unbounded - lastReachable)
            else -> unbounded
        }
    }

    /**
     * Ends a drag. The page to settle on is, after a fling ([velocity] in pages per second), the next page in its direction,
     * otherwise the nearest one, and never more than a page away from where the drag started.
     */
    suspend fun endDrag(velocity: Float): DragEnd {
        val position = value
        val page = when {
            velocity > FLING_VELOCITY -> floor(position).toInt() + 1
            velocity < -FLING_VELOCITY -> ceil(position).toInt() - 1
            else -> position.roundToInt()
        }
        val swipedOn = dragStart == lastReachable && (velocity > FLING_VELOCITY || unbounded > lastReachable + 0.5f)
        settled.snapTo(position)
        dragged = null
        return DragEnd(
            page = OnboardingStep.entries[page.coerceIn((dragStart - 1).coerceAtLeast(0), (dragStart + 1).coerceAtMost(lastReachable))],
            swipedOn = swipedOn,
        )
    }

    /**
     * Moves to [step]: quickly after a fling, which already gave it speed, and slower after a button, taking longer the more
     * pages it passes. With [reduceMotion] it jumps there.
     */
    suspend fun settle(step: OnboardingStep, afterFling: Boolean, reduceMotion: Boolean) {
        val target = step.ordinal.toFloat()
        val alreadyThere = if (settled.isRunning) settled.targetValue == target else settled.value == target
        if (dragged != null || alreadyThere) return
        if (reduceMotion) {
            settled.snapTo(target)
            return
        }
        val pages = abs(target - settled.value)
        val spec = if (afterFling) {
            tween<Float>(durationMillis = (220 + 260 * pages).roundToInt().coerceIn(240, 520), easing = EaseOutCubic)
        } else {
            tween(durationMillis = (380 + 200 * pages).roundToInt().coerceIn(420, 1100), easing = EaseInOutCubic)
        }
        settled.animateTo(target, spec)
    }

    private companion object {
        val LAST_INTRO = OnboardingStep.HandOff.ordinal

        // Halfway from the last intro page to Location
        val INTRO_GONE = LAST_INTRO + 0.5f
        const val INTRO_FADE = 0.5f

        // About 300 dp a second on a phone
        const val FLING_VELOCITY = 0.75f

        val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
        val EaseInOutCubic = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)

        fun rubberBand(pages: Float) = pages * 0.4f / (1 + pages * 0.6f)
    }
}

/**
 * Where a drag ended: the [page] to settle on, and whether the user [swipedOn] from the last page the drag could reach, by a
 * fling or by pulling more than half a page on.
 */
internal data class DragEnd(val page: OnboardingStep, val swipedOn: Boolean)
