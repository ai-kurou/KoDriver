plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.acewindowsautoreadout.braketemperaturedetail"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.acewindowsautoreadout.braketemperaturedetail.generated.resources"
}
