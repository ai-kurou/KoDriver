plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.lmuwindowsdebugstatedetail"
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(projects.core.domain)
            implementation(projects.core.lmuWindowsDomain)
            implementation(libs.reorderable)
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources"
}
