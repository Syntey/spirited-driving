package com.syntey.spiriteddriving.feature.ui.secondScreen

import app.futured.arkitekt.decompose.navigation.NavigationActions
import com.syntey.spiriteddriving.feature.ui.picker.PickerArgs
import com.syntey.spiriteddriving.feature.ui.picker.PickerType

internal interface SecondScreenNavigation : NavigationActions {
    fun SecondComponent.pop()
    fun SecondComponent.openPicker(type: PickerType, args: PickerArgs)
    fun SecondComponent.navigateToThird(id: String)
}
