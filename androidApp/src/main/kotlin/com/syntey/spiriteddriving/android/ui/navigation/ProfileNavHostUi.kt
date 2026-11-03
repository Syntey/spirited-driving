package com.syntey.spiriteddriving.android.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.syntey.spiriteddriving.feature.navigation.profile.ProfileChild
import com.syntey.spiriteddriving.feature.navigation.profile.ProfileConfig
import com.syntey.spiriteddriving.feature.navigation.profile.ProfileNavHost
import com.syntey.spiriteddriving.resources.*
import com.syntey.spiriteddriving.ui.screen.ProfileScreenUi
import com.syntey.spiriteddriving.ui.screen.ThirdScreenUi
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.router.stack.ChildStack
import dev.icerock.moko.resources.StringResource

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun ProfileNavHostUi(
    navHost: ProfileNavHost,
    modifier: Modifier = Modifier,
) {
    val stack: ChildStack<ProfileConfig, ProfileChild> by navHost.stack.collectAsStateWithLifecycle()
    val actions = navHost.actions

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
                    is ProfileChild.Profile -> ProfileScreenUi(screen = childInstance.screen, modifier = Modifier.fillMaxSize())
                    is ProfileChild.Third -> ThirdScreenUi(screen = childInstance.screen, modifier = Modifier.fillMaxSize())
                }
            }
        },
    )
}

private val ProfileChild.title: StringResource
    get() = when (this) {
        is ProfileChild.Profile -> MR.strings.profile_screen_title
        is ProfileChild.Third -> MR.strings.third_screen_title
    }
