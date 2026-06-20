plugins {
    id("feature-kmp")
    alias(libs.plugins.aboutlibraries)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidLibrary {
        namespace = "kurou.kodriver.feature.otherlicensedetail"
        androidResources {
            enable = true
        }
    }

    sourceSets {
        val nonAndroidMain by creating {
            dependsOn(commonMain.get())
        }
        jvmMain.get().dependsOn(nonAndroidMain)
        jsMain.get().dependsOn(nonAndroidMain)
        wasmJsMain.get().dependsOn(nonAndroidMain)

        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
        }
        jvmMain.dependencies {
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(libs.aboutlibraries.compose.m3)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmTest.dependencies {
            implementation(libs.compose.uiTest)
            implementation(libs.compose.uiTestJunit4)
            implementation(libs.kotlin.testJunit)
            implementation(compose.desktop.currentOs)
            implementation(libs.roborazzi.composeDesktop)
        }
    }
}

compose.resources {
    packageOfResClass = "kodriver.feature.otherlicensedetail.generated.resources"
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

apply(from = rootProject.file("gradle/roborazzi.gradle.kts"))
