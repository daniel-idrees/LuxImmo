import java.util.Properties

plugins {
    alias(libs.plugins.luximmo.android.library)
    alias(libs.plugins.luximmo.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

val baseUrlKey = "BASE_URL"

val baseUrl: String = System.getenv(baseUrlKey) ?: Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use(::load)
}.getProperty(baseUrlKey).orEmpty()

android {
    namespace = "com.example.luximmo.core.network"
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        buildConfigField("String", baseUrlKey, "\"${baseUrl}\"")
    }
}

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.kotlinx.serialization.json)

    //okhttp
    implementation(libs.okhttp.logging)

    //retrofit
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
}
