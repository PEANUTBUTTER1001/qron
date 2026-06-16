plugins {
    id("my.android.application")
    id("my.android.compose")
    id("my.android.hilt")
}

android {
    namespace = "com.peanutbutter1001.qron"

    defaultConfig {
        applicationId = "com.peanutbutter1001.qron"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    // Android Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.bundles.compose.core)
    debugImplementation(libs.bundles.compose.debug)
}
