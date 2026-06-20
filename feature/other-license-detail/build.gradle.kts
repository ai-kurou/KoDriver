plugins {
    id("feature-compose")
    alias(libs.plugins.aboutlibraries)
}

kotlin {
    androidLibrary {
        namespace = "kurou.kodriver.feature.otherlicensedetail"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(libs.aboutlibraries.compose.m3)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmTest.dependencies {
            implementation(libs.compose.uiTest)
            implementation(libs.compose.uiTestJunit4)
            implementation(libs.kotlin.testJunit)
            implementation(compose.desktop.currentOs)
            implementation(libs.roborazzi.composeDesktop)
        }
    }
}

compose.resources {
    packageOfResClass = "kodriver.feature.otherlicensedetail.generated.resources"
}

apply(from = rootProject.file("gradle/roborazzi.gradle.kts"))
