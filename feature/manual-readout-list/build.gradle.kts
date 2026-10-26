plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.manualreadoutlist"
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.gt7Ps5Domain)
            implementation(projects.core.aceWindowsDomain)
            implementation(projects.core.lmuWindowsDomain)
            implementation(projects.core.designsystem)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.material3.adaptive.layout)
            implementation(libs.compose.material3.adaptive.navigation)
            implementation(libs.navigation3.ui)
        }
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
                implementation(libs.roborazzi.compose)
                implementation(libs.robolectric)
                implementation(libs.roborazzi.core)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.manualreadoutlist.generated.resources"
}
