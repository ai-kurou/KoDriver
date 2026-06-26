plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.otherconsoleipdetail"
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(libs.compose.material.icons.extended)
        }
        named("androidHostTest") {
            dependencies {
                implementation(libs.kotlin.testJunit)
                implementation(libs.junit)
                implementation(libs.roborazzi.compose)
                implementation(libs.robolectric)
                implementation(libs.roborazzi.core)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "kodriver.feature.otherconsoleipdetail.generated.resources"
}
