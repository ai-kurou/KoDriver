plugins {
    id("feature-compose-screenshot")
    alias(libs.plugins.aboutlibraries)
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.otherlicensedetail"
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(libs.aboutlibraries.compose.m3)
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
    packageOfResClass = "kodriver.feature.otherlicensedetail.generated.resources"
}
