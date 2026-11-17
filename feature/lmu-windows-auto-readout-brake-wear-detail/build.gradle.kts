plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.lmuWindowsDomain)
            implementation(projects.core.designsystem)
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail.generated.resources"
}
