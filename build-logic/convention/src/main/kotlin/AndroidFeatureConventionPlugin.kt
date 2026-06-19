import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("my.android.library")
                apply("my.android.compose")
                apply("my.android.hilt")
            }

            val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

            dependencies {
                add("implementation", libs.findLibrary("androidx.core.ktx").get())
                add("implementation", libs.findLibrary("androidx.lifecycle.runtime.ktx").get())
                add("implementation", libs.findBundle("compose.core").get())
                add("implementation", libs.findLibrary("hilt.navigation.compose").get())
                add("debugImplementation", libs.findBundle("compose.debug").get())
            }
        }
    }
}
