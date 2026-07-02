plugins {
    id("feature-compose-screenshot")
    `java-test-fixtures`
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
            implementation(projects.core.designsystem)
            implementation(projects.core.domain)
            implementation(libs.compose.material3)
            implementation(libs.compose.material3.adaptive.layout)
            implementation(libs.compose.material3.adaptive.navigation)
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
    packageOfResClass = "kodriver.feature.telemetryloglist.generated.resources"
}

dependencies {
    testFixturesImplementation(projects.core.domain)
    testFixturesImplementation(libs.koin.core)
    testFixturesImplementation(libs.kotlinx.coroutinesCore)
}
