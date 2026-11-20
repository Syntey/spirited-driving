package com.syntey.spiriteddriving.core.ui.base

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CombinedStateFlowTest {

    @Test
    fun `value follows each source as soon as it changes`() {
        val a = MutableStateFlow(1)
        val b = MutableStateFlow("x")
        val c = MutableStateFlow(true)
        val combined = combineStates(a, b, c) { valueA, valueB, valueC -> "$valueA$valueB$valueC" }

        assertEquals("1xtrue", combined.value)

        a.value = 2
        assertEquals("2xtrue", combined.value)

        c.value = false
        b.value = "y"
        assertEquals("2yfalse", combined.value)
    }

    @Test
    fun `collect emits each distinct combined value as the sources change`() = runTest {
        val a = MutableStateFlow(1)
        val b = MutableStateFlow(0)
        val c = MutableStateFlow(0)
        val combined = combineStates(a, b, c) { valueA, valueB, valueC -> valueA + valueB + valueC }
        val emitted = mutableListOf<Int>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { combined.collect { emitted += it } }

        a.value = 2
        b.value = 3
        c.value = 3
        advanceUntilIdle()

        assertEquals(listOf(1, 2, 5, 8), emitted)
    }

    @Test
    fun `collect skips a change that leaves the combined value the same`() = runTest {
        val a = MutableStateFlow(1)
        // Only the parity of a counts, so a change of a by two, or of b, leaves the combined value the same
        val combined = combineStates(a, MutableStateFlow(0), MutableStateFlow(0)) { valueA, _, _ -> valueA % 2 }
        val emitted = mutableListOf<Int>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { combined.collect { emitted += it } }

        a.value = 3
        a.value = 4
        a.value = 5
        advanceUntilIdle()

        assertEquals(listOf(1, 0, 1), emitted)
    }
}
