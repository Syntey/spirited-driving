package com.syntey.spiriteddriving.core.ui.base

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * A [StateFlow] that combines three others with [transform]. Its [StateFlow.value] is computed from the sources' current values
 * on each read, so a read right after a change sees it; collectors get each distinct combined value as the sources change.
 */
fun <A, B, C, R> combineStates(
    a: StateFlow<A>,
    b: StateFlow<B>,
    c: StateFlow<C>,
    transform: (A, B, C) -> R,
): StateFlow<R> = CombinedStateFlow(a, b, c, transform)

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
private class CombinedStateFlow<A, B, C, R>(
    private val a: StateFlow<A>,
    private val b: StateFlow<B>,
    private val c: StateFlow<C>,
    private val transform: (A, B, C) -> R,
) : StateFlow<R> {

    override val value: R get() = transform(a.value, b.value, c.value)

    override val replayCache: List<R> get() = listOf(value)

    // Never completes, as a StateFlow doesn't: it waits to be cancelled once the sources' changes are passed on
    override suspend fun collect(collector: FlowCollector<R>): Nothing {
        combine(a, b, c) { valueA, valueB, valueC -> transform(valueA, valueB, valueC) }
            .distinctUntilChanged()
            .collect(collector)
        awaitCancellation()
    }
}
