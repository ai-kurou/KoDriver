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
    packageOfResClass = "kodriver.feature.otherserveripdetail.generated.resources"
}
