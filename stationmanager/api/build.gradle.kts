plugins {
   pureKotlinModule
   serialization
   alias(libs.plugins.kotlinRpc)
}

dependencies {
   implementation(libs.kotlin.datetime)
   implementation(libs.kotlin.rpc)
   implementation(libs.kotlin.serialization)
   implementation(libs.kotlin.coroutines)
}
