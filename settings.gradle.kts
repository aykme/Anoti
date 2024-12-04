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
include(":ui-core:res")
include(":ui-core:atom")
include(":core-kmp:celebrity")
include(":core-platform:celebrity")
include(":core-kmp:anime-database")
include(":core-platform:anime-database")
include(":core-platform:di")
include(":core-kmp:network")
include(":core-platform:network")
include(":feature-kmp:bottom-navigation-bar")
include(":feature-kmp:anime-base")
include(":feature-platform:anime-base")
include(":feature-kmp:anime-list")
include(":feature-platform:anime-list")
include(":feature-kmp:anime-favorites")
include(":feature-platform:anime-favorites")
include(":feature-kmp:anime-background-update")
include(":feature-platform:anime-background-update")
include(":feature-kmp:anime-notification")
include(":feature-platform:anime-notification")
include(":core-platform:navigation")
include(":feature-platform:anime-notification-external")
