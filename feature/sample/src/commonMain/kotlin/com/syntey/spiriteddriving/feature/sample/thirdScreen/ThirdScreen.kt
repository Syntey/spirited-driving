package com.syntey.spiriteddriving.feature.sample.thirdScreen

import kotlinx.coroutines.flow.StateFlow

interface ThirdScreen {

    val viewState: StateFlow<ThirdViewState>
    val actions: Actions

    interface Actions {
        fun onBack()
    }
}
