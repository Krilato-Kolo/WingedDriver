package com.krilatokolo.wingeddriver.stationmanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krilatokolo.stationmanager.phoneconnection.Stop
import com.krilatokolo.stationmanager.phoneconnection.TrainSchedule
import com.krilatokolo.wingeddriver.navigation.ScheduleSubscreenKey
import com.krilatokolo.wingeddriver.ui.components.ProgressErrorSuccessScaffold
import com.krilatokolo.wingeddriver.ui.debugging.PreviewTheme
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.navigation.di.ContributesScreenBinding
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
@ContributesScreenBinding
class ScheduleSubscreen(
   private val viewmodel: ScheduleSubscreenViewmodel,
) : Screen<ScheduleSubscreenKey>() {
   @Composable
   override fun Content(key: ScheduleSubscreenKey) {
      val state = viewmodel.uiState.collectAsStateWithLifecycleAndBlinkingPrevention()

      ProgressErrorSuccessScaffold(state::value) { schedule ->

         ScheduleContent(schedule)
      }
   }
}

@Composable
private fun ScheduleContent(schedule: List<TrainSchedule>, expanded: Boolean = false) {
   var selectedEntryIndex by remember { mutableStateOf(0) }
   var dropdownExpanded by remember { mutableStateOf(expanded) }

   val selectedEntry = schedule.getOrNull(selectedEntryIndex)

   Column(
      Modifier
         .padding(horizontal = 16.dp)
         .fillMaxSize()
         .verticalScroll(rememberScrollState())
   ) {
      Box {
         Button(onClick = { dropdownExpanded = !dropdownExpanded }) {
            Text(selectedEntry?.name.orEmpty())
         }

         DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false },
         ) {
            schedule.forEachIndexed { index, train ->
               DropdownMenuItem(
                  text = { Text(train.name) },
                  onClick = {
                     selectedEntryIndex = index
                     dropdownExpanded = false
                  }
               )
            }
         }
      }

      for (stop in selectedEntry?.stops.orEmpty()) {
         Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            Text(stop.name, fontSize = 24.sp)

            Spacer(Modifier.weight(1f))

            Text("${stop.from.formatTime()} - ${stop.to.formatTime()}", fontSize = 24.sp)
         }
      }
   }
}

private fun LocalTime?.formatTime(): String {
   return this?.format(TIME_FORMAT) ?: "                "
}

private val TIME_FORMAT = LocalTime.Format {
   hour(); char(':'); minute(); char(':'); second()
}

@Preview
@Composable
private fun SchedulePreview() {
   PreviewTheme {
      val schedule = listOf(
         TrainSchedule(
            "Train A",
            List(5) {
               Stop("Stop $it", LocalTime(6 + it, 0), LocalTime(7 + it, 0))
            } + listOf(
               Stop("Stop 5", null, LocalTime(11, 0)),
               Stop("Stop 6", LocalTime(12, 0), null),
            )
         )
      )

      ScheduleContent(schedule)
   }
}

@Preview
@Composable
private fun ScheduleDropdownPreview() {
   PreviewTheme {
      val schedule = listOf(
         TrainSchedule(
            "Train A",
            emptyList(),
         ),

         TrainSchedule(
            "Train B",
            emptyList(),
         ),

         TrainSchedule(
            "Train C",
            emptyList(),
         ),
      )

      ScheduleContent(schedule, expanded = true)
   }
}
