plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(libs.compose.material.icons.extended)
        }
    }
}

compose.resources {
    packageOfResClass = "kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources"
}
