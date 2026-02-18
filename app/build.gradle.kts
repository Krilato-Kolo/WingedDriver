import com.slack.keeper.optInToKeeper

plugins {
   androidAppModule
   compose
   navigation
   parcelize
   showkase
   id("com.slack.keeper")
}

android {
   namespace = "com.krilatokolo.wingeddriver"

   buildFeatures {
      buildConfig = true
   }

   defaultConfig {
      applicationId = "com.krilatokolo.wingeddriver"
      targetSdk = 33
      versionCode = 1
      versionName = "1.0.0"

      testInstrumentationRunner = "com.krilatokolo.wingeddriver.instrumentation.TestRunner"
      testInstrumentationRunnerArguments += "clearPackageData" to "true"
      // Needed to enable test coverage
      testInstrumentationRunnerArguments += "useTestStorageService" to "true"
   }

   testOptions {
      execution = "ANDROIDX_TEST_ORCHESTRATOR"
   }

   if (providers.gradleProperty("testAppWithProguard").isPresent) {
      testBuildType = "proguardedDebug"
   }

   signingConfigs {
      getByName("debug") {
         // SHA1: C4:B7:55:07:3F:98:8E:94:CB:F8:D4:7C:C6:82:5C:32:03:E1:5A:42
         // SHA256: 87:6B:FF:40:65:00:EE:B9:80:2F:08:D7:67:E6:01:D4:C1:4C:18:51:E0:82:7E:DD:A1:78:EC:5B:05:68:66:AD

         storeFile = File(rootDir, "keys/debug.jks")
         storePassword = "android"
         keyAlias = "androiddebugkey"
         keyPassword = "android"
      }

      create("release") {
         // SHA1: TODO
         // SHA256: TODO

         storeFile = File(rootDir, "keys/release.jks")
         storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
         keyAlias = "app"
         keyPassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
      }
   }

   buildTypes {
      getByName("debug") {
         // TODO uncomment when above signing config becomes valid
         // signingConfig = signingConfigs.getByName("debug")
      }

      create("proguardedDebug") {
         isMinifyEnabled = true
         isShrinkResources = true

         proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro",
         )

         testProguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro",
            "proguard-rules-test.pro"
         )

         matchingFallbacks += "debug"

         signingConfig = signingConfigs.getByName("debug")
      }

      getByName("release") {
         isMinifyEnabled = true
         isShrinkResources = true

         proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
         )

         signingConfig = signingConfigs.getByName("release")
      }
   }

   androidResources {
      generateLocaleConfig = true
   }
}

androidComponents {
   beforeVariants { builder ->
      if (builder.name.contains("proguardedDebug")) {
         builder.optInToKeeper()
      }
   }
}

keeper {
   automaticR8RepoManagement = false
}

custom {
   enableEmulatorTests.set(true)
}

dependencies {
   implementation(projects.common)
   implementation(projects.commonNavigation)
   implementation(projects.commonRetrofit)
   implementation(projects.commonRetrofit.android)
   implementation(projects.commonAndroid)
   implementation(projects.commonCompose)
   implementation(projects.driving.data)
   implementation(projects.driving.ui)
   implementation(projects.logging.crashreport)
   implementation(projects.wifi.data)
   implementation(projects.wifi.ui)

   implementation(libs.androidx.activity.compose)
   implementation(libs.androidx.core)
   implementation(libs.androidx.core.splashscreen)
   implementation(libs.androidx.lifecycle.runtime)
   implementation(libs.androidx.lifecycle.viewModel)
   implementation(libs.androidx.lifecycle.viewModel.compose)
   implementation(libs.androidx.navigation3)
   implementation(libs.androidx.navigation3)
   implementation(libs.accompanist.permissions)
   implementation(libs.coil)
   implementation(libs.dispatch)
   implementation(libs.logcat)
   implementation(libs.moshi)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.core)
   implementation(libs.kotlinova.navigation)
   implementation(libs.kotlinova.navigation.navigation3)
   implementation(libs.simpleStack)

   implementation(libs.androidx.datastore)
   implementation(libs.androidx.datastore.preferences)

   androidTestImplementation(libs.androidx.test.junitRules)
   androidTestImplementation(libs.androidx.test.runner)
   androidTestImplementation(libs.junit4)
   androidTestImplementation(libs.kotlinova.retrofit.test)
   androidTestImplementation(libs.okhttp)
   androidTestImplementation(libs.okhttp.mockWebServer)
   androidTestUtil(libs.androidx.test.orchestrator)
   androidTestUtil(libs.androidx.test.services)

   keeperR8(libs.androidx.r8)
}
