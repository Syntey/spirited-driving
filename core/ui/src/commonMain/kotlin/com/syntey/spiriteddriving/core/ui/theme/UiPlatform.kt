package com.syntey.spiriteddriving.core.ui.theme

/**
 * The platform whose look the shared UI takes: its type sizes and component styles ([PlatformStyle]). It's the platform the
 * app runs on, except in previews, which can show either one.
 */
enum class UiPlatform {
    Android,
    Ios,
    ;

    companion object {
        val Current: UiPlatform get() = currentUiPlatform
    }
}

internal expect val currentUiPlatform: UiPlatform
