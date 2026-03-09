package com.krilatokolo.wingeddriver.tools.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krilatokolo.wingeddriver.navigation.keys.ToolsScreenKey
import com.krilatokolo.wingeddriver.tools.invertDirectionPreference
import com.krilatokolo.wingeddriver.ui.components.ProgressErrorSuccessScaffold
import com.krilatokolo.wingeddriver.ui.debugging.FullScreenPreviews
import com.krilatokolo.wingeddriver.ui.debugging.PreviewTheme
import me.zhanghai.compose.preference.LocalPreferenceTheme
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.preferenceTheme
import si.inova.kotlinova.navigation.di.ContributesScreenBinding
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
@ContributesScreenBinding
class ToolsScreen(
   private val navigator: Navigator,
   private val viewModel: ToolsViewModel,
) : Screen<ToolsScreenKey>() {
   @Composable
   override fun Content(key: ToolsScreenKey) {
      val stateOutcome = viewModel.appVersion.collectAsStateWithLifecycle()

      ProgressErrorSuccessScaffold(
         stateOutcome::value,
         Modifier
            .fillMaxSize()
            .safeDrawingPadding()
      ) { state ->
         ToolsScreenContent(
            state = state,
            updatePreference = { prefKey, value ->
               @Suppress("UNCHECKED_CAST")
               viewModel.updatePreference(prefKey as Preferences.Key<Any?>, value)
            }
         )
      }
   }
}

@Composable
private fun ToolsScreenContent(
   state: ToolsState,
   updatePreference: (Preferences.Key<*>, Any?) -> Unit,
) {
   CompositionLocalProvider(
      LocalPreferenceTheme provides preferenceTheme(
         verticalSpacing = 0.dp,
         padding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
      )
   ) {
      LazyVerticalGrid(
         GridCells.Adaptive(160.dp),
         horizontalArrangement = Arrangement.spacedBy(16.dp),
         verticalArrangement = Arrangement.spacedBy(16.dp),
         contentPadding = WindowInsets.safeDrawing.asPaddingValues(),
         modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
      ) {
         item(span = { GridItemSpan(maxLineSpan) }) {
            SwitchPreference(
               state.preferences[invertDirectionPreference] == true,
               onValueChange = {
                  updatePreference(invertDirectionPreference, it)
               },
               title = { Text(stringResource(R.string.invert_direction)) },
            )
         }

         item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
               stringResource(R.string.winged_driver_version, state.versionName),
               Modifier
                  .fillMaxWidth()
                  .wrapContentWidth(Alignment.CenterHorizontally)
            )
         }
      }
   }
}

@Composable
private fun ToolButton(onClick: () -> Unit, icon: Int, text: Int) {
   Button(onClick = onClick, Modifier.sizeIn(minHeight = 60.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
         Icon(
            painterResource(icon),
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
         )

         Text(stringResource(text))
      }
   }
}

@FullScreenPreviews
@Composable
internal fun ToolsScreenPreview() {
   PreviewTheme {
      ToolsScreenContent(
         state = ToolsState("1.0.0-alpha07", emptyPreferences()),
         updatePreference = { _, _ -> },
      )
   }
}
