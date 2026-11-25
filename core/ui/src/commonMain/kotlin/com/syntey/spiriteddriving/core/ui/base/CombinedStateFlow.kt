package com.syntey.spiriteddriving.core.ui.base

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * A [StateFlow] that [compute] builds from the states it reads. Its [StateFlow.value] is computed on each read, so a read right
 * after a change sees it; collectors get each distinct value as [changes] emits, which is when something it reads may have changed.
 */
fun <T> derivedStateFlow(changes: Flow<*>, compute: () -> T): StateFlow<T> = DerivedStateFlow(changes, compute)

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
private class DerivedStateFlow<T>(private val changes: Flow<*>, private val compute: () -> T) : StateFlow<T> {

    override val value: T get() = compute()

    override val replayCache: List<T> get() = listOf(value)

    // Never completes, as a StateFlow doesn't: it waits to be cancelled once the changes are passed on
    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        changes.map { compute() }
            .onStart { emit(compute()) }
            .distinctUntilChanged()
            .collect(collector)
        awaitCancellation()
    }
}
