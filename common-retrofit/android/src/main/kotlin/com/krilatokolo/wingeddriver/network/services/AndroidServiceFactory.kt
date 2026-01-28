package com.krilatokolo.wingeddriver.network.services

import com.squareup.moshi.Moshi
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import dispatch.core.DefaultCoroutineScope
import okhttp3.Cache
import okhttp3.OkHttpClient
import com.krilatokolo.wingeddriver.network.exceptions.DefaultErrorHandler
import si.inova.kotlinova.core.reporting.ErrorReporter
import si.inova.kotlinova.retrofit.caching.GlobalOkHttpDiskCacheManager

@Inject
class AndroidServiceFactory(
   moshi: Provider<Moshi>,
   errorReporter: ErrorReporter,
   okHttpClient: Provider<OkHttpClient>,
   defaultCoroutineScope: DefaultCoroutineScope,
   defaultErrorHandler: DefaultErrorHandler,
   @BaseUrl
   baseUrl: String,
   private val cacheManager: GlobalOkHttpDiskCacheManager,
) : BaseServiceFactory(defaultCoroutineScope, moshi, okHttpClient, errorReporter, defaultErrorHandler, baseUrl) {
   override fun createCache(): Cache {
      return cacheManager.cache
   }
}
