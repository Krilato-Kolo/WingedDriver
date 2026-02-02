plugins {
   androidLibraryModule
   di
}

dependencies {
   api(projects.wifi.api)

   implementation(projects.common)
   implementation(libs.androidx.core)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.core)
}
