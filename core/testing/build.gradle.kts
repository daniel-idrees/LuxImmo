plugins {
    alias(libs.plugins.luximmo.android.library)
}

android {
    namespace = "com.example.luximmo.core.testing"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:domain"))

    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
    api (libs.mockito.kotlin)
    api(libs.mockito.core)
    api(libs.mockito.inline)
    api(kotlin("test"))
}