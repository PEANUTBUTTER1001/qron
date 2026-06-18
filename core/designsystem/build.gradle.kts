plugins {
    id("my.android.library")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.peanutbutter1001.qron.core.designsystem"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
}
