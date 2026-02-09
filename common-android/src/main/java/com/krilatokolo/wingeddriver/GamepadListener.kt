package com.krilatokolo.wingeddriver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.compose.LifecycleResumeEffect

@Composable
fun GamepadListener(
   onLeftTriggerUpdate: (Float) -> Unit = {},
   onRightTriggerUpdate: (Float) -> Unit = {},
   onButtonPressed: (Int) -> Unit = {},
   onButtonReleased: (Int) -> Unit = {},
   onControllerDisconnected: () -> Unit = {},
) {
   val dispatcher = LocalGamepadDispatcher.current
   LifecycleResumeEffect(Unit) {
      dispatcher.currentInstance = GamepadListenerInstance(
         onLeftTriggerUpdate,
         onRightTriggerUpdate,
         onButtonPressed,
         onButtonReleased,
         onControllerDisconnected,
      )

      onPauseOrDispose {
         dispatcher.currentInstance = null
      }
   }
}

data class GamepadListenerInstance(
   val onLeftTriggerUpdate: (Float) -> Unit,
   val onRightTriggerUpdate: (Float) -> Unit,
   val onButtonPressed: (Int) -> Unit,
   val onButtonReleased: (Int) -> Unit,
   val onControllerDisconnected: () -> Unit,
)

class GamepadDispatcher {
   var currentInstance: GamepadListenerInstance? = null
}

val LocalGamepadDispatcher = staticCompositionLocalOf<GamepadDispatcher> { error("Not provided") }
