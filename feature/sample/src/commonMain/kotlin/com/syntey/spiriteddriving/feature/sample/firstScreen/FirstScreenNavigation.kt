package com.syntey.spiriteddriving.feature.sample.firstScreen

import app.futured.arkitekt.decompose.navigation.NavigationActions

interface FirstScreenNavigation : NavigationActions {
    fun FirstScreen.navigateToSecond()
}
