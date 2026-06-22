plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.gt7ps5readout.mybestlapdetail"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.designsystem)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.koin.compose.viewmodel)
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
    packageOfResClass = "kodriver.feature.gt7ps5readout.mybestlapdetail.generated.resources"
}
