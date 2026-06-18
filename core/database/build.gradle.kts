plugins {
    id("my.android.library")
    id("my.android.hilt")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.peanutbutter1001.qron.core.database"
}

dependencies {
    implementation(project(":domain"))

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}
