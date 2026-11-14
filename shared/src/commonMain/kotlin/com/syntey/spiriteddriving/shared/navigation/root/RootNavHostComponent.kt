package com.syntey.spiriteddriving.shared.navigation.root

import app.futured.arkitekt.crusecases.execute
import app.futured.arkitekt.decompose.ext.asStateFlow
import co.touchlab.kermit.Logger
import com.syntey.spiriteddriving.shared.domain.IsOnboardingDoneUseCase
import com.syntey.spiriteddriving.core.ui.base.AppComponent
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.feature.map.ui.MapScreens
import com.syntey.spiriteddriving.feature.onboarding.ui.OnboardingScreens
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
    private val isOnboardingDoneUseCase: IsOnboardingDoneUseCase,
) : AppComponent<RootNavHostViewState, Nothing>(componentContext, RootNavHostViewState),
    RootNavHost {

    private val logger = Logger.withTag("RootNavHostComponent")
    private val rootNavigator: RootNavHostNavigation = RootNavHostNavigator()

    override val slot: StateFlow<ChildSlot<RootConfig, RootChild>> = childSlot(
        source = rootNavigator.slotNavigator,
        serializer = RootConfig.serializer(),
        initialConfiguration = { null },
        handleBackButton = false,
        childFactory = { config, childCtx ->
            when (config) {
                RootConfig.Onboarding -> RootChild.Onboarding(OnboardingScreens.onboarding(childCtx, rootNavigator))
                RootConfig.Map -> RootChild.Map(MapScreens.tripPlan(childCtx, rootNavigator))
            }
        },
    ).asStateFlow()

    init {
        doOnCreate {
            checkOnboardingDone()
        }
    }

    private fun checkOnboardingDone() {
        isOnboardingDoneUseCase.execute {
            onSuccess { isDone ->
                if (isDone) {
                    rootNavigator.slotNavigator.activate(RootConfig.Map)
                } else {
                    rootNavigator.slotNavigator.activate(RootConfig.Onboarding)
                }
            }
            onError { error ->
                logger.e(error) { "First launch state not read" }
                rootNavigator.slotNavigator.activate(RootConfig.Map)
            }
        }
    }
}
