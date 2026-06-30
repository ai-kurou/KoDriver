rootProject.name = "KoDriver"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
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
include(":core:lmu-windows-data")
include(":core:gt7-ps5-data")
include(":core:designsystem")
include(":feature:lmu-windows-connection")
include(":feature:main")
include(":feature:server-connection")
include(":feature:lmu-windows-narrator")
include(":feature:other-license-detail")
include(":feature:other-list")
include(":feature:other-server-ip-detail")
include(":feature:other-console-ip-detail")
include(":feature:other-readout-start-sound-detail")
include(":feature:other-volume-detail")
include(":feature:other-keep-screen-on-detail")
include(":feature:readout-list")
include(":feature:lmu-windows-readout-vehicle-approach-detail")
include(":feature:lmu-windows-readout-flag-detail")
include(":feature:lmu-windows-readout-vehicle-damage-detail")
include(":feature:gt7-ps5-connection")
include(":feature:gt7-ps5-readout-my-bestlap-detail")
include(":feature:gt7-ps5-readout-remaining-fuel-laps-detail")
include(":feature:gt7-ps5-narrator")
include(":feature:telemetry-log-list")
include(":server")
