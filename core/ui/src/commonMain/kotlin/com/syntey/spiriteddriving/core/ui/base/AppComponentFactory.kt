package com.syntey.spiriteddriving.core.ui.base

import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

/**
 * Factory object for creating application components using Koin. Navigation hosts are created through it.
 *
 * Feature screens annotated with `@GenerateFactory` get a generated factory instead. [parameters] must follow the order of
 * the component's `@InjectedParam` constructor parameters after the context; a mismatch fails at runtime, when Koin
 * resolves the component.
 */
object AppComponentFactory : KoinComponent {

    /**
     * Injects an [AppComponent] instance with the specified context and additional injection parameters.
     *
     * @param C The type of the application component.
     * @param childContext The context of the component.
     * @param parameters Additional parameters for the component.
     * @return The created application component.
     */
    inline fun <reified C : AppComponent<*, *>> createAppComponent(
        childContext: AppComponentContext,
        vararg parameters: Any?,
    ): C = get(
        qualifier = null,
        parameters = {
            parametersOf(childContext, *parameters)
        },
    )
}
