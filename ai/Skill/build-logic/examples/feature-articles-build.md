# Example — a feature module's `build.gradle.kts`

A generic, filled-in version of the feature build file (skeleton in this skill's `template.md`). Here the convention-plugin prefix is `myapp` and the feature is `articles`.

```kotlin
// feature/articles/build.gradle.kts
plugins {
    alias(libs.plugins.myapp.android.feature)
    alias(libs.plugins.myapp.android.compose)
    alias(libs.plugins.kotlin.serialization) // serializes nav keys
}

android {
    namespace = "com.example.myapp.feature.articles"
}

dependencies {
    testImplementation(project(":core:testing"))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
```

The file stays this small because `myapp.android.feature` already wires `:core:domain`, `:core:ui`, `:core:common`, lifecycle, and hilt-navigation-compose.

## A matching version-catalog excerpt (`gradle/libs.versions.toml`)

Every alias the build file above references is declared here.

```toml
[versions]
kotlin = "2.0.21"
composeBom = "2024.09.00"

[libraries]
androidx-junit = { group = "androidx.test.ext", name = "junit", version = "1.2.1" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }

[plugins]
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
# project convention plugins, referenced as alias(libs.plugins.myapp.android.feature)
myapp-android-feature = { id = "myapp.android.feature", version = "unspecified" }
myapp-android-compose = { id = "myapp.android.compose", version = "unspecified" }
```

The convention plugin that backs `myapp.android.feature` — and how it's registered — is shown in [`android-feature-convention-plugin.md`](android-feature-convention-plugin.md).

## Common violation and its fix

The version-catalog guardrail: a version pinned in a module build file instead of the catalog.

```kotlin
// ❌ Violation — hardcoded version in feature/articles/build.gradle.kts
implementation("com.squareup.retrofit2:retrofit:2.11.0")

// ✅ Fix — add it to the catalog, reference by alias
implementation(libs.retrofit)
```
