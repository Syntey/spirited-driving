package com.syntey.spiriteddriving.shared.navigation.home

import app.futured.arkitekt.decompose.ext.asStateFlow
import com.syntey.spiriteddriving.core.ui.base.AppComponent
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.feature.sample.SampleScreens
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
internal class HomeNavHostComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam private val initialStack: List<HomeConfig>,
) : AppComponent<Unit, Nothing>(componentContext, Unit),
    HomeNavHost {

    private val homeNavigator: HomeNavHostNavigation = HomeNavigator()

    override val stack: StateFlow<ChildStack<HomeConfig, HomeChild>> = childStack(
        source = homeNavigator.stackNavigator,
        serializer = HomeConfig.serializer(),
        initialStack = { initialStack },
        key = "HomeStack",
        handleBackButton = true,
        childFactory = { config, childCtx ->
            when (config) {
                HomeConfig.First -> HomeChild.First(
                    SampleScreens.first(
                        componentContext = childCtx,
                        navigation = homeNavigator,
                    ),
                )

                HomeConfig.Second -> HomeChild.Second(
                    SampleScreens.second(
                        componentContext = childCtx,
                        navigation = homeNavigator,
                    ),
                )

                is HomeConfig.Third -> HomeChild.Third(
                    SampleScreens.third(
                        componentContext = childCtx,
                        navigation = homeNavigator,
                        args = config.args,
                    ),
                )
            }
        },
    ).asStateFlow()

    override val sheet: StateFlow<ChildSlot<HomeSheetConfig, HomeSheetChild>> = childSlot(
        source = homeNavigator.sheetNavigator,
        serializer = HomeSheetConfig.serializer(),
        initialConfiguration = { null },
        key = "HomeSheetSlot",
        handleBackButton = true,
        childFactory = { config, childCtx ->
            when (config) {
                is HomeSheetConfig.Picker -> HomeSheetChild.Picker(
                    SampleScreens.picker(
                        componentContext = childCtx,
                        navigation = homeNavigator,
                        type = config.pickerType,
                        args = config.args,
                    ),
                )
            }
        },
    ).asStateFlow()

    override val actions: HomeNavHost.Actions = object : HomeNavHost.Actions {
        override fun navigate(newStack: List<Child<HomeConfig, HomeChild>>) =
            homeNavigator.stackNavigator.navigate { newStack.map { it.configuration } }
        override fun pop() = homeNavigator.stackNavigator.pop()
        override fun onSheetDismissed() = homeNavigator.sheetNavigator.dismiss()
    }
}
