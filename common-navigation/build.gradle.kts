plugins {
   androidLibraryModule
   compose
   parcelize
}

dependencies {
   api(libs.kotlinova.navigation)
   implementation(libs.androidx.activity.compose)
   implementation(libs.kotlinova.core)
   implementation(libs.kotlinova.compose)
   implementation(libs.kotlin.coroutines)
}
