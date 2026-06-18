plugins {
    id("my.android.library")
    id("my.android.hilt")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.peanutbutter1001.qron.data"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:database"))
    
    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    
    // ML Kit Barcode
    implementation(libs.mlkit.barcode.scanning)
    
    // Coroutines
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.play.services)
}
