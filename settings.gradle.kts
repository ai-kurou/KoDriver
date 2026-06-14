rootProject.name = "KoDriver"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":app:androidApp")
include(":app:desktopApp")
include(":app:shared")
include(":app:webApp")
include(":core:domain")
include(":core:data")
include(":core:designsystem")
include(":feature:lmu-connection")
include(":feature:lmu-narrator")
include(":feature:other-license-detail")
include(":feature:other-list")
include(":feature:other-volume-detail")
include(":feature:readout-list")
include(":feature:lmu-readout-vehicle-approach-detail")
include(":feature:lmu-readout-flag-detail")
include(":feature:lmu-readout-vehicle-damage-detail")
include(":server")
