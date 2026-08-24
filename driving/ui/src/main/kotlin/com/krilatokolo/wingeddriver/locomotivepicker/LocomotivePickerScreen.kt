package com.krilatokolo.wingeddriver.locomotivepicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.krilatokolo.wingeddriver.navigation.keys.base.LocomotivePickerScreenKey
import com.krilatokolo.wingeddriver.savedlocos.model.SavedLoco
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
         viewModel::setFilter,
         {
            viewModel.selectLoco(it)
            navigator.goBack()
         },
         {
            viewModel.selectLoco(it)
            navigator.goBack()
         }
      )
   }
}

@Composable
private fun LocomotivePickerScreenContent(
   locos: List<SavedLoco>,
   setFilter: (String) -> Unit,
   selectLoco: (String) -> Unit,
   selectLocoGw: (SavedLoco) -> Unit,
) {
   Column(
      Modifier
         .fillMaxSize()
         .safeDrawingPadding()
         .padding(16.dp)
   ) {
      val textState = rememberTextFieldState()
      val focusRequester = remember { FocusRequester() }

      LaunchedEffect(textState.text) {
         setFilter(textState.text.toString())
      }

      TextField(
         textState,
         Modifier
            .focusRequester(focusRequester)
            .fillMaxWidth()
            .padding(bottom = 8.dp),
         keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
         onKeyboardAction = {
            selectLoco(textState.text.toString())
         },
      )

      LazyVerticalGrid(
         GridCells.Adaptive(170.dp),
         verticalArrangement = Arrangement.spacedBy(8.dp),
         horizontalArrangement = Arrangement.spacedBy(8.dp),
         modifier = Modifier.weight(1f)
      ) {
         items(locos) { loco ->
            Button(
               onClick = { selectLocoGw(loco) },
               modifier = Modifier.padding(0.dp),
               shape = RectangleShape,
               contentPadding = PaddingValues(1.dp)
            ) {
               Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  if (loco.imageUrl != null) {
                     AsyncImage(
                        loco.imageUrl, contentDescription = null,
                        modifier = Modifier
                           .padding(bottom = 4.dp)
                           .fillMaxWidth()
                           .aspectRatio(IMAGE_ASPECT_RATIO)
                     )
                  } else {
                     Spacer(
                        Modifier
                           .padding(bottom = 4.dp)
                           .fillMaxWidth()
                           .aspectRatio(IMAGE_ASPECT_RATIO)
                     )
                  }

                  Text(loco.name)
               }
            }
         }
      }

      LaunchedEffect(Unit) {
         focusRequester.requestFocus()
      }
   }
}

private const val IMAGE_ASPECT_RATIO = 5f / 2f

@Preview
@Composable
private fun LocomotivePickerScreenPreview() {
   PreviewTheme {
      LocomotivePickerScreenContent(
         List(50) {
            SavedLoco(it + 100, name = (it + 100).toString(), imageUrl = it.takeIf { it % 5 != 0 }?.toString())
         },
         {},
         {},
         {},
      )
   }
}
