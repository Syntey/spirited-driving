package com.syntey.spiriteddriving.feature.navigation.home

import com.syntey.spiriteddriving.feature.ui.firstScreen.FirstComponent
import com.syntey.spiriteddriving.feature.ui.firstScreen.FirstScreenNavigation
import com.syntey.spiriteddriving.feature.ui.picker.FruitPickerComponent
import com.syntey.spiriteddriving.feature.ui.picker.PickerArgs
import com.syntey.spiriteddriving.feature.ui.picker.PickerNavigation
import com.syntey.spiriteddriving.feature.ui.picker.PickerType
import com.syntey.spiriteddriving.feature.ui.picker.VegetablePickerComponent
import com.syntey.spiriteddriving.feature.ui.secondScreen.SecondComponent
import com.syntey.spiriteddriving.feature.ui.secondScreen.SecondScreenNavigation
import com.syntey.spiriteddriving.feature.ui.thirdScreen.ThirdComponent
import com.syntey.spiriteddriving.feature.ui.thirdScreen.ThirdScreenArgs
import com.syntey.spiriteddriving.feature.ui.thirdScreen.ThirdScreenNavigation
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

    override fun FirstComponent.navigateToSecond() =
        stackNavigator.pushNew(HomeConfig.Second)

    override fun SecondComponent.pop() =
        stackNavigator.pop()

    override fun SecondComponent.openPicker(type: PickerType, args: PickerArgs) =
        sheetNavigator.activate(HomeSheetConfig.Picker(type, args))

    override fun SecondComponent.navigateToThird(id: String) =
        stackNavigator.pushNew(HomeConfig.Third(ThirdScreenArgs(id)))

    override fun ThirdComponent.pop() {
        stackNavigator.pop()
    }

    override fun VegetablePickerComponent.dismiss() = sheetNavigator.dismiss()

    override fun FruitPickerComponent.dismiss() = sheetNavigator.dismiss()
}
