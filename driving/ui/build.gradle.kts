plugins {
   androidLibraryModule
   compose
   di
   navigation
}

android {
   namespace = "com.krilatokolo.wingeddriver.driving.ui"

   buildFeatures {
      androidResources = true
   }
}

dependencies {
   api(projects.driving.api)
   api(projects.wifi.api)
   api(projects.common)
   api(projects.commonAndroid)
   api(libs.androidx.datastore.preferences)

   implementation(projects.tools.api)
   implementation(projects.commonCompose)
   implementation(projects.savedlocos.api)
   implementation(libs.coil)
   implementation(libs.dispatch)
   implementation(libs.kotlinova.core)
   implementation(libs.kotlinova.compose)

   testImplementation(testFixtures(projects.common))
}
