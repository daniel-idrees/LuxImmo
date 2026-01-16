pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "LuxImmo"
include(":app")
include(":core")
include(":core:data")
include(":core:database")
include(":core:domain")
include(":core:designsystem")
include(":core:network")
include(":core:ui")
include(":feature")
include(":feature:listings")
