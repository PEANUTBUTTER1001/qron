plugins {
    id("my.android.library")
    id("my.android.hilt")
}

android {
    namespace = "com.peanutbutter1001.qron.core.vision"
}

dependencies {
    implementation(project(":domain"))

    // ML Kit Barcode
    implementation(libs.mlkit.barcode.scanning)

    // Coroutines
    implementation(libs.coroutines.android)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
}
