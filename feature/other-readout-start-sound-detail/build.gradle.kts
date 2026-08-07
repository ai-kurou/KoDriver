plugins {
    id("feature-compose-screenshot")
    `java-test-fixtures`
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.otherreadoutstartsounddetail"
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
        }
        jvmTest.dependencies {
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.mockk)
        }
        named("androidHostTest") {
            dependencies {
                implementation(libs.kotlin.testJunit)
                implementation(libs.junit)
                implementation(libs.compose.uiTest)
                implementation(libs.compose.uiTestJunit4)
                implementation(libs.roborazzi.compose)
                implementation(libs.robolectric)
                implementation(libs.roborazzi.core)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.otherreadoutstartsounddetail.generated.resources"
}

dependencies {
    testFixturesApi(projects.core.domain)
    testFixturesImplementation(libs.koin.core)
    testFixturesImplementation(libs.kotlinx.coroutinesCore)
}
