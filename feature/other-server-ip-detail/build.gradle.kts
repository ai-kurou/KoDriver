plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.otherserveripdetail"
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(libs.compose.material.icons.extended)
        }
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.sentry.bom))
            implementation(libs.sentry)
        }
        jvmMain.dependencies {
            implementation(projects.core.domain)
            implementation(libs.jmdns)
            implementation(project.dependencies.platform(libs.sentry.bom))
            implementation(libs.sentry)
        }
        jvmTest.dependencies {
            implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.mockk)
        }
        named("androidHostTest") {
            dependencies {
                implementation(libs.compose.uiTest)
                implementation(libs.compose.uiTestJunit4)
                implementation(libs.androidx.activity.compose)
                implementation(libs.mockk)
                implementation(libs.robolectric)
                implementation(libs.kotlin.testJunit)
                implementation(libs.junit)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.otherserveripdetail.generated.resources"
}
