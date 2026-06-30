plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.telemetrylogdetail"
        withHostTest {
            isIncludeAndroidResources = true
        }
    }
}

compose.resources {
    packageOfResClass = "kodriver.feature.telemetrylogdetail.generated.resources"
}
