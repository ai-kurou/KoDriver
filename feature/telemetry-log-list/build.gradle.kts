plugins {
    id("feature-compose")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.telemetryloglist"
        withHostTest {
            isIncludeAndroidResources = true
        }
    }
}

compose.resources {
    packageOfResClass = "kodriver.feature.telemetryloglist.generated.resources"
}
