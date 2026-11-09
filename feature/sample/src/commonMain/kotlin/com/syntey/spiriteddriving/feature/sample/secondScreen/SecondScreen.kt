package com.syntey.spiriteddriving.feature.sample.secondScreen

import kotlinx.coroutines.flow.StateFlow

interface SecondScreen {

    val viewState: StateFlow<SecondViewState>
    val actions: Actions

    interface Actions {
        fun onBack()
        fun onPickFruit()
        fun onPickVeggie()
    }
}
