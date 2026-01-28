plugins {
   pureKotlinModule
   di
   testFixtures
}

dependencies {
   api(libs.kotlinova.core)
   api(libs.kotlinova.retrofit)
   api(libs.moshi)
   api(libs.okhttp)
   api(libs.retrofit)

   implementation(libs.retrofit.moshi)
   implementation(libs.kotlin.coroutines)

   testImplementation(testFixtures(projects.commonRetrofit))
   testImplementation(libs.kotlinova.retrofit.test)
   testImplementation(libs.okhttp.mockWebServer)
   testImplementation(libs.turbine)

   testFixturesApi(libs.kotlinova.retrofit.test)
   testFixturesImplementation(libs.kotlin.coroutines)
   testFixturesImplementation(libs.kotlinova.core)
   testFixturesImplementation(libs.kotlinova.core.test)
   testFixturesImplementation(libs.moshi)
   testFixturesImplementation(libs.okhttp)
   testFixturesImplementation(libs.kotlin.coroutines.test)
}
