import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import kotlinx.kover.gradle.plugin.dsl.KoverReportFiltersConfig

// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kover)
}

subprojects {
    plugins.withId("io.gitlab.arturbosch.detekt") {
        extensions.configure<DetektExtension> {
            buildUponDefaultConfig = true
            config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        }

        dependencies.add("detektPlugins", libs.detekt.formatting)

        tasks.withType<Detekt>().configureEach {
            // Ant-style exclude() patterns match paths relative to each source root, but
            // generated-source roots (KSP, Compose resources) live inside build/ themselves,
            // so "build" never appears in a path relative to them — filter on the absolute
            // path instead.
            exclude { it.file.invariantSeparatorsPath.contains("/build/") }
        }
    }
}

// Coverage comes from the Android host test runs, which is where commonTest executes.
// Only projects with their own build script are measured. ":core-kmp" and ":feature-kmp" exist
// solely as path segments and have nothing to instrument.
val coveredProjects = subprojects.filter { it.buildFile.exists() }

configure(coveredProjects) {
    apply(plugin = "org.jetbrains.kotlinx.kover")

    extensions.configure<KoverProjectExtension> {
        reports {
            filters { excludeUnmeasuredCode() }
        }
    }
}

dependencies {
    coveredProjects.forEach { kover(it) }
}

kover {
    reports {
        filters { excludeUnmeasuredCode() }
    }
}

// Applied to every project as well as to the aggregated report, so a single module's report and
// the project-wide one count the same classes.
fun KoverReportFiltersConfig.excludeUnmeasuredCode() {
    excludes {
        // Composables are not exercised by commonTest, so they would only add noise.
        annotatedBy("androidx.compose.runtime.Composable")
        classes(
            // Room
            "**_Impl*",
            // kotlin-inject
            "**.Inject*Component*",
            // Compose Resources
            "com.alekseivinogradov.anoti.**.generated.resources.**"
        )
    }
}

tasks.register("detektAll") {
    description = "Runs detekt across every module and every Kotlin source set (KMP and Android)."
    group = "verification"
    dependsOn(
        provider {
            subprojects.flatMap { it.tasks.withType<Detekt>() }
        }
    )
}
