plugins {
    id("my.android.library")
    id("my.android.hilt")
}

android {
    namespace = "com.peanutbutter1001.qron.feature.scan"
}

dependencies {
    implementation(project(":domain"))

    implementation(project(":core:vision"))
    
    // Coroutines
    implementation(libs.coroutines.android)
    
    implementation(libs.androidx.core.ktx)
}
