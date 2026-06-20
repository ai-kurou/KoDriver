plugins {
    id("feature-compose-screenshot")
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
    }
}

compose.resources {
    packageOfResClass = "kodriver.feature.otherlicensedetail.generated.resources"
}
