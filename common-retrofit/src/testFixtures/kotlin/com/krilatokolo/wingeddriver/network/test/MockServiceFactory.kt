package com.krilatokolo.wingeddriver.network.test

import kotlinx.coroutines.test.TestScope
import com.krilatokolo.wingeddriver.network.di.NetworkProviders
import com.krilatokolo.wingeddriver.network.exceptions.DefaultErrorHandler
import com.krilatokolo.wingeddriver.network.services.BaseServiceFactory
import si.inova.kotlinova.core.test.outcomes.ThrowingErrorReporter
import si.inova.kotlinova.retrofit.MockWebServerScope

fun MockWebServerScope.serviceFactory(testScope: TestScope): BaseServiceFactory {
   val moshi = NetworkProviders.createMoshi(emptySet())

   return BaseServiceFactory(
      testScope,
      { moshi },
      { NetworkProviders.prepareDefaultOkHttpClient().build() },
      ThrowingErrorReporter(testScope),
      DefaultErrorHandler(),
      baseUrl
   )
}
