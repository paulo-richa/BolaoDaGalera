import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.googleServices)
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
        version = "1.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "ComposeApp"
            isStatic = true
        }
        pod("FirebaseAuth")
        pod("FirebaseFirestore")
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.splashscreen)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
            
            // UI Testing
            // Note: In KMP, some people use commonTest, but for Compose Android is easiest
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            //implementation(libs.androidx.lifecycle.viewmodelCompose)
            //implementation(libs.androidx.lifecycle.runtimeCompose)
            // Firebase KMP (GitLive SDK)
            // The Firebase BOM will be provided at the module-level dependencies
            // block (outside the kotlin { } sourceSets) because using
            // platform(...) inside the Kotlin Multiplatform sourceSets can trigger
            // a Kotlin/Gradle DSL compilation issue. See below for the BOM.
            implementation(libs.firebase.auth)
            implementation(libs.firebase.firestore)
            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            //implementation(libs.koin.compose.viewmodel)
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
            // Material Icons (shorthand do plugin CMP)
            // The shorthand `compose.materialIconsExtended` is deprecated and
            // pinned to an old version; declare the artifact explicitly so the
            // build script doesn't treat the deprecation as an error.
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
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
        versionCode = 6
        versionName = "2.0"
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
                releaseNotes = "v2.0 (Build 6): Filtro dinâmico 'HOJE' para jogos, correção na lógica de pontos do ranking (alvo vs check), proteção contra regressão de placares (0x0) e navegação inteligente entre rodadas."
            }
        }
        getByName("debug") {
            firebaseAppDistribution {
                appId = "1:254672592094:android:432e51c0bcc8e75a92f64f"
                artifactType = "APK"
                testers = "paulo.richa@hotmail.com"
                releaseNotes = "v1.1: Deep Links de convite, saída de bolão, notificações pulsantes e correções na Home."
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Provide Firebase BOM at module level so platform versions are available
    // to the GitLive KMP artifacts which rely on platform-specific Android
    // Firebase artifacts without explicit versions.
    implementation(platform("com.google.firebase:firebase-bom:32.2.0"))

    debugImplementation(libs.compose.uiTooling)
    debugImplementation(libs.compose.ui.test.manifest)
    androidTestImplementation(libs.compose.ui.test.junit4)
}

