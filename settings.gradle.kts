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

rootProject.name = "NativePluginSample"
include(":sampleNativeLibKt")
include(":sampleNativeLibJv")
include(":sampleLibraryKt")
include(":sampleLibraryJv")
include(":BluetoothLELib")
include(":AndroidDevelopersDocLib")
include(":UnitySendMassageTest")
