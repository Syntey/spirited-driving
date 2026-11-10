package com.syntey.spiriteddriving.feature.sample.firstScreen

import app.futured.arkitekt.decompose.presentation.UiEvent
import dev.icerock.moko.resources.desc.StringDesc

sealed interface FirstUiEvent : UiEvent {
    data class ShowToast(val text: StringDesc) : FirstUiEvent
}
