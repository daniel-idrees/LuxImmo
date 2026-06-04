# Build Logic — Template

Fill-in skeleton. Replace `<conv>` with your convention-plugin prefix and `<org>` / `<app>` / `<name>` with your project's values.

## A feature module's `build.gradle.kts`

This stays tiny because the `<conv>.android.feature` convention plugin supplies the common dependencies.

```kotlin
plugins {
    alias(libs.plugins.<conv>.android.feature)
    alias(libs.plugins.<conv>.android.compose)
    alias(libs.plugins.kotlin.serialization) // only if the feature serializes nav keys
}

android {
    namespace = "<org>.<app>.feature.<name>"
}

dependencies {
    testImplementation(project(":core:testing"))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
```

## Single-module app `build.gradle.kts`

A single-module project has no convention plugins — the one module applies its plugins directly:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.app"
    // compileSdk, defaultConfig, buildFeatures { compose = true }, Java/Kotlin target …
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    // Compose, Hilt, Room, Retrofit, Coroutines, Coil, Navigation 3 …
    testImplementation(libs.junit)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
```
