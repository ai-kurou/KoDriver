plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(projects.core.domain)
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
    packageOfResClass = "kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources"
}
