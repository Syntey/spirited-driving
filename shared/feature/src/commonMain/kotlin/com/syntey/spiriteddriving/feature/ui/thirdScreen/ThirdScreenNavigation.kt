package com.syntey.spiriteddriving.feature.ui.thirdScreen

import app.futured.arkitekt.decompose.navigation.NavigationActions

internal interface ThirdScreenNavigation : NavigationActions {
    fun ThirdComponent.pop()
}
