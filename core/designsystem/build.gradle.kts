plugins {
    alias(libs.plugins.luximmo.android.library)
    alias(libs.plugins.luximmo.android.compose)
}

android {
    namespace = "com.example.core.designsystem"
}

dependencies {
    api(libs.androidx.activity.compose)
    api(libs.androidx.compose.material.iconsExtended)
}
