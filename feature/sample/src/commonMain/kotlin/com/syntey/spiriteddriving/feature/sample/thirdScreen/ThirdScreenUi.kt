package com.syntey.spiriteddriving.feature.sample.thirdScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.localized

@Composable
fun ThirdScreenUi(
    screen: ThirdScreen,
    modifier: Modifier = Modifier,
) {
    val viewState by screen.viewState.collectAsStateWithLifecycle()

    Content(viewState = viewState, modifier = modifier)
}

@Composable
private fun Content(
    viewState: ThirdViewState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = viewState.text.localized())
    }
}
