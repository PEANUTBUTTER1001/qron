plugins {
    id("my.android.feature")
}

android {
    namespace = "com.peanutbutter1001.qron.feature.history"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":domain"))

    implementation(libs.bundles.compose.core)
}
