plugins {
    id("feature-compose-screenshot")
    alias(libs.plugins.aboutlibraries)
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.otherlicensedetail"
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(libs.aboutlibraries.compose.m3)
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
        }
        named("androidHostTest") {
            kotlin.srcDir(
                rootProject.layout.projectDirectory.dir(
                    "build-logic/src/featureComposeScreenshotAndroidHostTest/kotlin",
                ),
            )
            dependencies {
                implementation(libs.kotlin.testJunit)
                implementation(libs.junit)
                implementation(libs.roborazzi.compose)
                implementation(libs.robolectric)
                implementation(libs.roborazzi.core)
            }
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
