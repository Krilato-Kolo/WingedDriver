package com.krilatokolo.wingeddriver.savedlocos.di

import com.krilatokolo.wingeddriver.network.services.ServiceFactory
import com.krilatokolo.wingeddriver.network.services.create
import com.krilatokolo.wingeddriver.savedlocos.SavedLocosService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface SavedLocosFactories {
   @Provides
   @SingleIn(AppScope::class)
   private fun provideSavedLocosService(serviceFactory: ServiceFactory): SavedLocosService = serviceFactory.create()
}
