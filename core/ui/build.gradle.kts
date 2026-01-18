plugins {
    alias(libs.plugins.luximmo.android.library)
    alias(libs.plugins.luximmo.android.compose)
    alias(libs.plugins.luximmo.android.hilt)
}

android {
    namespace = "com.example.core.ui"
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

    //splash
    implementation(libs.androidx.core.splashscreen)
}
