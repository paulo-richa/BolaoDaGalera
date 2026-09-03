import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

composeCompiler {
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("config/compose/stability_config.conf"))
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // api (not implementation): public design-system functions expose types
            // from these packages in their own signatures (e.g. FabPosition in
            // BolaoScaffold, ButtonColors in BolaoButton). If declared as
            // implementation, consumer modules that don't declare compose.material3
            // directly (e.g. :feature-auth, intentionally, to prevent importing raw
            // material3) would compile without error but generate calls with an
            // incorrect ABI (mangled name mismatch), failing at runtime with
            // NoSuchMethodError.
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.material3)
            api(libs.compose.ui)
            api(libs.compose.material.icons.core)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.components.resources)
        }
    }
}

android {
    namespace = "com.lpstudio.bolaodagalera.designsystem"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        baseline = file("lint-baseline.xml")
        checkDependencies = true
        abortOnError = true
        ignoreWarnings = false
        showAll = true
        explainIssues = true
    }
}

dependencies {
    detektPlugins(project(":detekt-rules"))
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(file("../config/detekt/detekt.yml"))
    baseline = file("detekt-baseline.xml")
    buildUponDefaultConfig = true
    allRules = false
}

// Isolated design-system rule (no hardcoded strings in Bolao* components).
val detektDesignSystem by tasks.registering(Detekt::class) {
    description = "Checks for hardcoded strings in Bolao* design-system components."
    setSource(files("src/commonMain/kotlin"))
    config.setFrom(file("../config/detekt/detekt-design-system.yml"))
    buildUponDefaultConfig = false
    include("**/*.kt")
    exclude("**/build/**")
}

tasks.named("check") {
    dependsOn(detektDesignSystem)
}

ktlint {
    android.set(true)
    ignoreFailures.set(false)
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
    }
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}
