plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.manualreadoutlist"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(libs.compose.material.icons.extended)
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.manualreadoutlist.generated.resources"
}
