plugins {
    id("my.android.library")
    id("my.android.compose")
    id("my.android.hilt")
}

android {
    namespace = "com.peanutbutter1001.qron.feature.history"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":domain"))

    implementation(project(":data")) // Often needed in feature for previews/fakes or specific mappings, though strictly ViewModel needs domain.
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.bundles.compose.core)
    implementation(libs.hilt.navigation.compose)
    debugImplementation(libs.bundles.compose.debug)
}
