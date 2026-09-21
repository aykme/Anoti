import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import kotlinx.kover.gradle.plugin.dsl.KoverReportFiltersConfig
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    // Not applied here, only put on the build script's classpath so the subprojects block below
    // can configure its extension.
    alias(libs.plugins.kotlinCompose) apply false
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
        dependencies.add("detektPlugins", libs.detekt.compose)

        tasks.withType<Detekt>().configureEach {
            // Ant-style exclude() patterns match paths relative to each source root, but
            // generated-source roots (KSP, Compose resources) live inside build/ themselves,
            // so "build" never appears in a path relative to them — filter on the absolute
            // path instead.
            exclude { it.file.invariantSeparatorsPath.contains("/build/") }
        }

        // The detekt Gradle plugin only generates tasks for main-compilation source sets, so
        // commonTest would otherwise never be analysed.
        val commonTestSources = file("src/commonTest/kotlin")
        if (commonTestSources.isDirectory) {
            tasks.register<Detekt>("detektCommonTest") {
                description = "Runs detekt over the commonTest source set."
                group = "verification"
                setSource(files(commonTestSources))
                // The plugin wires the extension's settings only into the tasks it registers
                // itself, so a hand-registered one would run detekt's stock config instead.
                config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
                buildUponDefaultConfig = true
            }
        }
    }

    // The reports cost compile time and are only read during a performance pass, so they stay off
    // until -PcomposeCompilerReports asks for them.
    val composeCompilerReportsRequested = providers.gradleProperty("composeCompilerReports").isPresent
    val composeCompilerReportModuleName = path.removePrefix(":").replace(':', '-')

    plugins.withId("org.jetbrains.kotlin.plugin.compose") {
        if (composeCompilerReportsRequested) {
            extensions.configure<ComposeCompilerGradlePluginExtension> {
                val destination = layout.buildDirectory.dir("compose_compiler")
                reportsDestination.set(destination)
                metricsDestination.set(destination)
            }

            // Each report file is named after the Kotlin module name, which defaults to the
            // Gradle path. On Windows its colons would divert the content into an NTFS alternate
            // data stream, leaving an empty file behind.
            tasks.withType<KotlinJvmCompile>().configureEach {
                compilerOptions.moduleName.set(composeCompilerReportModuleName)
            }
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
