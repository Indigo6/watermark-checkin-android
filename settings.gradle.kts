pluginManagement {
    repositories {
        google()
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

rootProject.name = "WatermarkCheckin"

include(":app")
include(":core:domain")
include(":core:record")
include(":location:common")
include(":location:manual")
include(":location:smart")
