plugins {
    id("my.android.library")
    id("my.android.compose")
    id("my.android.hilt")
}

android {
    namespace = "com.peanutbutter1001.qron.feature.result"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":domain"))

    implementation(project(":data"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.bundles.compose.core)
    implementation(libs.hilt.navigation.compose)
    debugImplementation(libs.bundles.compose.debug)
}
