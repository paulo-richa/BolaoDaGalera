plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.roborazzi) apply false
    id("com.google.firebase.appdistribution") version "5.1.1" apply false
}

subprojects {
    plugins.withId("io.gitlab.arturbosch.detekt") {
        tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
            exclude("**/build/**", "**/generated/**")
        }
        // DetektCreateBaselineTask is a separate task type from Detekt - without this,
        // the baseline-generation task ignores the excludes above and sweeps generated
        // Compose resource-accessor code into the baseline as noise.
        tasks.withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>().configureEach {
            exclude("**/build/**", "**/generated/**")
        }
        // The detekt Gradle plugin doesn't auto-wire the aggregate `detekt` task
        // to any source set in Kotlin Multiplatform library modules - without
        // this, `detekt` silently reports NO-SOURCE and skips the module's
        // commonMain entirely (only composeApp, an androidApplication module,
        // happened to get this wiring for free). `tasks.matching` degrades to a
        // no-op where the task doesn't exist (e.g. the plain-JVM detekt-rules
        // module), so this is safe to apply blanket across all subprojects.
        tasks.matching { it.name == "detekt" }.configureEach {
            dependsOn(tasks.matching { it.name == "detektMetadataCommonMain" })
        }
    }
}
