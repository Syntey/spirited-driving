package com.syntey.spiriteddriving.core.ui.testing

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.start
import com.arkivanov.essenty.lifecycle.stop
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.DefaultAppComponentContext
import kotlinx.coroutines.CoroutineDispatcher

/**
 * A started [AppComponentContext] for component tests, with a lifecycle the test stops, starts and destroys, and a back
 * dispatcher the test presses Back on. Use cases of the components run on [useCaseDispatcher]. Only tests use it; production
 * code builds its contexts from the navigation.
 *
 * Starts created and resumed, like a screen on screen. Use [stop] and [start] to check what happens when the screen
 * leaves and comes back.
 */
class TestComponentContext(useCaseDispatcher: CoroutineDispatcher) {

    private val lifecycle = LifecycleRegistry()
    private val backDispatcher = BackDispatcher()

    val context: AppComponentContext = DefaultAppComponentContext(
        componentContext = DefaultComponentContext(lifecycle = lifecycle, backHandler = backDispatcher),
        useCaseDispatcher = useCaseDispatcher,
    )

    init {
        lifecycle.create()
        lifecycle.resume()
    }

    /** Stops the lifecycle, as when the screen leaves the foreground: components stop collecting flows that need it. */
    fun stop() = lifecycle.stop()

    /** Starts the lifecycle again after [stop], as when the screen comes back. */
    fun start() = lifecycle.start()

    /** Resumes the lifecycle after [stop] or [start]. */
    fun resume() = lifecycle.resume()

    /** Destroys the lifecycle, as when the screen is gone for good: the component's scope is cancelled. */
    fun destroy() = lifecycle.destroy()

    /** Presses Back. Returns whether a callback handled it. */
    fun back(): Boolean = backDispatcher.back()
}
