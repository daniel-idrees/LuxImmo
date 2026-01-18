plugins {
    alias(libs.plugins.luximmo.android.library)
}

android {
    namespace = "com.example.luximmo.core.domain"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
}
