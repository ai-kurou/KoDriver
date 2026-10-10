plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.gt7ps5readout.remainingfueldetail"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(projects.core.domain)
        }
        jvmTest.dependencies {
            implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.mockk)
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.gt7ps5readout.remainingfueldetail.generated.resources"
}
