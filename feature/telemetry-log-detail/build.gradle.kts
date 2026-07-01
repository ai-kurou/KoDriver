plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.telemetrylogdetail"
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(libs.compose.foundation)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutinesTest)
        }
        jvmTest.dependencies {
            implementation(libs.compose.uiTest)
            implementation(libs.compose.uiTestJunit4)
            implementation(libs.kotlin.testJunit)
            implementation(compose.desktop.currentOs)
        }
    }
}

compose.resources {
    packageOfResClass = "kodriver.feature.telemetrylogdetail.generated.resources"
}
