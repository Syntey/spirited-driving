package com.syntey.spiriteddriving.core.ui.base

import app.futured.arkitekt.decompose.ArkitektComponentContext
import app.futured.arkitekt.decompose.navigation.NavigationResultRegistry
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ComponentContextFactory
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.arkivanov.essenty.instancekeeper.InstanceKeeperOwner
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.statekeeper.StateKeeperOwner
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Application-specific [ComponentContext] that can be decorated with custom functionality.
 * See [docs](https://arkivanov.github.io/Decompose/component/custom-component-context/) for more info.
 */
interface AppComponentContext : ArkitektComponentContext<AppComponentContext> {

    /**
     * The dispatcher that components run their use cases on. Null keeps Arkitekt's default. Only tests set it, so their
     * use cases run under the test's virtual time.
     */
    val useCaseDispatcher: CoroutineDispatcher?
}

/**
 * Default implementation of [AppComponentContext].
 *
 * @param componentContext [ComponentContext] to wrap.
 * @param useCaseDispatcher The dispatcher for use cases, passed on to the child contexts; null keeps Arkitekt's default.
 */
class DefaultAppComponentContext(
    componentContext: ComponentContext,
    override val navigationResultRegistry: NavigationResultRegistry,
    override val useCaseDispatcher: CoroutineDispatcher?,
) : AppComponentContext,
    LifecycleOwner by componentContext,
    StateKeeperOwner by componentContext,
    InstanceKeeperOwner by componentContext,
    BackHandlerOwner by componentContext {

    constructor(
        componentContext: ComponentContext,
    ) : this(
        componentContext = componentContext,
        navigationResultRegistry = NavigationResultRegistry(componentContext.stateKeeper),
        useCaseDispatcher = null,
    )

    constructor(
        componentContext: ComponentContext,
        useCaseDispatcher: CoroutineDispatcher?,
    ) : this(
        componentContext = componentContext,
        navigationResultRegistry = NavigationResultRegistry(componentContext.stateKeeper),
        useCaseDispatcher = useCaseDispatcher,
    )

    override val componentContextFactory: ComponentContextFactory<AppComponentContext> =
        ComponentContextFactory { lifecycle, stateKeeper, instanceKeeper, backHandler ->
            val ctx = componentContext.componentContextFactory(lifecycle, stateKeeper, instanceKeeper, backHandler)
            DefaultAppComponentContext(ctx, navigationResultRegistry, useCaseDispatcher)
        }
}
