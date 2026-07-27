plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.acewindowsreadout.remainingfueldetail"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
        }
    }
}

compose.resources {
    packageOfResClass = "kodriver.feature.acewindowsreadout.remainingfueldetail.generated.resources"
}
