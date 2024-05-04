rootProject.name = "revanced-integrations"

pluginManagement {
    repositories {
        google()
        mavenCentral()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

buildCache {
    local {
        isEnabled = "CI" !in System.getenv()
    }
}

include(":app")
include(":stub")
