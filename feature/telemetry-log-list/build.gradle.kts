plugins {
    id("feature-compose")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.telemetryloglist"
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.material3)
            implementation(libs.compose.material3.adaptive.layout)
            implementation(libs.compose.material3.adaptive.navigation)
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
    packageOfResClass = "kodriver.feature.telemetryloglist.generated.resources"
}
