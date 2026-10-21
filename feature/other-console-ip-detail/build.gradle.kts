plugins {
    id("feature-compose-screenshot")
    `java-test-fixtures`
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
            implementation(projects.core.gt7Ps5Domain)
            implementation(projects.core.designsystem)
            implementation(libs.compose.material.icons.extended)
        }
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.sentry.bom))
            implementation(libs.sentry)
        }
        jvmMain.dependencies {
            implementation(project.dependencies.platform(libs.sentry.bom))
            implementation(libs.sentry)
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
            implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutinesTest)
        }
        named("androidHostTest") {
            kotlin.srcDir(
                rootProject.layout.projectDirectory.dir(
                    "build-logic/src/featureComposeScreenshotAndroidHostTest/kotlin",
                ),
            )
            dependencies {
                implementation(libs.compose.uiTest)
                implementation(libs.compose.uiTestJunit4)
                implementation(libs.androidx.activity.compose)
                implementation(libs.mockk)
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
    packageOfResClass = "kurou.kodriver.feature.otherconsoleipdetail.generated.resources"
}

dependencies {
    testFixturesApi(projects.core.domain)
    testFixturesApi(projects.core.gt7Ps5Domain)
    testFixturesImplementation(platform(libs.koin.bom))
    testFixturesImplementation(libs.koin.core)
    testFixturesImplementation(platform(libs.kotlinx.coroutines.bom))
    testFixturesImplementation(libs.kotlinx.coroutinesCore)
}
