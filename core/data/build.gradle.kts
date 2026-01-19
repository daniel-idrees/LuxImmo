plugins {
    alias(libs.plugins.luximmo.android.library)
    alias(libs.plugins.luximmo.android.hilt)
}

android {
    namespace = "com.example.luximmo.core.data"
    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:common"))

    implementation(libs.kotlinx.coroutines.core)

    //testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation (libs.mockito.kotlin)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
}
