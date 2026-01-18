import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.example.luximmo.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "luximmo.android.library"
            implementationClass = "com.example.convention.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "luximmo.android.compose"
            implementationClass = "com.example.convention.AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "luximmo.android.hilt"
            implementationClass = "com.example.convention.AndroidHiltConventionPlugin"
        }
        register("androidApplication") {
            id = "luximmo.android.application"
            implementationClass = "com.example.convention.AndroidApplicationConventionPlugin"
        }
        register("androidRoom") {
            id = "luximmo.android.room"
            implementationClass = "com.example.convention.AndroidRoomConventionPlugin"
        }
        register("androidFeature") {
            id = "luximmo.android.feature"
            implementationClass = "com.example.convention.AndroidFeatureConventionPlugin"
        }
    }
}


dependencies {
    compileOnly(libs.android.gradleApiPlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
}
