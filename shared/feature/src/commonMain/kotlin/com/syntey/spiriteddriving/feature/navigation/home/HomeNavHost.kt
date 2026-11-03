package com.syntey.spiriteddriving.feature.navigation.home

import com.syntey.spiriteddriving.feature.ui.firstScreen.FirstScreen
import com.syntey.spiriteddriving.feature.ui.picker.PickerArgs
import com.syntey.spiriteddriving.feature.ui.picker.PickerScreen
import com.syntey.spiriteddriving.feature.ui.picker.PickerType
import com.syntey.spiriteddriving.feature.ui.secondScreen.SecondScreen
import com.syntey.spiriteddriving.feature.ui.thirdScreen.ThirdScreen
import com.syntey.spiriteddriving.feature.ui.thirdScreen.ThirdScreenArgs
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

interface HomeNavHost : BackHandlerOwner {

    val stack: StateFlow<ChildStack<HomeConfig, HomeChild>>
    val sheet: StateFlow<ChildSlot<HomeSheetConfig, HomeSheetChild>>
    val actions: Actions

    interface Actions {
        fun navigate(newStack: List<Child<HomeConfig, HomeChild>>)
        fun pop()
        fun onSheetDismissed()
    }
}

@Serializable
sealed interface HomeConfig {

    @Serializable
    data object First : HomeConfig

    @Serializable
    data object Second : HomeConfig

    @Serializable
    data class Third(val args: ThirdScreenArgs) : HomeConfig
}

@Serializable
sealed interface HomeSheetConfig {

    @Serializable
    data class Picker(val pickerType: PickerType, val args: PickerArgs) : HomeSheetConfig
}

sealed interface HomeChild {

    data class First(val screen: FirstScreen) : HomeChild
    data class Second(val screen: SecondScreen) : HomeChild
    data class Third(val screen: ThirdScreen) : HomeChild
}

sealed interface HomeSheetChild {

    data class Picker(val screen: PickerScreen) : HomeSheetChild
}
