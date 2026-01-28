package com.krilatokolo.wingeddriver.network.di

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import okhttp3.OkHttpClient
import si.inova.kotlinova.retrofit.interceptors.BypassCacheInterceptor
import java.time.Duration

@ContributesTo(AppScope::class)
interface NetworkProviders {
   // Uncomment when adding adapters
   // val moshiAdapters: Set<MoshiAdapter>

   @Provides
   @SingleIn(AppScope::class)
   fun provideMoshi(
      adapters: Set<@JvmSuppressWildcards MoshiAdapter> = emptySet(), // Remove empty set when adding adapters
   ): Moshi {
      if (Thread.currentThread().name == "main") {
         error("Moshi should not be initialized on the main thread")
      }

      return createMoshi(adapters)
   }

   @Provides
   @SingleIn(AppScope::class)
   fun provideOkHttpClient(): OkHttpClient {
      if (Thread.currentThread().name == "main") {
         error("OkHttp should not be initialized on the main thread")
      }

      return prepareDefaultOkHttpClient().build()
   }

   companion object {
      fun prepareDefaultOkHttpClient(): OkHttpClient.Builder {
         return OkHttpClient.Builder()
            .addInterceptor(BypassCacheInterceptor())
            .callTimeout(DEFAULT_TIMEOUT)
            .readTimeout(DEFAULT_TIMEOUT)
            .writeTimeout(DEFAULT_TIMEOUT)
            .connectTimeout(DEFAULT_TIMEOUT)
      }

      fun createMoshi(
         adapters: Set<@JvmSuppressWildcards MoshiAdapter> = emptySet(), // Remove empty set when adding adapters
      ): Moshi {
         if (Thread.currentThread().name == "main") {
            error("Moshi should not be initialized on the main thread")
         }

         return Moshi.Builder().also { builder ->
            for (adapter in adapters) {
               if (adapter is JsonAdapter.Factory) {
                  builder.addLast(adapter)
               } else {
                  builder.addLast(adapter)
               }
            }
         }.build()
      }
   }
}

interface MoshiAdapter

private val DEFAULT_TIMEOUT = Duration.ofSeconds(10)
