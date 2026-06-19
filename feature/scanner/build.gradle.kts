plugins {
    id("my.android.feature")
}

android {
    namespace = "com.peanutbutter1001.qron.feature.scanner"
}

dependencies {
    implementation(project(":core:vision"))
    implementation(project(":core:designsystem"))
    implementation(project(":domain"))

    implementation(project(":feature:result")) // For navigation to ScanResultActivity

    // CameraX
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    // ML Kit
    implementation(libs.mlkit.barcode.scanning)
}
