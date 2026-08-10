@file:Suppress("UnstableApiUsage")

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

rootProject.name = "Anoti"
include(":app")
include(":main")
include(":core-kmp:celebrity")
include(":core-kmp:anime-database")
include(":core-kmp:di")
include(":core-kmp:network")
include(":core-kmp:test-utils")
include(":feature-kmp:bottom-navigation-bar")
include(":feature-kmp:anime-base")
include(":feature-kmp:anime-list")
include(":feature-kmp:anime-favorites")
include(":feature-kmp:anime-background-update")
include(":feature-kmp:anime-notification")
include(":feature-kmp:anime-notification-external")
