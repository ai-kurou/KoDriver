plugins {
    id("feature-compose-screenshot")
    `java-test-fixtures`
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.readoutlist"
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(libs.compose.material3.adaptive.layout)
            implementation(libs.compose.material3.adaptive.navigation)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.reorderable)
            implementation(libs.navigation3.ui)
        }
        jvmTest.dependencies {
            implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.mockk)
        }
        jvmTest {
            kotlin.srcDir(
                rootProject.layout.projectDirectory.dir("build-logic/src/paneDirectiveJvmTest/kotlin"),
            )
        }
        named("androidHostTest") {
            kotlin.srcDir(
                rootProject.layout.projectDirectory.dir(
                    "build-logic/src/featureComposeScreenshotAndroidHostTest/kotlin",
                ),
            )
            kotlin.srcDir(
                rootProject.layout.projectDirectory.dir(
                    "build-logic/src/paneDirectiveAndroidHostTest/kotlin",
                ),
            )
            dependencies {
                implementation(libs.kotlin.testJunit)
                implementation(libs.junit)
                implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
                implementation(libs.kotlinx.coroutinesTest)
                implementation(libs.compose.uiTestJunit4)
                implementation(libs.androidx.activity.compose)
                implementation(libs.roborazzi.compose)
                implementation(libs.compose.material3.adaptive.layout)
                implementation(libs.robolectric)
                implementation(libs.roborazzi.core)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.readoutlist.generated.resources"
}

dependencies {
    testFixturesImplementation(projects.core.domain)
    testFixturesImplementation(platform(libs.koin.bom))
    testFixturesImplementation(libs.koin.core)
    testFixturesImplementation(platform(libs.kotlinx.coroutines.bom))
    testFixturesImplementation(libs.kotlinx.coroutinesCore)
}
