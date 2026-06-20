plugins {
    id("feature-compose-screenshot")
}

kotlin {
    androidLibrary {
        namespace = "kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.designsystem)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.koin.compose.viewmodel)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

compose.resources {
    packageOfResClass = "kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources"
}
