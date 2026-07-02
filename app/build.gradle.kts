plugins {
    id("my.android.application")
    id("my.android.compose")
    id("my.android.hilt")
    alias(libs.plugins.kotlin.serialization)
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

    // Navigation (type-safe)
    implementation(libs.androidx.navigation.compose)

    // Modules
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":feature:history"))
    implementation(project(":feature:scanner"))
    implementation(project(":feature:result"))
    implementation(project(":feature:scan"))
    implementation(project(":core:designsystem"))

    // CameraX
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    // Hilt Navigation Compose
    implementation(libs.hilt.navigation.compose)

    // ML Kit (for InputImage)
    implementation(libs.mlkit.barcode.scanning)
}
