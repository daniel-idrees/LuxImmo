plugins {
    alias(libs.plugins.luximmo.android.feature)
    alias(libs.plugins.luximmo.android.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.luximmo.feature.listings"
    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    //testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation (libs.mockito.kotlin)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(kotlin("test"))
}
