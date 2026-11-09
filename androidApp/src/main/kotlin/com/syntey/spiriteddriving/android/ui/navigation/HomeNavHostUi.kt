package com.syntey.spiriteddriving.android.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.shared.navigation.home.HomeChild
import com.syntey.spiriteddriving.shared.navigation.home.HomeConfig
import com.syntey.spiriteddriving.shared.navigation.home.HomeNavHost
import com.syntey.spiriteddriving.shared.navigation.home.HomeSheetChild
import com.syntey.spiriteddriving.shared.navigation.home.HomeSheetConfig
import com.syntey.spiriteddriving.feature.sample.picker.PickerScreen
import com.syntey.spiriteddriving.core.resources.*
import com.syntey.spiriteddriving.feature.sample.firstScreen.FirstScreenUi
import com.syntey.spiriteddriving.feature.sample.picker.PickerScreenUi
import com.syntey.spiriteddriving.feature.sample.secondScreen.SecondScreenUi
import com.syntey.spiriteddriving.feature.sample.thirdScreen.ThirdScreenUi
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.stack.ChildStack
import dev.icerock.moko.resources.StringResource

@OptIn(ExperimentalDecomposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeNavHostUi(
    navHost: HomeNavHost,
    modifier: Modifier = Modifier,
) {
    val stack: ChildStack<HomeConfig, HomeChild> by navHost.stack.collectAsStateWithLifecycle()
    val sheet: ChildSlot<HomeSheetConfig, HomeSheetChild> by navHost.sheet.collectAsStateWithLifecycle()
    val actions = navHost.actions
    val bottomSheetState = rememberModalBottomSheetState()

    Scaffold(
        modifier = modifier,
        topBar = {
            NavHostTopBar(
                title = kmpStringResource(stack.active.instance.title),
                canNavigateBack = stack.backStack.isNotEmpty(),
                onNavigateBack = actions::pop,
            )
        },
        content = { paddings ->
            Children(
                stack = stack,
                modifier = Modifier
                    .padding(paddings)
                    .consumeWindowInsets(paddings),
                animation = predictiveBackAnimation(
                    backHandler = navHost.backHandler,
                    onBack = actions::pop,
                ),
            ) { child ->
                when (val childInstance = child.instance) {
                    is HomeChild.First -> FirstScreenUi(screen = childInstance.screen, modifier = Modifier.fillMaxSize())
                    is HomeChild.Second -> SecondScreenUi(screen = childInstance.screen, modifier = Modifier.fillMaxSize())
                    is HomeChild.Third -> ThirdScreenUi(screen = childInstance.screen, modifier = Modifier.fillMaxSize())
                }
            }
        },
    )

    val sheetChildInstance = sheet.child?.instance
    when (sheetChildInstance) {
        is HomeSheetChild.Picker -> PickerSheet(
            screen = sheetChildInstance.screen,
            bottomSheetState = bottomSheetState,
            onDismissRequest = actions::onSheetDismissed,
        )

        null -> Unit
    }
}

private val HomeChild.title: StringResource
    get() = when (this) {
        is HomeChild.First -> MR.strings.first_screen_title
        is HomeChild.Second -> MR.strings.second_screen_title
        is HomeChild.Third -> MR.strings.third_screen_title
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickerSheet(
    screen: PickerScreen,
    bottomSheetState: SheetState,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        sheetState = bottomSheetState,
        onDismissRequest = onDismissRequest,
        contentWindowInsets = { WindowInsets(0) },
        modifier = modifier,
    ) {
        PickerScreenUi(screen)
    }
}
