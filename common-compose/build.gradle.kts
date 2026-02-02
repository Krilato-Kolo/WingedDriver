plugins {
   androidLibraryModule
   compose
   parcelize
   showkase
}

android {
   namespace = "com.krilatokolo.wingeddriver.ui"

   androidResources.enable = true
}

dependencies {
   implementation(projects.common)
   implementation(libs.kotlinova.core)
   implementation(libs.kotlinova.compose)
   implementation(libs.coil)
   implementation(libs.coil.okhttp)
}
