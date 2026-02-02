plugins {
   androidLibraryModule
   di
}

dependencies {
   api(projects.driving.api)
   api(projects.wifi.api)

   implementation(libs.androidx.core)
   implementation(libs.dispatch)
   implementation(libs.kotlin.coroutines)

   testImplementation(testFixtures(projects.common))
}
