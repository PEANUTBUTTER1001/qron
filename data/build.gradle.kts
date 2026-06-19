plugins {
    id("my.android.library")
    id("my.android.hilt")
}

android {
    namespace = "com.peanutbutter1001.qron.data"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:database"))
    
    // Coroutines
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.play.services)
}
