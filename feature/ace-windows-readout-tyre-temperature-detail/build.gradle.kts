plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(projects.core.domain)
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources"
}
