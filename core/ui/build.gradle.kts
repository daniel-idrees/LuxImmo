import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.core.ui"
    compileSdk {
        version = release(36)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    api(project(":core:designsystem"))

    //navigation 3
    api(libs.androidx.navigation3.runtime)
    api(libs.androidx.navigation3.ui)

    api(libs.androidx.compose.material3.adaptive)
    api(libs.androidx.compose.material3.adaptive.layout)
    api(libs.androidx.compose.material3.adaptive.navigation)
    api(libs.androidx.compose.material3.adaptive.navigation3)

    //coil
    api(libs.coil3.compose)
    api(libs.coil3.coil.network.okhttp)

    //hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}