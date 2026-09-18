plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.narratoroverlay"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(projects.core.domain)
            implementation(libs.koin.compose)
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
            implementation(libs.kotlinx.coroutinesTest)
        }
    }
}
