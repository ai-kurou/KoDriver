plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
        }
    }
}

compose.resources {
    packageOfResClass = "kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources"
}
