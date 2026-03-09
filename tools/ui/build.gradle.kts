plugins {
   androidLibraryModule
   compose
   di
   navigation
}

android {
   namespace = "com.krilatokolo.wingeddriver.tools.ui"

   androidResources.enable = true
}

dependencies {
   api(projects.tools.api)
   implementation(libs.androidx.datastore.preferences)
   implementation(projects.common)
   implementation(projects.commonCompose)
   implementation(libs.composePreference)
   implementation(libs.kotlinova.core)
}
