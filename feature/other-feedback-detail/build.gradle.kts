plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.otherfeedbackdetail"
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(projects.core.domain)
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
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.otherfeedbackdetail.generated.resources"
}
