plugins {
   androidLibraryModule
   di
}

dependencies {
   api(projects.stationmanager.api)
   api(projects.wifi.api)

   implementation(libs.kotlin.coroutines)
   implementation(libs.ktor.client)
   implementation(libs.ktor.okhttp)
   implementation(libs.okhttp)
   implementation(libs.kotlin.rpc.json)
   implementation(libs.kotlin.rpc.client)
   implementation(libs.kotlin.rpc.ktorClient)
   implementation(libs.logcat)
}
