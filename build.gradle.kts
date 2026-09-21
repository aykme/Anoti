import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import kotlinx.kover.gradle.plugin.dsl.KoverReportFiltersConfig
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

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

// Read here, not inside `subprojects`: the catalog's accessor is only registered on a project
// once that project is being evaluated, which is after the block below runs.
val robolectricSdk = libs.versions.robolectricSdk.get()

subprojects {
    plugins.withId("io.gitlab.arturbosch.detekt") {
        val detektExtension = extensions.getByType<DetektExtension>()
        detektExtension.buildUponDefaultConfig = true
        detektExtension.config.setFrom(files("$rootDir/config/detekt/detekt.yml"))

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
        // commonTest would otherwise never be analyzed.
        val commonTestSources = file("src/commonTest/kotlin")
        if (commonTestSources.isDirectory) {
            tasks.register<Detekt>("detektCommonTest") {
                description = "Runs detekt over the commonTest source set."
                group = "verification"
                setSource(files(commonTestSources))
                // The plugin wires the extension's settings only into the tasks it registers
                // itself, so a hand-registered one would run detekt's stock config instead.
                config.setFrom(detektExtension.config)
                buildUponDefaultConfig = detektExtension.buildUponDefaultConfig
                // Same reason: report names also come from the plugin's own registration, and the
                // fallback name collides with the bare `detekt` task's.
                listOf(reports.xml, reports.html, reports.txt, reports.sarif, reports.md)
                    .forEach { report ->
                        report.outputLocation.convention(
                            layout.buildDirectory
                                .file("reports/detekt/commonTest.${report.type.extension}")
                        )
                    }
            }
        }
    }

    // Robolectric reads this file off the test classpath, so the SDK it emulates is set once from
    // the version catalog instead of being repeated in an annotation on every test class.
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        // A plain local, so the task's action holds the value rather than this build script.
        val sdk = robolectricSdk
        val configDirectory = layout.buildDirectory.dir("generated/robolectric")

        extensions.configure<KotlinMultiplatformExtension> {
            // The source set appears only once the android target is declared, which is after
            // this plugin is applied. So react to its creation instead of looking it up now.
            // A module that never gets one then has nothing registered for it.
            sourceSets.configureEach {
                if (name != "androidHostTest") return@configureEach

                val generateRobolectricConfig = tasks.register("generateRobolectricConfig") {
                    description = "Writes the Robolectric properties this module's host tests read."
                    group = "build"
                    inputs.property("sdk", sdk)
                    outputs.dir(configDirectory)
                    doLast {
                        val directory = configDirectory.get().asFile
                        directory.mkdirs()
                        directory.resolve("robolectric.properties").writeText("sdk=$sdk\n")
                    }
                }
                resources.srcDir(generateRobolectricConfig)
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

            // Each report file is named after the Kotlin module name. Kotlin/Native builds that
            // name from the Gradle path, and on Windows its colons divert the content into an NTFS
            // alternate data stream, leaving an empty file behind. The JVM default already has no
            // colons and already tells main apart from hostTest, so renaming it there would only
            // make the two compilations overwrite each other's report.
            tasks.withType<KotlinNativeCompile>().configureEach {
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
    pluginManager.apply("org.jetbrains.kotlinx.kover")

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
