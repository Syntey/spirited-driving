package com.syntey.spiriteddriving.core.ui.base

import app.futured.arkitekt.crusecases.CoroutineScopeOwner
import app.futured.arkitekt.crusecases.FlowUseCase
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.start
import com.arkivanov.essenty.lifecycle.stop
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ExecuteWithLifecycleTest {

    @Test
    fun `flow is not collected before the lifecycle starts`() = runTest {
        val owner = owner()
        val useCase = FakeUpdatesUseCase()
        with(owner) { useCase.executeWithLifecycle { } }
        owner.lifecycle.create()
        runCurrent()

        assertEquals(0, useCase.updates.subscriptionCount.value)
    }

    @Test
    fun `values emitted while started reach onNext`() = runTest {
        val owner = owner()
        val useCase = FakeUpdatesUseCase()
        val received = mutableListOf<Int>()
        with(owner) { useCase.executeWithLifecycle { onNext(received::add) } }
        owner.lifecycle.start()
        runCurrent()

        useCase.updates.emit(1)
        runCurrent()

        assertEquals(listOf(1), received)
    }

    @Test
    fun `flow stops being collected when the lifecycle stops`() = runTest {
        val owner = owner()
        val useCase = FakeUpdatesUseCase()
        with(owner) { useCase.executeWithLifecycle { } }
        owner.lifecycle.start()
        runCurrent()

        owner.lifecycle.stop()
        runCurrent()

        assertEquals(0, useCase.updates.subscriptionCount.value)
    }

    @Test
    fun `flow is collected again on the next start`() = runTest {
        val owner = owner()
        val useCase = FakeUpdatesUseCase()
        with(owner) { useCase.executeWithLifecycle { } }
        owner.lifecycle.start()
        owner.lifecycle.stop()
        runCurrent()

        owner.lifecycle.start()
        runCurrent()

        assertEquals(1, useCase.updates.subscriptionCount.value)
    }

    @Test
    fun `cancelling the job in the pool stops the flow for good`() = runTest {
        val owner = owner()
        val useCase = FakeUpdatesUseCase()
        with(owner) { useCase.executeWithLifecycle { } }
        owner.lifecycle.start()
        runCurrent()

        owner.useCaseJobPool[useCase]?.cancel()
        owner.lifecycle.stop()
        owner.lifecycle.start()
        runCurrent()

        assertEquals(0, useCase.updates.subscriptionCount.value)
    }
}

private fun TestScope.owner() = TestOwner(useCaseScope = backgroundScope, workerDispatcher = StandardTestDispatcher(testScheduler))

private class TestOwner(override val useCaseScope: CoroutineScope, private val workerDispatcher: CoroutineDispatcher) :
    CoroutineScopeOwner,
    LifecycleOwner {

    override val lifecycle: LifecycleRegistry = LifecycleRegistry()
    override val useCaseJobPool: MutableMap<Any, Job> = mutableMapOf()

    override fun getWorkerDispatcher(): CoroutineDispatcher = workerDispatcher
}

private class FakeUpdatesUseCase : FlowUseCase<Unit, Int> {
    val updates = MutableSharedFlow<Int>()

    override fun build(args: Unit): Flow<Int> = updates
}
