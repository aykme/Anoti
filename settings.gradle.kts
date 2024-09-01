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
include(":ui_core:theme")
include(":ui_core:atom")
include(":core_kmp:network")
include(":core_platform:network")
include(":feature_kmp:anime_network_base")
include(":feature_platform:anime_network_base")
include(":feature_kmp:anime_list")
include(":feature_platform:anime_list")