package com.syntey.spiriteddriving.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syntey.spiriteddriving.feature.ui.secondScreen.SecondScreen
import com.syntey.spiriteddriving.resources.*
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun SecondScreenUi(
    screen: SecondScreen,
    modifier: Modifier = Modifier,
) {
    Content(actions = screen.actions, modifier = modifier)
}

@Composable
private fun Content(
    actions: SecondScreen.Actions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.padding(horizontal = 20.dp)) {
            Button(onClick = { actions.onPickFruit() }, modifier = Modifier.weight(1f)) {
                Text(text = stringResource(MR.strings.second_screen_button_fruit))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { actions.onPickVeggie() }, modifier = Modifier.weight(1f)) {
                Text(text = stringResource(MR.strings.second_screen_button_veggie))
            }
        }
    }
}
