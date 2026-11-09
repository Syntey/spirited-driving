package com.syntey.spiriteddriving.shared.navigation.root

import app.futured.arkitekt.crusecases.execute
import app.futured.arkitekt.decompose.ext.asStateFlow
import com.syntey.spiriteddriving.shared.domain.IsUserLoggedInUseCase
import com.syntey.spiriteddriving.shared.navigation.signedIn.SignedInNavHostComponentFactory
import com.syntey.spiriteddriving.core.ui.base.AppComponent
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.feature.sample.SampleScreens
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.essenty.lifecycle.doOnCreate
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
internal class RootNavHostComponent(
    @InjectedParam componentContext: AppComponentContext,
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase,
) : AppComponent<RootNavHostViewState, Nothing>(componentContext, RootNavHostViewState),
    RootNavHost {

    private val rootNavigator: RootNavHostNavigation = RootNavHostNavigator()

    override val slot: StateFlow<ChildSlot<RootConfig, RootChild>> = childSlot(
        source = rootNavigator.slotNavigator,
        serializer = RootConfig.serializer(),
        initialConfiguration = { null },
        handleBackButton = false,
        childFactory = { config, childCtx ->
            when (config) {
                RootConfig.Login -> RootChild.Login(
                    SampleScreens.login(
                        childCtx,
                        rootNavigator,
                    ),
                )

                is RootConfig.SignedIn -> RootChild.SignedIn(
                    navHost = SignedInNavHostComponentFactory.createComponent(
                        componentContext = childCtx,
                        navigationToLogin = { rootNavigator.slotNavigator.activate(RootConfig.Login) },
                        initialConfig = config.initialConfig,
                    ),
                )
            }
        },
    ).asStateFlow()

    init {
        doOnCreate {
            checkUserLoggedIn()
        }
    }

    private fun checkUserLoggedIn() {
        isUserLoggedInUseCase.execute {
            onSuccess { isLoggedIn ->
                if (isLoggedIn) {
                    rootNavigator.slotNavigator.activate(RootConfig.SignedIn())
                } else {
                    rootNavigator.slotNavigator.activate(RootConfig.Login)
                }
            }
        }
    }
}
