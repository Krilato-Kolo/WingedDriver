package com.krilatokolo.wingeddriver.locomotivepicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krilatokolo.wingeddriver.navigation.keys.base.LocomotivePickerScreenKey
import com.krilatokolo.wingeddriver.ui.debugging.PreviewTheme
import si.inova.kotlinova.navigation.instructions.goBack
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
class LocomotivePickerScreen(
   private val viewModel: LocomotivePickerViewModel,
   private val navigator: Navigator,
) : Screen<LocomotivePickerScreenKey>() {
   @Composable
   override fun Content(key: LocomotivePickerScreenKey) {
      LocomotivePickerScreenContent(
         viewModel.uiState.collectAsStateWithLifecycle().value,
         {
            viewModel.selectLoco(it)
            navigator.goBack()
         }
      )
   }
}

@Composable
private fun LocomotivePickerScreenContent(
   locos: List<Int>,
   selectLoco: (Int) -> Unit,
) {
   Column(
      Modifier
         .fillMaxSize()
         .safeDrawingPadding()
         .padding(16.dp)
   ) {
      val textState = rememberTextFieldState()
      val focusRequester = remember { FocusRequester() }

      TextField(
         textState,
         Modifier
            .focusRequester(focusRequester)
            .fillMaxWidth()
            .padding(bottom = 8.dp),
         keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
         onKeyboardAction = {
            textState.text.toString().toIntOrNull()?.let { locoNumber ->
               selectLoco(locoNumber)
            }
         },
      )

      LazyVerticalGrid(
         GridCells.Adaptive(75.dp),
         verticalArrangement = Arrangement.spacedBy(8.dp),
         horizontalArrangement = Arrangement.spacedBy(8.dp),
         modifier = Modifier.weight(1f)
      ) {
         items(locos) { loco ->
            Button(onClick = { selectLoco(loco) }, Modifier.padding(0.dp)) {
               Text(loco.toString())
            }
         }
      }

      LaunchedEffect(Unit) {
         focusRequester.requestFocus()
      }
   }
}

@Preview
@Composable
private fun LocomotivePickerScreenPreview() {
   PreviewTheme {
      LocomotivePickerScreenContent(List(50) { it + 100 }, {})
   }
}
