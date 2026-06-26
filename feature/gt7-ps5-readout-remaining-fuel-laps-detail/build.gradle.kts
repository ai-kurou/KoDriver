plugins {
    id("feature-compose")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
        }
    }
}
