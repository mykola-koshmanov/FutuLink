plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.futulink.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.futulink.android"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // The remote config endpoint is public, not a secret. It lives here so that the
        // single source of truth for both build types is the build script.
        buildConfigField(
            "String",
            "REMOTE_CONFIG_URL",
            "\"https://gist.githubusercontent.com/mykola-koshmanov/1cdf8d6a522ab06d32e48dcc42e5bb6a/raw/config.json\""
        )

        // Hetzner's public speed-test file: exactly 104,857,600 bytes (100 MiB) of random,
        // incompressible data served over HTTPS as application/octet-stream, with no redirect
        // and no cookies. Cloudflare's __down endpoint could not be used because it answers
        // any request of 100,000,000 bytes or more with HTTP 403. The measurement repeats the
        // request when a body ends before the 10 s window, so the body size only affects how
        // often a new request has to be started.
        buildConfigField(
            "String",
            "SPEED_TEST_URL",
            "\"https://fsn1-speed.hetzner.com/100MB.bin\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
