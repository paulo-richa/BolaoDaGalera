import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
    alias(libs.plugins.firebasePerf)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.roborazzi)
    id("com.google.firebase.appdistribution")
    kotlin("native.cocoapods")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Bolão da Galera Shared Module"
        homepage = "https://github.com/lpstudio"
        version = "3.2.3"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "ComposeApp"
            isStatic = true
        }
        pod("FirebaseAuth") {
            linkOnly = true
        }
        pod("FirebaseFirestore") {
            linkOnly = true
        }
        pod("FirebaseRemoteConfig") {
            linkOnly = true
        }
        pod("Google-Mobile-Ads-SDK") {
            moduleName = "GoogleMobileAds"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.splashscreen)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.play.services.ads)
            implementation("com.google.firebase:firebase-messaging")

            // UI Testing
            // Note: In KMP, some people use commonTest, but for Compose Android is easiest
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(project(":designsystem"))
            implementation(project(":core-common"))
            implementation(project(":core-data"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            // implementation(libs.androidx.lifecycle.viewmodelCompose)
            // implementation(libs.androidx.lifecycle.runtimeCompose)
            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            // Logging
            implementation(libs.kermit)
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            // Serialization
            implementation(libs.kotlinx.serialization.json)
            // Navigation
            implementation(libs.navigation.compose)
            // DateTime
            implementation(libs.kotlinx.datetime)
            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
            // Coil
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.coil.svg)
            implementation(libs.compose.material.icons.core)
            implementation(libs.compose.material.icons.extended)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "com.lpstudio.bolaodagalera"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.lpstudio.bolaodagalera"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 27
        versionName = "3.2.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            firebaseAppDistribution {
                appId = "1:254672592094:android:432e51c0bcc8e75a92f64f"
                artifactType = "APK"
                testers = "paulo.richa@hotmail.com"
                releaseNotes = "v3.2.3 (Build 27): AdMob Android de produção e melhorias de validação."
            }
        }
        getByName("debug") {
            firebaseAppDistribution {
                appId = "1:254672592094:android:432e51c0bcc8e75a92f64f"
                artifactType = "APK"
                testers = "paulo.richa@hotmail.com"
                releaseNotes = "v3.2.3 (Build 27): AdMob Android de produção e melhorias de validação."
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    lint {
        baseline = file("lint-baseline.xml")
        checkDependencies = true
        abortOnError = true
        ignoreWarnings = false
        showAll = true
        explainIssues = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    // Provide Firebase BOM at module level so platform versions are available
    // to the GitLive KMP artifacts which rely on platform-specific Android
    // Firebase artifacts without explicit versions.
    implementation(platform("com.google.firebase:firebase-bom:34.18.0"))

    debugImplementation(libs.compose.uiTooling)
    debugImplementation(libs.compose.ui.test.manifest)
    androidTestImplementation(libs.compose.ui.test.junit4)

    // Unit Tests
    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.compose.ui.test.manifest)

    // Roborazzi for Snapshot Testing
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit)

    detektPlugins(project(":detekt-rules"))
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(file("../config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
}

// Regra isolada do design system (sem string hardcoded em componente Bolao*).
// Task separada da principal pra não misturar com o restante das regras de
// estilo/complexidade - roda só sobre commonMain, onde vivem as telas.
val detektDesignSystem by tasks.registering(Detekt::class) {
    description = "Verifica strings hardcoded em componentes Bolao* (regra do design system)."
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
