package com.syntey.spiriteddriving.android.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.feature.map.ui.tripPlanScreen.MapScreenUi
import com.syntey.spiriteddriving.feature.onboarding.ui.onboardingScreen.OnboardingScreenUi
import com.syntey.spiriteddriving.shared.navigation.root.RootChild
import com.syntey.spiriteddriving.shared.navigation.root.RootConfig
import com.syntey.spiriteddriving.shared.navigation.root.RootNavHost
import com.syntey.spiriteddriving.shared.navigation.root.RootSheetChild
import com.syntey.spiriteddriving.shared.navigation.root.RootSheetConfig
import com.arkivanov.decompose.router.slot.ChildSlot

@Composable
fun RootNavHostUi(
    navHost: RootNavHost,
    modifier: Modifier = Modifier,
) {
    val slot: ChildSlot<RootConfig, RootChild> by navHost.slot.collectAsStateWithLifecycle()
    val sheet: ChildSlot<RootSheetConfig, RootSheetChild> by navHost.sheet.collectAsStateWithLifecycle()

    Box(modifier.background(MaterialTheme.colorScheme.background)) {
        when (val childInstance = slot.child?.instance) {
            is RootChild.Onboarding -> OnboardingScreenUi(screen = childInstance.screen, modifier = Modifier.fillMaxSize())
            is RootChild.Map -> MapScreenUi(screen = childInstance.screen, modifier = Modifier.fillMaxSize())
            null -> ApplicationLoading(Modifier.fillMaxSize())
        }
        // Slides in over the map like a pushed screen, and keeps the last child on screen while it slides out
        AnimatedContent(
            targetState = sheet.child?.instance,
            transitionSpec = {
                if (targetState != null) {
                    slideInHorizontally { it } togetherWith ExitTransition.None
                } else {
                    EnterTransition.None togetherWith slideOutHorizontally { it }
                }
            },
            contentKey = { it?.let { instance -> instance::class } },
            label = "RootSheet",
        ) { sheetInstance ->
            when (sheetInstance) {
                is RootSheetChild.Profile -> ProfileNavHostUi(navHost = sheetInstance.navHost, modifier = Modifier.fillMaxSize())
                null -> Box(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun ApplicationLoading(
    modifier: Modifier = Modifier,
) = Box(modifier.background(MaterialTheme.colorScheme.background)) {
    CircularProgressIndicator(
        Modifier
            .size(Grid.d12)
            .align(Alignment.Center),
    )
}
