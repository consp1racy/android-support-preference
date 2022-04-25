pluginManagement {
    repositories {
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

include(":support-spinner")
include(":support-preference")
include(":support-preference-color")
include(":sample")

includeBuild("gradle/plugins/publish")
