plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.otherserveripdetail"
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(libs.compose.material.icons.extended)
        }
        jvmMain.dependencies {
            implementation(projects.core.domain)
            implementation(libs.jmdns)
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
        }
        named("androidHostTest") {
            dependencies {
                implementation(libs.kotlin.testJunit)
                implementation(libs.junit)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.otherserveripdetail.generated.resources"
}
