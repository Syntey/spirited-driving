package com.syntey.spiriteddriving.feature.sample.picker

import app.futured.arkitekt.decompose.navigation.NavigationActions

interface PickerNavigation : NavigationActions {
    fun PickerScreen.dismiss()
}
