plugins {
   pureKotlinModule
   di
}

dependencies {
   api(projects.savedlocos.api)
   implementation(projects.common)
   implementation(projects.commonRetrofit)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.core)
   implementation(libs.okhttp)
   implementation(libs.retrofit)
}
