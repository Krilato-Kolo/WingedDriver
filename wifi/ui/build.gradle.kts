plugins {
   androidLibraryModule
   compose
   di
   navigation
}

android {
   namespace = "com.krilatokolo.wingeddriver.wifi.ui"

   androidResources.enable = true
}

dependencies {
   api(projects.wifi.api)

   implementation(projects.commonCompose)
   implementation(libs.androidx.datastore.preferences)
   implementation(libs.kotlinova.core)
   implementation(libs.kotlinova.compose)
}
