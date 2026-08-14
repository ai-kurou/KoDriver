plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.othervolumedetail"
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
        }
        named("androidHostTest") {
            kotlin.srcDir(
                rootProject.layout.projectDirectory.dir(
                    "build-logic/src/featureComposeScreenshotAndroidHostTest/kotlin",
                ),
            )
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
    packageOfResClass = "kurou.kodriver.feature.othervolumedetail.generated.resources"
}
