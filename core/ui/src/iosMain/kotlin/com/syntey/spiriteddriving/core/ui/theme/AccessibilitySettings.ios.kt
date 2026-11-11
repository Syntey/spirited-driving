package com.syntey.spiriteddriving.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.chrisbanes.haze.glass.GlassAccessibilitySettings
import dev.chrisbanes.haze.glass.LocalGlassAccessibilitySettings
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIAccessibilityDarkerSystemColorsEnabled
import platform.UIKit.UIAccessibilityDarkerSystemColorsStatusDidChangeNotification
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled
import platform.UIKit.UIAccessibilityIsReduceTransparencyEnabled
import platform.UIKit.UIAccessibilityReduceMotionStatusDidChangeNotification
import platform.UIKit.UIAccessibilityReduceTransparencyStatusDidChangeNotification

@Composable
internal actual fun ProvideAccessibilitySettings(content: @Composable () -> Unit) {
    var settings by remember { mutableStateOf(IosAccessibilitySettings.read()) }

    DisposableEffect(Unit) {
        val center = NSNotificationCenter.defaultCenter
        val observers = listOf(
            UIAccessibilityReduceMotionStatusDidChangeNotification,
            UIAccessibilityReduceTransparencyStatusDidChangeNotification,
            UIAccessibilityDarkerSystemColorsStatusDidChangeNotification,
        ).map { name ->
            center.addObserverForName(name, null, NSOperationQueue.mainQueue) { settings = IosAccessibilitySettings.read() }
        }
        onDispose { observers.forEach(center::removeObserver) }
    }

    val surface = MaterialTheme.colorScheme.surface
    val glassSettings = remember(settings, surface) {
        GlassAccessibilitySettings(
            reduceTransparency = settings.reduceTransparency,
            increaseContrast = settings.increaseContrast,
            reducedTransparencyFallbackColor = surface,
        )
    }
    CompositionLocalProvider(
        LocalReduceMotion provides settings.reduceMotion,
        LocalGlassAccessibilitySettings provides glassSettings,
        content = content,
    )
}

private data class IosAccessibilitySettings(val reduceMotion: Boolean, val reduceTransparency: Boolean, val increaseContrast: Boolean) {
    companion object {
        fun read() = IosAccessibilitySettings(
            reduceMotion = UIAccessibilityIsReduceMotionEnabled(),
            reduceTransparency = UIAccessibilityIsReduceTransparencyEnabled(),
            increaseContrast = UIAccessibilityDarkerSystemColorsEnabled(),
        )
    }
}
