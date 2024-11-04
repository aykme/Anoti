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
include(":ui-core:theme")
include(":ui-core:atom")
include(":core-kmp:celebrity")
include(":core-platform:celebrity")
include(":core-kmp:database")
include(":core-platform:database")
include(":core-kmp:network")
include(":core-platform:network")
include(":feature-kmp:bottom-navigation-bar")
include(":feature-kmp:anime-base")
include(":feature-platform:anime-base")
include(":feature-kmp:anime-list")
include(":feature-platform:anime-list")
include(":feature-kmp:anime-favorites")
include(":feature-platform:anime-favorites")
include(":feature-platform:anime-background-update")
