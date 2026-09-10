import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
    alias(libs.plugins.firebasePerf)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.androidxBaselineProfile)
    alias(libs.plugins.playPublisher) apply false
    alias(libs.plugins.firebaseAppDistribution)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
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
    buildFeatures {
        compose = true
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
}

baselineProfile {
    // Generation only needs one representative variant, not the full build matrix.
    automaticGenerationDuringBuild = false
}

dependencies {
    baselineProfile(project(":baselineprofile"))

    implementation(project(":composeApp"))
    // Entry-point classes (BolaoApplication/MainActivity/BolaoFirebaseMessagingService)
    // reference these directly (CrashlyticsLogWriter, AuthRepository/NotificationRepository,
    // CrashReporter) rather than only through composeApp's public API.
    implementation(project(":core-common"))
    implementation(project(":core-data"))

    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.koin.android)
    implementation(libs.koin.core)
    implementation(libs.play.services.ads)
    implementation("com.google.firebase:firebase-messaging")
    implementation(libs.androidx.profileinstaller)
    implementation(libs.kermit)
    implementation(libs.kotlinx.coroutines.core)

    coreLibraryDesugaring(libs.android.desugar.jdk.libs)
    // Provide Firebase BOM at module level so platform versions are available
    // to the GitLive KMP artifacts which rely on platform-specific Android
    // Firebase artifacts without explicit versions.
    implementation(platform(libs.firebase.bom))

    debugImplementation(libs.compose.uiTooling)
    debugImplementation(libs.compose.ui.test.manifest)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(project(":core-testing"))
    // BolaoDetailUiTest/TestApp reference BolaoDetailScreen/BolaoViewModel directly - composeApp
    // only exposes feature-bolao as `implementation` (not transitively), so androidApp's own
    // dependency on composeApp above doesn't make these visible to its test sources.
    androidTestImplementation(project(":feature-bolao"))

    detektPlugins(project(":detekt-rules"))
}

// Publishing to the Play Store's closed testing track ("Teste fechado - Alpha"),
// driven by promote-play-store.yml on a release/x.y.z branch. The plugin is only
// applied when PLAY_SERVICE_ACCOUNT_PATH is set - Gradle Play Publisher wires a
// versionCode-processing task into every release-variant build (not just publish
// tasks) once applied, which would otherwise force assembleRelease (used by
// release-candidate.yml/release.yml for Firebase App Distribution, unrelated to
// the Play Store) to require Play Console credentials too.
val playServiceAccountPath = System.getenv("PLAY_SERVICE_ACCOUNT_PATH")
if (playServiceAccountPath != null) {
    apply(plugin = "com.github.triplet.play")
    configure<com.github.triplet.gradle.play.PlayPublisherExtension> {
        serviceAccountCredentials.set(file(playServiceAccountPath))
        track.set(System.getenv("PLAY_TRACK") ?: "alpha")
        defaultToAppBundles.set(true)
        resolutionStrategy.set(com.github.triplet.gradle.androidpublisher.ResolutionStrategy.AUTO)
        // versionCode conflicts are resolved automatically (see resolutionStrategy above) by
        // querying the Play Console for the current max and incrementing - the versionCode in
        // defaultConfig above is irrelevant for this task, only for APK builds.
    }
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(file("../config/detekt/detekt.yml"), file("../config/detekt/detekt-code-conventions.yml"))
    buildUponDefaultConfig = true
    allRules = false
}

// Isolated design-system ruleset (no hardcoded strings in Bolao* components) - kept for
// parity with composeApp's own task wiring below, even though androidApp's own source has
// no Bolao* components (this module is a thin shell); `setSource` on an empty/nonexistent
// directory is a safe no-op, and keeping the task means `check` behaves identically to
// before the split if entry-point code ever grows Bolao* components.
val detektDesignSystem by tasks.registering(Detekt::class) {
    description = "Checks for hardcoded strings in Bolao* components (design-system rule)."
    setSource(files("src/main/kotlin"))
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
