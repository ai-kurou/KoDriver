plugins {
    id("feature-compose-screenshot")
    alias(libs.plugins.aboutlibraries)
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.otherlicensedetail"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(libs.aboutlibraries.compose.m3)
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
        }
    }
}

// Gradle依存として解決されないVosk本体と同梱の日本語モデルのライセンスを、config/libraries のJSONで一覧に加える
aboutLibraries {
    collect {
        configPath.set(layout.projectDirectory.dir("config"))
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.otherlicensedetail.generated.resources"
}
