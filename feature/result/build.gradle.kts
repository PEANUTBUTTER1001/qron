plugins {
    id("my.android.feature")
}

android {
    namespace = "com.peanutbutter1001.qron.feature.result"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:vision"))
    implementation(project(":domain"))

    implementation(libs.bundles.compose.core)
}
