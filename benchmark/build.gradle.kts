import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.test")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "com.example.benchmark"

    defaultConfig {
        minSdk = 24
        compileSdk = 36

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Suppress errors when running on an emulator. Results from an emulator are not
        // representative of real-world performance, but are fine for local verification.
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // The :app module this benchmark/profile-generation test runs against.
    targetProjectPath = ":app"
    // Run the instrumentation in the same process as the target app so Macrobenchmark can
    // launch and measure it.
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

// Configuration for the Baseline Profile producer. The androidx.baselineprofile plugin
// creates the required `benchmarkRelease` / `nonMinifiedRelease` variants automatically and
// wires the generated profile back into the consumer (:app).
baselineProfile {
    // Use a connected device/emulator to generate the profile. Switch to a Gradle Managed
    // Device by configuring `managedDevices` if you want fully reproducible CI runs.
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}
