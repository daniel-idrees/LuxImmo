plugins {
    alias(libs.plugins.luximmo.android.feature)
    alias(libs.plugins.luximmo.android.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.luximmo.feature.listings"
    testOptions.unitTests.isIncludeAndroidResources = true
    testOptions.targetSdk = 36
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    //testing
    testImplementation(project(":core:testing"))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
