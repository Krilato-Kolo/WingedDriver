package com.krilatokolo.wingeddriver

import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import com.krilatokolo.wingeddriver.controller.AbstractController
import com.krilatokolo.wingeddriver.controller.ControllerPacket
import com.krilatokolo.wingeddriver.controller.UsbDriverListener
import com.krilatokolo.wingeddriver.controller.UsbDriverService
import com.krilatokolo.wingeddriver.ui.theme.WingedDriverTheme
import com.zhuinden.simplestack.Backstack
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import si.inova.kotlinova.compose.result.LocalResultPassingStore
import si.inova.kotlinova.compose.result.ResultPassingStore
import si.inova.kotlinova.compose.time.ComposeAndroidDateTimeFormatter
import si.inova.kotlinova.compose.time.LocalDateFormatter
import si.inova.kotlinova.core.time.AndroidDateTimeFormatter
import si.inova.kotlinova.navigation.deeplink.HandleNewIntentDeepLinks
import si.inova.kotlinova.navigation.deeplink.MainDeepLinkHandler
import si.inova.kotlinova.navigation.di.NavigationContext
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.navigation3.NavDisplay
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

class MainActivity : ComponentActivity(), UsbDriverListener {
   private lateinit var navigationInjectionFactory: NavigationInjection.Factory
   private lateinit var mainDeepLinkHandler: MainDeepLinkHandler
   private lateinit var navigationContext: NavigationContext
   private lateinit var dateFormatter: AndroidDateTimeFormatter
   private lateinit var mainViewModelFactory: MainViewModel.Factory

   private lateinit var activityStartedRepository: ActivityStartedRepositoryImpl

   private val viewModel by viewModels<MainViewModel>() { ViewModelFactory() }
   private var initComplete = false
   private var controllerDispatcher = GamepadDispatcher()
   private val usbDriverService = UsbDriverService(this)

   private var lastButtonFlags = 0
   private var lastLeftTrigger = 0f
   private var lastRightTrigger = 0f
   private var lastHatX = 0f
   private var lastHatY = 0f
   private var repeaterJob: Job? = null
   private var repeaterButton: Int? = null

   override fun onCreate(savedInstanceState: Bundle?) {
      val appGraph = (requireNotNull(application) as WingedDriverApplication).applicationGraph

      navigationInjectionFactory = appGraph.getNavigationInjectionFactory()
      mainDeepLinkHandler = appGraph.getMainDeepLinkHandler()
      navigationContext = appGraph.getNavigationContext()
      dateFormatter = appGraph.getDateFormatter()
      mainViewModelFactory = appGraph.getMainViewModelFactory()
      activityStartedRepository = appGraph.getActivityStartedRepository()

      super.onCreate(savedInstanceState)
      enableEdgeToEdge()

      window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

      val splashScreen = installSplashScreen()
      splashScreen.setKeepOnScreenCondition { !initComplete }

      beginInitialisation(savedInstanceState == null)

      usbDriverService.listener = this
      usbDriverService.onCreate()
   }

   override fun onDestroy() {
      usbDriverService.onDestroy()
      super.onDestroy()
   }

   override fun onStart() {
      super.onStart()
      activityStartedRepository.activityStarted.value = true
   }

   override fun onStop() {
      activityStartedRepository.activityStarted.value = false
      super.onStop()
   }

   private fun beginInitialisation(startup: Boolean) {
      lifecycleScope.launch {
         val initialHistory: List<ScreenKey> = listOf(viewModel.startingScreen.filterNotNull().first())

         val deepLinkTarget = if (startup) {
            intent?.data?.let { mainDeepLinkHandler.handleDeepLink(it, startup = true) }
         } else {
            null
         }

         val overridenInitialHistoryFromDeepLink = if (deepLinkTarget != null) {
            deepLinkTarget.performNavigation(initialHistory, navigationContext).newBackstack
         } else {
            initialHistory
         }

         setContent {
            NavigationRoot(overridenInitialHistoryFromDeepLink)
         }

         initComplete = true
      }
   }

   @Composable
   private fun NavigationRoot(initialHistory: List<ScreenKey>) {
      WingedDriverTheme {
         // A surface container using the 'background' color from the theme
         Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
         ) {
            val resultPassingStore = rememberSaveable { ResultPassingStore() }
            CompositionLocalProvider(
               LocalDateFormatter provides ComposeAndroidDateTimeFormatter(dateFormatter),
               LocalResultPassingStore provides resultPassingStore,
               LocalGamepadDispatcher provides controllerDispatcher,
            ) {
               val backstack = navigationInjectionFactory.NavDisplay(
                  initialHistory = { initialHistory },
                  entryDecorators = listOf(
                     rememberSaveableStateHolderNavEntryDecorator(),
                     NavEntryDecorator<ScreenKey>(
                        decorate = { targetNavEntry ->
                           Surface {
                              targetNavEntry.Content()
                           }
                        }
                     )

                  )
               )

               LogCurrentScreen(backstack)

               mainDeepLinkHandler.HandleNewIntentDeepLinks(this@MainActivity, backstack)
            }
         }
      }
   }

   override fun reportControllerState(
      controllerId: Int,
      buttonFlags: Int,
      leftStickX: Float,
      leftStickY: Float,
      rightStickX: Float,
      rightStickY: Float,
      leftTrigger: Float,
      rightTrigger: Float,
   ) {
      reportButtonState(buttonFlags, lastButtonFlags)
      lastButtonFlags = buttonFlags

      controllerDispatcher.currentInstance?.let {
         if (leftTrigger != lastLeftTrigger) {
            it.onLeftTriggerUpdate(leftTrigger)
            lastLeftTrigger = leftTrigger
         }
         if (rightTrigger != lastRightTrigger) {
            it.onRightTriggerUpdate(rightTrigger)
            lastRightTrigger = rightTrigger
         }
      }
   }

   @Suppress("CognitiveComplexMethod")
   private fun reportButtonState(current: Int, previous: Int) {
      for (button in ControllerPacket.ALL_BUTOTNS) {
         val masked = current and button
         val previousMasked = previous and button

         if (masked != previousMasked) {
            runOnUiThread {
               if (masked == 0) {
                  onControllerButtonReleased(button)
               } else {
                  onControllerButtonPressed(button)
               }
            }
         }
      }
   }

   private fun onControllerButtonPressed(button: Int) {
      if (button in REPEAT_BUTTONS) {
         runWithRepeat(button) {
            controllerDispatcher.currentInstance?.onButtonPressed(button)
         }
      } else {
         controllerDispatcher.currentInstance?.onButtonPressed(button)
      }
   }

   private fun onControllerButtonReleased(button: Int) {
      controllerDispatcher.currentInstance?.onButtonReleased(button)

      if (repeaterButton == button) {
         repeaterJob?.cancel()
      }
   }

   @Suppress("MagicNumber")
   private fun runWithRepeat(button: Int, function: () -> Unit) {
      repeaterJob?.cancel()
      repeaterButton = button
      repeaterJob = lifecycleScope.launch {
         function()

         delay(500)
         while (isActive) {
            function()
            delay(100)
         }
      }
   }

   override fun deviceRemoved(controller: AbstractController?) {
      controllerDispatcher.currentInstance?.onControllerDisconnected()
      repeaterJob?.cancel()
   }

   override fun deviceAdded(controller: AbstractController?) {
   }

   override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
      val button = keycodeToControllerPacketButton(keyCode) ?: return false
      if (event.repeatCount == 0) {
         onControllerButtonPressed(button)
      }
      return true
   }

   override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
      val button = keycodeToControllerPacketButton(keyCode) ?: return false
      if (event.repeatCount == 0) {
         onControllerButtonReleased(button)
      }
      return true
   }

   @Suppress("CognitiveComplexMethod", "MagicNumber") // Not that complex, just a lot of copy paste
   override fun onGenericMotionEvent(event: MotionEvent): Boolean {
      if (event.isFromSource(InputDevice.SOURCE_JOYSTICK)) {
         controllerDispatcher.currentInstance?.let {
            val leftTrigger = event.getAxisValue(MotionEvent.AXIS_LTRIGGER)
            val rightTrigger = event.getAxisValue(MotionEvent.AXIS_RTRIGGER)
            val hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X)
            val hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y)

            if (leftTrigger != lastLeftTrigger) {
               it.onLeftTriggerUpdate(leftTrigger)
               lastLeftTrigger = leftTrigger
            }
            if (rightTrigger != lastRightTrigger) {
               it.onRightTriggerUpdate(rightTrigger)
               lastRightTrigger = rightTrigger
            }
            if (lastHatX != hatX) {
               if (lastHatX > 0.5f) {
                  onControllerButtonReleased(ControllerPacket.LEFT_FLAG)
               }
               if (lastHatX < -0.5f) {
                  onControllerButtonReleased(ControllerPacket.RIGHT_FLAG)
               }

               if (hatX > 0.5f) {
                  onControllerButtonPressed(ControllerPacket.LEFT_FLAG)
               }
               if (hatX < -0.5f) {
                  onControllerButtonPressed(ControllerPacket.RIGHT_FLAG)
               }
               lastHatX = hatX
            }
            if (lastHatY != hatY) {
               if (lastHatY > 0.5f) {
                  onControllerButtonReleased(ControllerPacket.UP_FLAG)
               }
               if (lastHatY < -0.5f) {
                  onControllerButtonReleased(ControllerPacket.DOWN_FLAG)
               }

               if (hatY > 0.5f) {
                  onControllerButtonPressed(ControllerPacket.UP_FLAG)
               }
               if (hatY < -0.5f) {
                  onControllerButtonPressed(ControllerPacket.DOWN_FLAG)
               }
               lastHatY = hatY
            }
         }

         return true
      }

      return false
   }

   private fun keycodeToControllerPacketButton(keycode: Int): Int? {
      return when (keycode) {
         KeyEvent.KEYCODE_BUTTON_A -> ControllerPacket.A_FLAG
         KeyEvent.KEYCODE_BUTTON_B -> ControllerPacket.B_FLAG
         KeyEvent.KEYCODE_BUTTON_X -> ControllerPacket.X_FLAG
         KeyEvent.KEYCODE_BUTTON_Y -> ControllerPacket.Y_FLAG
         KeyEvent.KEYCODE_BUTTON_L1 -> ControllerPacket.LB_FLAG
         KeyEvent.KEYCODE_BUTTON_R1 -> ControllerPacket.RB_FLAG
         KeyEvent.KEYCODE_BUTTON_START -> ControllerPacket.PLAY_FLAG
         KeyEvent.KEYCODE_BUTTON_SELECT -> ControllerPacket.BACK_FLAG
         KeyEvent.KEYCODE_BUTTON_THUMBL -> ControllerPacket.LS_CLK_FLAG
         KeyEvent.KEYCODE_BUTTON_THUMBR -> ControllerPacket.RS_CLK_FLAG
         else -> null
      }
   }

   private inner class ViewModelFactory : ViewModelProvider.Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
         @Suppress("UNCHECKED_CAST")
         return mainViewModelFactory.create() as T
      }
   }
}

@Composable
private fun LogCurrentScreen(backstack: Backstack) {
   DisposableEffect(backstack) {
      val listener = Backstack.CompletionListener { stateChange ->
         @Suppress("UNUSED_VARIABLE") // TODO use it
         val newTopKey = stateChange.topNewKey<ScreenKey>()

         // TODO log new top key here to the crash reporting service, such as Firebase
         //  (and ideally set a Key) to make debugging crashes / error reports easier
      }

      backstack.addStateChangeCompletionListener(listener)

      onDispose { backstack.removeStateChangeCompletionListener(listener) }
   }
}

private val REPEAT_BUTTONS = listOf(ControllerPacket.LB_FLAG, ControllerPacket.RB_FLAG)
