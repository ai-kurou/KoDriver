plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.otherwakewordhotkeydetail"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.otherwakewordhotkeydetail.generated.resources"
}
