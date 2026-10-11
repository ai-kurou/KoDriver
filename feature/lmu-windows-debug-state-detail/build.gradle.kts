plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.lmuwindowsdebugstatedetail"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources"
}
