package com.syntey.spiriteddriving.core.ui.base

import app.futured.arkitekt.crusecases.CoroutineScopeOwner
import app.futured.arkitekt.crusecases.FlowUseCase
import app.futured.arkitekt.crusecases.FlowUseCaseConfig
import app.futured.arkitekt.crusecases.error.UseCaseErrorHandler
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

context(coroutineScopeOwner: CoroutineScopeOwner, lifecycleOwner: LifecycleOwner)
fun <T> FlowUseCase<Unit, T>.executeWithLifecycle(config: FlowUseCaseConfig.Builder<T, T>.() -> Unit): Unit =
    executeWithLifecycle(Unit, config)

/**
 * Arkitekt's `execute`, but the flow is collected only while [lifecycleOwner] is started: collection is cancelled on
 * stop and starts again from scratch on the next start. The job is kept in `useCaseJobPool` under this use case, so
 * cancelling it there stops the use case for good, and it ends when the lifecycle is destroyed.
 */
@OptIn(ExperimentalCoroutinesApi::class)
context(coroutineScopeOwner: CoroutineScopeOwner, lifecycleOwner: LifecycleOwner)
fun <ARGS, T> FlowUseCase<ARGS, T>.executeWithLifecycle(args: ARGS, config: FlowUseCaseConfig.Builder<T, T>.() -> Unit) {
    val callbacks = FlowUseCaseConfig.Builder<T, T>().apply(config).build()
    if (callbacks.disposePrevious) {
        coroutineScopeOwner.useCaseJobPool[this]?.cancel()
    }
    val updates = build(args).flowOn(coroutineScopeOwner.getWorkerDispatcher())
    coroutineScopeOwner.useCaseJobPool[this] = lifecycleOwner.lifecycle.isStarted()
        .flatMapLatest { started -> if (started) updates else emptyFlow() }
        .onStart { callbacks.onStart() }
        .onEach { callbacks.onNext(it) }
        .onCompletion { error ->
            when {
                error is CancellationException -> Unit

                error != null -> {
                    UseCaseErrorHandler.globalOnErrorLogger(error)
                    callbacks.onError(error)
                }

                else -> callbacks.onComplete()
            }
        }
        // Handled in onCompletion, like Arkitekt's execute
        .catch { }
        .launchIn(coroutineScopeOwner.useCaseScope)
}

// The registry replays onStart to a late subscriber, so this starts with true when the lifecycle is already started
private fun Lifecycle.isStarted(): Flow<Boolean> = callbackFlow {
    val callbacks = object : Lifecycle.Callbacks {
        override fun onStart() {
            trySend(true)
        }

        override fun onStop() {
            trySend(false)
        }

        override fun onDestroy() {
            close()
        }
    }
    subscribe(callbacks)
    awaitClose { unsubscribe(callbacks) }
}
