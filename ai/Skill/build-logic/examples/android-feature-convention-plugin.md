# Example — authoring the `myapp.android.feature` convention plugin

The consumer side (a feature module applying the plugin) is in [`feature-articles-build.md`](feature-articles-build.md). This is the **authoring** side: the plugin source that lives in the `build-logic/convention` included build, plus how it's registered so modules can apply it by id.

## 1. The version-catalog accessor

Plugins can't use the generated `libs` accessor, so add a tiny extension to read the catalog by name.

```kotlin
// build-logic/convention/src/main/kotlin/Extensions.kt
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")
```

## 2. The shared Kotlin/Android config helper

Centralize `compileSdk`, `minSdk`, Java compatibility, and the Kotlin `jvmTarget` here so every plugin sets them once.

```kotlin
// build-logic/convention/src/main/kotlin/KotlinAndroid.kt
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

// The exact generic arity of CommonExtension depends on your AGP version.
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        compileSdk = 35
        defaultConfig { minSdk = 24 }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }
    configure<KotlinAndroidProjectExtension> {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
}
```

## 3. The feature convention plugin

`feature` composes `library` + `hilt`, then wires the dependencies every feature needs — so feature `build.gradle.kts` files stay nearly empty.

```kotlin
// build-logic/convention/src/main/kotlin/AndroidFeatureConventionPlugin.kt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // Reuse the other convention plugins rather than repeating their config.
        pluginManager.apply("myapp.android.library")
        pluginManager.apply("myapp.android.hilt")

        dependencies {
            add("implementation", project(":core:domain"))
            add("implementation", project(":core:ui"))
            add("implementation", project(":core:common"))
            add("implementation", libs.findLibrary("androidx-lifecycle-runtimeCompose").get())
            add("implementation", libs.findLibrary("androidx-hilt-navigation-compose").get())
        }
    }
}
```

## 4. Registering the plugins

The included build declares each plugin id and its implementing class. The `id`s here are exactly what the version catalog's `[plugins]` block points at (`id = "myapp.android.feature"`), which is how `alias(libs.plugins.myapp.android.feature)` resolves.

```kotlin
// build-logic/convention/build.gradle.kts
plugins {
    `kotlin-dsl`
}

dependencies {
    // Lets the plugins above call android { } / Kotlin DSL APIs.
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidFeature") {
            id = "myapp.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidCompose") {
            id = "myapp.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        // …android.application, android.library, android.hilt, android.room
    }
}
```

The `library`, `hilt`, `compose`, etc. plugins follow the same shape: apply the underlying Gradle/AGP plugins, call `configureKotlinAndroid(...)`, and add their dependencies via `libs.findLibrary(...)`.
