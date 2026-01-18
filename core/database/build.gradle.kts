plugins {
    alias(libs.plugins.luximmo.android.library)
    alias(libs.plugins.luximmo.android.hilt)
    alias(libs.plugins.luximmo.android.room)
}

android {
    namespace = "com.example.luximmo.core.database"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
