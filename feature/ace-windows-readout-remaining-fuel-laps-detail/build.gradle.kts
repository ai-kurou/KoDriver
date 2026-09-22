plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail.generated.resources"
}
