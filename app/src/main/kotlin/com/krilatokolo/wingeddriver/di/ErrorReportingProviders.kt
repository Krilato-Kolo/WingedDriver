package com.krilatokolo.wingeddriver.di

import android.content.Context
import com.krilatokolo.wingeddriver.BuildConfig
import com.krilatokolo.wingeddriver.common.exceptions.CrashOnDebugException
import com.krilatokolo.wingeddriver.crashreport.CrashReportService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import logcat.logcat
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.reporting.ErrorReporter

@Suppress("unused")
@ContributesTo(AppScope::class)
interface ErrorReportingProviders {
   @Provides
   fun provideErrorReporter(context: Context): ErrorReporter {
      return object : ErrorReporter {
         override fun report(throwable: Throwable) {
            if (throwable !is CauseException) {
               report(UnknownCauseException("Got reported non-cause exception", throwable))
               return
            }

            if (throwable.shouldReport) {
               logcat { "Reporting $throwable to Firebase" }
               throwable.printStackTrace()
               CrashReportService.showCrashNotification(throwable.stackTraceToString(), context)
            } else if (BuildConfig.DEBUG) {
               if (throwable is CrashOnDebugException) {
                  throw throwable
               }
               throwable.printStackTrace()
            }
         }
      }
   }
}
