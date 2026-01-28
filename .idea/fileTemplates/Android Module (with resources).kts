plugins {
   androidLibraryModule
   di
}

android {
    namespace = "com.krilatokolo.wingeddriver.${NAME}"
    
    buildFeatures {
        androidResources = true
    }
}

dependencies {
    testImplementation(projects.common.test)
}
