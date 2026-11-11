package com.syntey.spiriteddriving.feature.onboarding.onboardingScreen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.core.ui.components.GlassButton
import com.syntey.spiriteddriving.core.ui.components.Showcase
import com.syntey.spiriteddriving.core.ui.preview.ScreenPreviews
import com.syntey.spiriteddriving.core.ui.theme.AppTheme
import com.syntey.spiriteddriving.core.ui.theme.Grid
import com.syntey.spiriteddriving.core.ui.theme.UiPlatform
import com.syntey.spiriteddriving.feature.onboarding.art.OnboardingArt
import com.syntey.spiriteddriving.feature.onboarding.onboardingScreen.OnboardingScreen.Actions.Companion.noOpActions
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch

/**
 * First launch: the illustration fills the screen, and a glass panel at the bottom holds the pages. The screen draws behind
 * the system bars, and keeps Skip and the panel's content clear of them itself.
 */
@Composable
fun OnboardingScreenUi(screen: OnboardingScreen, modifier: Modifier = Modifier) {
    val viewState by screen.viewState.collectAsStateWithLifecycle()
    Content(viewState = viewState, actions = screen.actions, modifier = modifier)
}

@Composable
private fun Content(viewState: OnboardingViewState, actions: OnboardingScreen.Actions, modifier: Modifier = Modifier) {
    val reduceMotion = AppTheme.reduceMotion
    val position = remember { PagePosition(viewState.step) }
    val scope = rememberCoroutineScope()
    val hazeState = rememberHazeState()
    var width by remember { mutableIntStateOf(0) }
    var panelTop by remember { mutableFloatStateOf(Float.POSITIVE_INFINITY) }
    val skipBottom = skipBottom()

    LaunchedEffect(viewState.step, reduceMotion) {
        position.settle(viewState.step, afterFling = false, reduceMotion = reduceMotion)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { width = it.width }
            .draggable(
                state = rememberDraggableState { delta -> if (width > 0) position.dragBy(-delta / width) },
                orientation = Orientation.Horizontal,
                onDragStarted = { position.startDrag(lastSwipeable = viewState.lastSwipeable) },
                onDragStopped = { velocity ->
                    val end = position.endDrag(velocity = if (width > 0) -velocity / width else 0f)
                    actions.onSwiped(end.page)
                    if (end.swipedOn) actions.onSwipedOn()
                    scope.launch { position.settle(end.page, afterFling = true, reduceMotion = reduceMotion) }
                },
            ),
    ) {
        Illustration(
            position = position,
            step = viewState.step,
            reduceMotion = reduceMotion,
            hazeState = hazeState,
            controlsBottom = { skipBottom },
            panelTop = { panelTop },
            modifier = Modifier.fillMaxSize(),
        )
        SkipButton(
            position = position,
            onClick = actions::onSkip,
            hazeState = hazeState,
            modifier = Modifier.align(Alignment.TopEnd),
        )
        Panel(
            step = viewState.step,
            position = position,
            actions = actions,
            hazeState = hazeState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                .padding(top = Grid.d2)
                .onPlaced { panelTop = it.positionInParent().y },
        )
    }
}

// With Reduce Motion the illustration doesn't slide with the pages: it fades from one step to the next.
@Composable
private fun Illustration(
    position: PagePosition,
    step: OnboardingStep,
    reduceMotion: Boolean,
    hazeState: HazeState,
    controlsBottom: () -> Float,
    panelTop: () -> Float,
    modifier: Modifier = Modifier,
) {
    Box(modifier.hazeSource(hazeState)) {
        if (reduceMotion) {
            Crossfade(targetState = step, label = "illustration") { shown ->
                OnboardingArt(
                    position = { shown.ordinal.toFloat() },
                    controlsBottom = controlsBottom,
                    panelTop = panelTop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        } else {
            OnboardingArt(
                position = { position.value },
                controlsBottom = controlsBottom,
                panelTop = panelTop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

// Skip shows on the intro pages, and fades out on the way to Location.
@Composable
private fun SkipButton(position: PagePosition, onClick: () -> Unit, hazeState: HazeState, modifier: Modifier = Modifier) {
    if (position.introShown == 0f) return
    GlassButton(
        text = stringResource(MR.strings.onboarding_skip),
        onClick = onClick,
        hazeState = hazeState,
        modifier = modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.End))
            .padding(top = Grid.d2, end = Grid.d4)
            .graphicsLayer { alpha = position.introShown },
    )
}

// Skip's bottom edge, in pixels from the top. Worked out from its layout rather than measured, so the illustration stays
// put when Skip goes after the intro.
@Composable
private fun skipBottom(): Float {
    val density = LocalDensity.current
    val buttonBottom = with(density) { (Grid.d2 + AppTheme.platformStyle.glassButton.height).toPx() }
    return WindowInsets.safeDrawing.getTop(density) + buttonBottom
}

@ScreenPreviews
@Composable
private fun OnboardingScreenPreview() = Showcase {
    Content(viewState = OnboardingScreenPreviews.viewState(), actions = noOpActions())
}

@ScreenPreviews
@Composable
private fun OnboardingBudgetPreview() = Showcase {
    Content(viewState = OnboardingScreenPreviews.viewState(step = OnboardingStep.Budget), actions = noOpActions())
}

@ScreenPreviews
@Composable
private fun OnboardingHandOffPreview() = Showcase {
    Content(viewState = OnboardingScreenPreviews.viewState(step = OnboardingStep.HandOff), actions = noOpActions())
}

@ScreenPreviews
@Composable
private fun OnboardingLocationPreview() = Showcase {
    Content(viewState = OnboardingScreenPreviews.viewState(step = OnboardingStep.Location), actions = noOpActions())
}

@ScreenPreviews
@Composable
private fun OnboardingVignettePreview() = Showcase {
    Content(viewState = OnboardingScreenPreviews.viewState(step = OnboardingStep.Vignette), actions = noOpActions())
}

@ScreenPreviews
@Composable
private fun OnboardingIosPreview() = Showcase(platform = UiPlatform.Ios) {
    Content(viewState = OnboardingScreenPreviews.viewState(step = OnboardingStep.Budget), actions = noOpActions())
}
