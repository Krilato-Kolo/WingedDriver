plugins {
   androidLibraryModule
   compose
   di
   navigation
}

android {
   namespace = "com.krilatokolo.wingeddriver.stationmanager.ui"

   buildFeatures {
      androidResources = true
   }
}

dependencies {
   api(projects.stationmanager.api)
   implementation(projects.commonCompose)
   implementation(libs.kotlin.datetime)
   implementation(libs.kotlinova.core)
}
