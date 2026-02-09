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
   api(projects.common)
   api(projects.commonAndroid)
   implementation(projects.commonCompose)

   implementation(libs.dispatch)
   implementation(libs.kotlinova.core)
   implementation(libs.kotlinova.compose)

   testImplementation(testFixtures(projects.common))
}
