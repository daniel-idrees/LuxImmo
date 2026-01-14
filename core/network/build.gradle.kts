import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

val baseUrlKey = "BASE_URL"

val baseUrl: String = System.getenv(baseUrlKey) ?: Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use(::load)
}.getProperty(baseUrlKey).orEmpty()

android {
    namespace = "com.example.luximmo.core.network.data"
    buildFeatures {
        buildConfig = true
    }
    compileSdk {
        version = release(36)
    }
    defaultConfig {
        buildConfigField("String", baseUrlKey, "\"${baseUrl}\"")
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
    //hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.kotlinx.serialization.json)

    //okhttp
    implementation(libs.okhttp.logging)

    //retrofit
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
}