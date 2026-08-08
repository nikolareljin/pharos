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
    // A module declaring its own repository is a module that can pull a
    // dependency nobody reviewed. Fail instead.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "pharos"

// Modules are split when they contain code, not to look organised.
// :core:model, :core:runtime and :core:protocol arrive with the runtime and the
// protocol.
include(":app")
