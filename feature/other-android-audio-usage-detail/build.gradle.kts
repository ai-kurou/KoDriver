plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.otherandroidaudiousagedetail"
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        jvmTest.dependencies {
            implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutinesTest)
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
                implementation(libs.compose.uiTest)
                implementation(libs.compose.uiTestJunit4)
                implementation(libs.robolectric)
                implementation(libs.roborazzi.compose)
                implementation(libs.roborazzi.core)
            }
        }
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(libs.compose.material.icons.extended)
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.otherandroidaudiousagedetail.generated.resources"
}
