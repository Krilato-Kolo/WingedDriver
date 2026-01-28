plugins {
   androidLibraryModule
   compose
}

android {

   namespace = "com.krilatokolo.wingeddriver.crashreport"
   buildFeatures {
      androidResources = true
   }
}

dependencies {
   implementation(projects.commonCompose)
   implementation(libs.androidx.activity.compose)
   implementation(libs.androidx.core)
   implementation(libs.androidx.startup)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.core)
}
