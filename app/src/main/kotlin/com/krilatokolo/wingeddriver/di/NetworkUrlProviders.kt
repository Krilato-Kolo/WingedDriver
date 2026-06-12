package com.krilatokolo.wingeddriver.di

import com.krilatokolo.wingeddriver.BuildConfig
import com.krilatokolo.wingeddriver.network.services.BaseServiceFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@ContributesTo(AppScope::class)
interface NetworkUrlProviders {
   @Provides
   @BaseServiceFactory.BaseUrl
   fun provideBaseUrl(): String {
      return BuildConfig.BACKEND_URL
   }
}
