import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.HostTestBuilder
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
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
val minSdk = libs.versions.minSdk.get()

// The floor the whole project's line coverage may not fall below. It sits below what the
// project holds on purpose, so the gate catches a change that arrived with no tests rather
// than a branch no test can reach. `koverVerify` checks it; CLAUDE.md says when that is run.
val wholeProjectLineCoverageMinimum = 95

/** Writes the Robolectric properties a module's host tests read off their classpath. */
abstract class GenerateRobolectricConfig : DefaultTask() {

    @get:Input
    abstract val sdk: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val directory = outputDirectory.get().asFile
        directory.mkdirs()
        directory.resolve("robolectric.properties").writeText("sdk=${sdk.get()}\n")
    }
}

/** Registers the generator under [taskName], writing into [directory]. */
fun Project.registerRobolectricConfig(
    sdk: String,
    taskName: String = "generateRobolectricConfig",
    directory: String = "generated/robolectric"
): TaskProvider<GenerateRobolectricConfig> =
    tasks.register<GenerateRobolectricConfig>(taskName) {
        description = "Writes the Robolectric properties this module's host tests read."
        group = "build"
        this.sdk.set(sdk)
        outputDirectory.set(layout.buildDirectory.dir(directory))
    }

/** Registers the task writing the SDK levels this module's host tests can name in an annotation. */
fun Project.registerTestSdkVersions(oldestSupportedSdk: String): TaskProvider<Task> {
    val directory = layout.buildDirectory.dir("generated/testSdk")
    return tasks.register("generateTestSdkVersions") {
        description = "Writes the SDK levels host tests can name in an annotation."
        group = "build"
        inputs.property("minSdk", oldestSupportedSdk)
        outputs.dir(directory)
        doLast {
            val packageDirectory = directory.get().asFile
                .resolve("com/alekseivinogradov/anoti/testsdk")
            packageDirectory.mkdirs()
            packageDirectory.resolve("TestSdkVersions.kt").writeText(
                """
                package com.alekseivinogradov.anoti.testsdk

                /** The oldest Android version the app supports. */
                const val MIN_SDK = $oldestSupportedSdk

                """.trimIndent()
            )
        }
    }
}

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

        // The detekt Gradle plugin registers tasks per compilation, so a shared test source set
        // belongs to none of them: the tasks it does register for the leaf targets report
        // NO-SOURCE over these paths, and nothing else covers them.
        mapOf(
            "commonTest" to "src/commonTest/kotlin",
            "iosTest" to "src/iosTest/kotlin",
            "androidTest" to "src/androidTest/kotlin"
        )
            .forEach { (sourceSetName, path) ->
                val sources = file(path)
                if (!sources.isDirectory) return@forEach

                val taskName = "detekt${sourceSetName.replaceFirstChar(Char::uppercaseChar)}"
                tasks.register<Detekt>(taskName) {
                    description = "Runs detekt over the $sourceSetName source set."
                    group = "verification"
                    setSource(files(sources))
                    // The plugin wires the extension's settings only into the tasks it registers
                    // itself, so a hand-registered one would run detekt's stock config instead.
                    config.setFrom(detektExtension.config)
                    buildUponDefaultConfig = detektExtension.buildUponDefaultConfig
                    // Same reason: report names also come from the plugin's own registration, and
                    // the fallback name collides with the bare `detekt` task's.
                    listOf(reports.xml, reports.html, reports.txt, reports.sarif, reports.md)
                        .forEach { report ->
                            report.outputLocation.convention(
                                layout.buildDirectory
                                    .file("reports/detekt/$sourceSetName.${report.type.extension}")
                            )
                        }
                }
            }
    }

    // Robolectric reads its properties off the test classpath, so the SDK it emulates is set once
    // from the version catalog instead of being repeated in an annotation on every test class.
    // The generated constant is for the rare test that has to name a different level, which an
    // annotation can only take as a compile-time value.
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        extensions.configure<KotlinMultiplatformExtension> {
            // The source set appears only once the android target is declared, which is after
            // this plugin is applied. So react to its creation instead of looking it up now.
            // A module that never gets one then has nothing registered for it.
            sourceSets.configureEach {
                if (name != "androidHostTest") return@configureEach
                // A module can declare the source set and hold no tests. Generating into it
                // would make Gradle see test sources and then fail for finding no tests.
                if (!file("src/androidHostTest/kotlin").isDirectory) return@configureEach

                resources.srcDir(registerRobolectricConfig(robolectricSdk))
                kotlin.srcDir(registerTestSdkVersions(minSdk))
            }
        }
    }

    // The app module is not multiplatform: its host tests are plain Android unit tests, and AGP
    // accepts a generated directory only through the variant API. Same properties, wired per
    // variant. No SDK constant is generated here, since nothing names a level in an annotation.
    // The host-test accessor below is incubating; the stable one it replaces is deprecated.
    @Suppress("UnstableApiUsage")
    plugins.withId("com.android.application") {
        if (file("src/test/kotlin").isDirectory) {
            extensions.configure<ApplicationAndroidComponentsExtension> {
                onVariants { variant ->
                    val unitTest = variant.hostTests[HostTestBuilder.UNIT_TEST_TYPE]
                    val variantName = variant.name.replaceFirstChar(Char::uppercaseChar)
                    unitTest?.sources?.resources?.addGeneratedSourceDirectory(
                        taskProvider = registerRobolectricConfig(
                            sdk = robolectricSdk,
                            taskName = "generate${variantName}RobolectricConfig",
                            directory = "generated/robolectric/${variant.name}"
                        ),
                        wiredWith = GenerateRobolectricConfig::outputDirectory
                    )
                }
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
    coveredProjects.forEach { kover(project(it.path)) }
}

kover {
    reports {
        filters { excludeUnmeasuredCode() }

        verify {
            // Checked here rather than per module: a module's own report leaves out the coverage
            // its classes get from another module's tests, so it reads lower than the truth.
            rule("Whole project") {
                bound {
                    minValue = wholeProjectLineCoverageMinimum
                    coverageUnits = CoverageUnit.LINE
                    aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                }
            }
        }
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
            "**.AnimeDatabaseConstructor",
            // kotlin-inject
            "**.Inject*Component*",
            // Bodies the Kotlin compiler copies out of an interface into a static holder, so
            // the same lines are not counted twice.
            $$"**$DefaultImpls",
            // The holders the Compose compiler generates for composable lambdas, which carry no
            // code of their own.
            "**.ComposableSingletons$*",
            // Compose Resources
            "com.alekseivinogradov.anoti.**.generated.resources.**",
            // Handwritten test doubles.
            "**Fake*"
        )
        packages(
            // The same doubles, caught by where they live rather than by what they are called.
            "**.fake",
            // core-kmp:test-utils serves the tests rather than the app, so what it does for them
            // is what it is held to.
            "com.alekseivinogradov.anoti.testutils"
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
