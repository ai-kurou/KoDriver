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
            implementation(projects.core.aceWindowsDomain)
            implementation(projects.core.lmuWindowsDomain)
            implementation(projects.core.gt7Ps5Domain)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.material3.adaptive.layout)
            implementation(libs.compose.material3.adaptive.navigation)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        jvmTest.dependencies {
            implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.compose.uiTest)
            implementation(libs.compose.uiTestJunit4)
            implementation(libs.kotlin.testJunit)
            implementation(compose.desktop.currentOs)
            implementation(libs.mockk)
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.telemetryloglist.generated.resources"
}

dependencies {
    testFixturesApi(projects.core.domain)
    testFixturesImplementation(platform(libs.koin.bom))
    testFixturesImplementation(libs.koin.core)
    testFixturesImplementation(platform(libs.kotlinx.coroutines.bom))
    testFixturesImplementation(libs.kotlinx.coroutinesCore)
}
