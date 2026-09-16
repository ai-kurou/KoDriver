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
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
            implementation(libs.kotlinx.coroutinesTest)
        }
    }
}
