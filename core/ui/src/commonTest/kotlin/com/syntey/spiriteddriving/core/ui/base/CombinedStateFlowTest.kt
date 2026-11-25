package com.syntey.spiriteddriving.core.ui.base

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.merge
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
        val derived = derivedStateFlow(changes = merge(a, b, c)) { "${a.value}${b.value}${c.value}" }

        assertEquals("1xtrue", derived.value)

        a.value = 2
        assertEquals("2xtrue", derived.value)

        c.value = false
        b.value = "y"
        assertEquals("2yfalse", derived.value)
    }

    @Test
    fun `collect starts with the current value`() = runTest {
        val source = MutableStateFlow(4)
        val derived = derivedStateFlow(changes = source) { source.value * 2 }
        val emitted = mutableListOf<Int>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { derived.collect { emitted += it } }

        advanceUntilIdle()

        assertEquals(listOf(8), emitted)
    }

    @Test
    fun `collect emits each distinct value as the changes come`() = runTest {
        val a = MutableStateFlow(1)
        val b = MutableStateFlow(0)
        val derived = derivedStateFlow(changes = merge(a, b)) { a.value + b.value }
        val emitted = mutableListOf<Int>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { derived.collect { emitted += it } }

        a.value = 2
        b.value = 3
        advanceUntilIdle()

        assertEquals(listOf(1, 2, 5), emitted)
    }

    @Test
    fun `collect emits a change in a source that only the changes reach`() = runTest {
        val parent = MutableStateFlow("parent")
        val child = MutableStateFlow("first")
        // Nothing reads the value before the collector starts, so the child's change reaches the collector through changes alone
        val derived = derivedStateFlow(changes = merge(parent, child)) { "${parent.value}/${child.value}" }
        val emitted = mutableListOf<String>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { derived.collect { emitted += it } }

        child.value = "second"
        advanceUntilIdle()

        assertEquals(listOf("parent/first", "parent/second"), emitted)
    }

    @Test
    fun `collect skips a change that leaves the value the same`() = runTest {
        val a = MutableStateFlow(1)
        val b = MutableStateFlow(0)
        // Only the parity of a counts, so a change of a by two, or of b, leaves the value the same
        val derived = derivedStateFlow(changes = merge(a, b)) { a.value % 2 }
        val emitted = mutableListOf<Int>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { derived.collect { emitted += it } }

        a.value = 3
        a.value = 4
        a.value = 5
        advanceUntilIdle()

        assertEquals(listOf(1, 0, 1), emitted)
    }
}
