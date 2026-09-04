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
    alias(libs.plugins.kover)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.androidxBaselineProfile)
    alias(libs.plugins.playPublisher)
    id("com.google.firebase.appdistribution")
    kotlin("native.cocoapods")
}

composeCompiler {
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("config/compose/stability_config.conf"))
    // Official way to find out what's actually unstable before adding anything to the
    // stability config above, instead of guessing - see reports at
    // composeApp/build/compose_metrics after any build.
    metricsDestination.set(layout.buildDirectory.dir("compose_metrics"))
    reportsDestination.set(layout.buildDirectory.dir("compose_metrics"))
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
            implementation(libs.androidx.profileinstaller)

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
            implementation(project(":feature-auth"))
            implementation(project(":feature-bolao"))
            implementation(project(":feature-core"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
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
            // Ktor (HTTP client engine required by Coil's network image loader; no direct
            // Ktor usage in app code - all data access goes through the Firebase SDK)
            implementation(libs.ktor.client.core)
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
            implementation(libs.turbine)
            implementation(project(":core-testing"))
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
        // Overridden by promote-play-store.yml via -PversionNameOverride, derived from the
        // release/x.y.z branch name - the versionCode above only matters for APK builds
        // (debug/Firebase App Distribution); the Play Store's versionCode is auto-assigned
        // by the play { resolutionStrategy AUTO } block below at publish time.
        versionName = (project.findProperty("versionNameOverride") as String?) ?: "3.2.3"
        testInstrumentationRunner = "com.lpstudio.bolaodagalera.CustomTestRunner"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    // Populated from GitHub Actions secrets in CI (ANDROID_KEYSTORE_PATH points at the
    // keystore decoded from a base64 secret). Left unset for local builds, which stay
    // unsigned/debug-signed as before - no local.properties or checked-in keystore needed.
    val releaseKeystorePath = System.getenv("ANDROID_KEYSTORE_PATH")
    val firebaseServiceAccountPath = System.getenv("FIREBASE_SERVICE_ACCOUNT_PATH")
    // Falls back to a local Gradle property (set in ~/.gradle/gradle.properties, never
    // committed) so developers can still distribute test builds locally without an
    // email hardcoded in this public repo.
    val appDistributionTesters =
        System.getenv("FIREBASE_APP_DISTRIBUTION_TESTERS")
            ?: (project.findProperty("firebaseAppDistributionTesters") as String?)
            ?: ""
    signingConfigs {
        if (releaseKeystorePath != null) {
            create("release") {
                storeFile = file(releaseKeystorePath)
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
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
            if (releaseKeystorePath != null) {
                signingConfig = signingConfigs.getByName("release")
            }
            firebaseAppDistribution {
                appId = "1:254672592094:android:432e51c0bcc8e75a92f64f"
                artifactType = "APK"
                testers = appDistributionTesters
                releaseNotes = "v3.2.3 (Build 27): AdMob Android de produção e melhorias de validação."
                if (firebaseServiceAccountPath != null) {
                    serviceCredentialsFile = firebaseServiceAccountPath
                }
            }
        }
        getByName("debug") {
            firebaseAppDistribution {
                appId = "1:254672592094:android:432e51c0bcc8e75a92f64f"
                artifactType = "APK"
                testers = appDistributionTesters
                releaseNotes = "v3.2.3 (Build 27): AdMob Android de produção e melhorias de validação."
                if (firebaseServiceAccountPath != null) {
                    serviceCredentialsFile = firebaseServiceAccountPath
                }
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

baselineProfile {
    // Generation only needs one representative variant, not the full build matrix.
    automaticGenerationDuringBuild = false
}

dependencies {
    baselineProfile(project(":baselineprofile"))
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
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(project(":core-testing"))

    // Unit Tests
    testImplementation(libs.kotlin.test)
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

// Publishing to the Play Store's closed testing track ("Teste fechado - Alpha"),
// driven by promote-play-store.yml on a release/x.y.z branch. Left unconfigured
// (no credentials) for local/other builds - only that workflow sets these env vars.
val playServiceAccountPath = System.getenv("PLAY_SERVICE_ACCOUNT_PATH")
play {
    serviceAccountCredentials.set(file(playServiceAccountPath ?: "play-service-account-not-set.json"))
    track.set(System.getenv("PLAY_TRACK") ?: "alpha")
    defaultToAppBundles.set(true)
    resolutionStrategy.set(com.github.triplet.gradle.androidpublisher.ResolutionStrategy.AUTO)
    // versionCode conflicts are resolved automatically (see resolutionStrategy above) by
    // querying the Play Console for the current max and incrementing - the versionCode in
    // defaultConfig above is irrelevant for this task, only for APK builds.
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(file("../config/detekt/detekt.yml"))
    baseline = file("detekt-baseline.xml")
    buildUponDefaultConfig = true
    allRules = false
}

// Isolated design-system ruleset (no hardcoded strings in Bolao* components).
// Kept separate from the main task to avoid mixing with style/complexity
// rules - runs only over commonMain, where the screens live.
val detektDesignSystem by tasks.registering(Detekt::class) {
    description = "Checks for hardcoded strings in Bolao* components (design-system rule)."
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
