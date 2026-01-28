plugins {
   androidLibraryModule
   di
}

dependencies {
   api(projects.commonRetrofit)
   api(libs.dispatch)
   api(libs.kotlinova.core)
   api(libs.moshi)
   api(libs.okhttp)
   api(libs.kotlinova.retrofit)

   implementation(libs.kotlin.coroutines)
}
