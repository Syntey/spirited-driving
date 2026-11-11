package com.syntey.spiriteddriving.feature.onboarding.onboardingScreen

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PagePositionTest {

    @Test
    fun `a short slow drag goes back to the page it started on`() = runTest {
        val position = PagePosition(OnboardingStep.WhatItIs)

        position.startDrag(lastSwipeable = OnboardingStep.Location)
        position.dragBy(0.3f)

        assertEquals(OnboardingStep.WhatItIs, position.endDrag(velocity = 0f).page)
    }

    @Test
    fun `a slow drag past halfway goes on to the next page`() = runTest {
        val position = PagePosition(OnboardingStep.WhatItIs)

        position.startDrag(lastSwipeable = OnboardingStep.Location)
        position.dragBy(0.6f)

        assertEquals(OnboardingStep.Budget, position.endDrag(velocity = 0f).page)
    }

    @Test
    fun `a short fling goes on to the next page`() = runTest {
        val position = PagePosition(OnboardingStep.Budget)

        position.startDrag(lastSwipeable = OnboardingStep.Location)
        position.dragBy(0.1f)

        assertEquals(OnboardingStep.HandOff, position.endDrag(velocity = 2f).page)
    }

    @Test
    fun `a fling back goes to the previous page`() = runTest {
        val position = PagePosition(OnboardingStep.Budget)

        position.startDrag(lastSwipeable = OnboardingStep.Location)
        position.dragBy(-0.1f)

        assertEquals(OnboardingStep.WhatItIs, position.endDrag(velocity = -2f).page)
    }

    @Test
    fun `a long drag moves one page at most`() = runTest {
        val position = PagePosition(OnboardingStep.WhatItIs)

        position.startDrag(lastSwipeable = OnboardingStep.Location)
        position.dragBy(1.8f)

        assertEquals(OnboardingStep.Budget, position.endDrag(velocity = 2f).page)
    }

    @Test
    fun `swiping on from the last intro page reaches location`() = runTest {
        val position = PagePosition(OnboardingStep.HandOff)

        position.startDrag(lastSwipeable = OnboardingStep.Location)
        position.dragBy(0.6f)

        assertEquals(OnboardingStep.Location, position.endDrag(velocity = 0f).page)
    }

    @Test
    fun `pulling back on the first page stretches less than the finger moves`() = runTest {
        val position = PagePosition(OnboardingStep.WhatItIs)

        position.startDrag(lastSwipeable = OnboardingStep.Location)
        position.dragBy(-0.5f)

        assertTrue(position.value > -0.5f && position.value < 0f, "pulled to ${position.value}")
        assertEquals(OnboardingStep.WhatItIs, position.endDrag(velocity = -2f).page)
    }

    @Test
    fun `swiping back from location goes to the last intro page`() = runTest {
        val position = PagePosition(OnboardingStep.Location)

        position.startDrag(lastSwipeable = OnboardingStep.Location)
        position.dragBy(-0.6f)

        assertEquals(OnboardingStep.HandOff, position.endDrag(velocity = 0f).page)
    }

    @Test
    fun `swiping on from location before the prompt is answered stays there`() = runTest {
        val position = PagePosition(OnboardingStep.Location)

        position.startDrag(lastSwipeable = OnboardingStep.Location)
        position.dragBy(0.9f)

        assertTrue(position.value < OnboardingStep.Location.ordinal + 0.5f, "pulled to ${position.value}")
        assertEquals(OnboardingStep.Location, position.endDrag(velocity = 2f).page)
    }

    @Test
    fun `a fling on from location before the prompt is answered swipes on`() = runTest {
        val position = PagePosition(OnboardingStep.Location)

        position.startDrag(lastSwipeable = OnboardingStep.Location)
        position.dragBy(0.1f)

        assertEquals(DragEnd(page = OnboardingStep.Location, swipedOn = true), position.endDrag(velocity = 2f))
    }

    @Test
    fun `a slow pull past halfway on from location before the prompt is answered swipes on`() = runTest {
        val position = PagePosition(OnboardingStep.Location)

        position.startDrag(lastSwipeable = OnboardingStep.Location)
        position.dragBy(0.6f)

        assertEquals(DragEnd(page = OnboardingStep.Location, swipedOn = true), position.endDrag(velocity = 0f))
    }

    @Test
    fun `a short slow pull on from location stays without swiping on`() = runTest {
        val position = PagePosition(OnboardingStep.Location)

        position.startDrag(lastSwipeable = OnboardingStep.Location)
        position.dragBy(0.3f)

        assertEquals(DragEnd(page = OnboardingStep.Location, swipedOn = false), position.endDrag(velocity = 0f))
    }

    @Test
    fun `swiping on from location after the prompt is answered reaches the vignette`() = runTest {
        val position = PagePosition(OnboardingStep.Location)

        position.startDrag(lastSwipeable = OnboardingStep.Vignette)
        position.dragBy(0.1f)

        assertEquals(DragEnd(page = OnboardingStep.Vignette, swipedOn = false), position.endDrag(velocity = 2f))
    }

    @Test
    fun `swiping back from the vignette goes to location`() = runTest {
        val position = PagePosition(OnboardingStep.Vignette)

        position.startDrag(lastSwipeable = OnboardingStep.Vignette)
        position.dragBy(-0.1f)

        assertEquals(OnboardingStep.Location, position.endDrag(velocity = -2f).page)
    }

    @Test
    fun `skip and the dots show on the last intro page`() {
        assertEquals(1f, PagePosition(OnboardingStep.HandOff).introShown)
    }

    @Test
    fun `skip and the dots are gone on location`() {
        assertEquals(0f, PagePosition(OnboardingStep.Location).introShown)
    }

    @Test
    fun `skip and the dots fade back in while swiping back from location`() = runTest {
        val position = PagePosition(OnboardingStep.Location)

        position.startDrag(lastSwipeable = OnboardingStep.Location)
        position.dragBy(-0.75f)

        assertEquals(0.5f, position.introShown, absoluteTolerance = 0.001f)
    }
}
