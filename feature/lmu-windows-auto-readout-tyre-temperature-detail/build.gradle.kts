plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.lmuWindowsDomain)
            implementation(projects.core.designsystem)
            implementation(libs.compose.material.icons.extended)
        }
        jvmTest.dependencies {
            implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.mockk)
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources"
}
