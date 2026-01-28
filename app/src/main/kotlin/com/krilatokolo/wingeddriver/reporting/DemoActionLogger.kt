package com.krilatokolo.wingeddriver.reporting

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import logcat.logcat
import com.krilatokolo.wingeddriver.common.logging.ActionLogger

@ContributesBinding(AppScope::class)
@Inject
class DemoActionLogger : ActionLogger {
   override fun logAction(text: () -> String) {
      // TODO ideally here log actions somewhere where it can be useful (such as Firebase's Crashlytics)
      logcat(message = text)
   }
}
