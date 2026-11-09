package com.syntey.spiriteddriving.shared.navigation.home

import com.syntey.spiriteddriving.feature.sample.firstScreen.FirstScreen
import com.syntey.spiriteddriving.feature.sample.firstScreen.FirstScreenNavigation
import com.syntey.spiriteddriving.feature.sample.picker.PickerArgs
import com.syntey.spiriteddriving.feature.sample.picker.PickerNavigation
import com.syntey.spiriteddriving.feature.sample.picker.PickerScreen
import com.syntey.spiriteddriving.feature.sample.picker.PickerType
import com.syntey.spiriteddriving.feature.sample.secondScreen.SecondScreen
import com.syntey.spiriteddriving.feature.sample.secondScreen.SecondScreenNavigation
import com.syntey.spiriteddriving.feature.sample.thirdScreen.ThirdScreen
import com.syntey.spiriteddriving.feature.sample.thirdScreen.ThirdScreenArgs
import com.syntey.spiriteddriving.feature.sample.thirdScreen.ThirdScreenNavigation
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew

internal interface HomeNavHostNavigation :
    FirstScreenNavigation,
    SecondScreenNavigation,
    ThirdScreenNavigation,
    PickerNavigation {

    val stackNavigator: StackNavigation<HomeConfig>
    val sheetNavigator: SlotNavigation<HomeSheetConfig>
}

internal class HomeNavigator : HomeNavHostNavigation {
    override val stackNavigator = StackNavigation<HomeConfig>()
    override val sheetNavigator = SlotNavigation<HomeSheetConfig>()

    override fun FirstScreen.navigateToSecond() =
        stackNavigator.pushNew(HomeConfig.Second)

    override fun SecondScreen.pop() =
        stackNavigator.pop()

    override fun SecondScreen.openPicker(type: PickerType, args: PickerArgs) =
        sheetNavigator.activate(HomeSheetConfig.Picker(type, args))

    override fun SecondScreen.navigateToThird(id: String) =
        stackNavigator.pushNew(HomeConfig.Third(ThirdScreenArgs(id)))

    override fun ThirdScreen.pop() {
        stackNavigator.pop()
    }

    override fun PickerScreen.dismiss() = sheetNavigator.dismiss()
}
