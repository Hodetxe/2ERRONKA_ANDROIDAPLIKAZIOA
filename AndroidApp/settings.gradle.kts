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

rootProject.name = "AdroidApp"
include(":app")

val localAppData = providers.environmentVariable("LOCALAPPDATA").orNull
val buildRoot = java.io.File(localAppData ?: System.getProperty("java.io.tmpdir"), "AndroidAppGradleBuild")

gradle.beforeProject {
    buildDir = java.io.File(buildRoot, path.replace(":", "_"))
}
