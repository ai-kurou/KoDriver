plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
        }
    }
}

compose.resources {
    packageOfResClass = "kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources"
}
