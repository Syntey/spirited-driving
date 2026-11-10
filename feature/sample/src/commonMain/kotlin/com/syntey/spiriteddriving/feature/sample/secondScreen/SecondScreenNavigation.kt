package com.syntey.spiriteddriving.feature.sample.secondScreen

import app.futured.arkitekt.decompose.navigation.NavigationActions
import com.syntey.spiriteddriving.feature.sample.picker.PickerArgs
import com.syntey.spiriteddriving.feature.sample.picker.PickerType

interface SecondScreenNavigation : NavigationActions {
    fun SecondScreen.pop()
    fun SecondScreen.openPicker(type: PickerType, args: PickerArgs)
    fun SecondScreen.navigateToThird(id: String)
}
