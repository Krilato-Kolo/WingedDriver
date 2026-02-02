package com.krilatokolo.wingeddriver.driving

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.krilatokolo.wingeddriver.navigation.keys.DrivingScreenKey
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import kotlin.math.roundToInt

@InjectNavigationScreen
class DrivingScreen(
   private val viewModel: DrivingScreenViewModel,
) : Screen<DrivingScreenKey>() {
   @Composable
   override fun Content(key: DrivingScreenKey) {
      Column(
         Modifier
            .safeDrawingPadding()
            .fillMaxSize()
            // .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
         verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
         Spacer(Modifier.weight(1f))

         val state = viewModel.uiState.collectAsState()

         var value by remember { mutableStateOf(0) }
         Slider(
            value / 128.toFloat(),
            steps = 128,
            onValueChange = {
               val intValue = (it * 128).roundToInt()
               viewModel.setSpeed(intValue)
               value = intValue
            },
            modifier = Modifier.fillMaxWidth()
         )
      }
   }
}
