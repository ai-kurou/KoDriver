plugins {
    id("feature-compose")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.speechrecognitionlist"
        withHostTest {
            isIncludeAndroidResources = true
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.speechrecognitionlist.generated.resources"
}
