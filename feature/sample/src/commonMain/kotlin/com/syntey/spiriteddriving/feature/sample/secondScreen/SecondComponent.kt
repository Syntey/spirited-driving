package com.syntey.spiriteddriving.feature.sample.secondScreen

import app.futured.arkitekt.annotation.GenerateFactory
import app.futured.arkitekt.decompose.navigation.resultFlow
import com.syntey.spiriteddriving.feature.sample.NavigationResultKeys
import com.syntey.spiriteddriving.core.ui.base.AppComponentContext
import com.syntey.spiriteddriving.core.ui.base.ScreenComponent
import com.syntey.spiriteddriving.feature.sample.picker.PickerArgs
import com.syntey.spiriteddriving.feature.sample.picker.PickerType
import com.arkivanov.essenty.lifecycle.doOnCreate
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@GenerateFactory
@Factory
internal class SecondComponent(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam override val navigation: SecondScreenNavigation,
) : ScreenComponent<SecondViewState, Nothing, SecondScreenNavigation>(
    componentContext = componentContext,
    defaultState = SecondViewState,
),
    SecondScreen,
    SecondScreenNavigation by navigation,
    SecondScreen.Actions {

    override val viewState: StateFlow<SecondViewState> = componentState
    override val actions: SecondScreen.Actions = this
    private val pickerResults = resultFlow(NavigationResultKeys.Picker)

    override fun onBack() = pop()

    override fun onPickVeggie() = openPicker(PickerType.Vegetable, PickerArgs(NavigationResultKeys.Picker))

    override fun onPickFruit() = openPicker(PickerType.Fruit, PickerArgs(NavigationResultKeys.Picker))

    init {
        lifecycle.doOnCreate {
            collectPickerResults()
        }
    }

    private fun collectPickerResults() {
        pickerResults
            .onEach { selection -> navigateToThird(selection) }
            .launchIn(lifecycleScope)
    }
}
