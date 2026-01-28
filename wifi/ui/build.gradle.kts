plugins {
   androidLibraryModule
   compose
   di
   navigation
}

android {
   namespace = "com.krilatokolo.wingeddriver.wifi.ui"

   androidResources.enable = true
}

dependencies {
   api(projects.wifi.api)
}
