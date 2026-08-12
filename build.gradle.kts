import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.detekt) apply false
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

tasks.register("detektAll") {
    description = "Runs detekt across every module and every Kotlin source set (KMP and Android)."
    group = "verification"
    dependsOn(
        provider {
            subprojects.flatMap { it.tasks.withType<Detekt>() }
        }
    )
}