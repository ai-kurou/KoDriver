plugins {
    id("feature-compose")
}

private val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("compose-foundation").get())
            implementation(libs.findLibrary("compose-material3").get())
            implementation(libs.findLibrary("compose-uiToolingPreview").get())
        }
        jvmTest.dependencies {
            implementation(libs.findLibrary("compose-uiTest").get())
            implementation(libs.findLibrary("compose-uiTestJunit4").get())
            implementation(libs.findLibrary("kotlin-testJunit").get())
            implementation(compose.desktop.currentOs)
            implementation(libs.findLibrary("roborazzi-composeDesktop").get())
        }
    }
}

apply(from = rootProject.file("gradle/roborazzi.gradle.kts"))
