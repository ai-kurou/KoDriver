plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources"
}

// Pane/ViewModelとそのテストは別PRで追加するため、jvmTestにテストが存在しないことによる失敗を一時的に許容する。
tasks.withType<Test>().configureEach {
    failOnNoDiscoveredTests = false
}
