import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kover)
    `java-test-fixtures`
}

kotlin {
    jvm()
    
    js {
        browser()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    
    android {
       namespace = "kurou.kodriver.app.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()

       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       lint {
           abortOnError = true
           warningsAsErrors = false
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
        }
        jvmMain.dependencies {
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(projects.feature.main)
            implementation(projects.feature.lmuWindowsConnection)
            implementation(projects.feature.gt7Ps5Connection)
            implementation(projects.feature.serverConnection)
            implementation(projects.feature.lmuWindowsNarrator)
            implementation(projects.feature.otherLicenseDetail)
            implementation(projects.feature.otherList)
            implementation(projects.feature.otherServerIpDetail)
            implementation(projects.feature.otherConsoleIpDetail)
            implementation(projects.feature.otherReadoutStartSoundDetail)
            implementation(projects.feature.otherVolumeDetail)
            implementation(projects.feature.readoutList)
            implementation(projects.feature.lmuWindowsReadoutVehicleApproachDetail)
            implementation(projects.feature.lmuWindowsReadoutFlagDetail)
            implementation(projects.feature.lmuWindowsReadoutVehicleDamageDetail)
            implementation(projects.feature.gt7Ps5ReadoutMyBestlapDetail)
            implementation(libs.koin.core)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material3.adaptive.navigation.suite)
            implementation(libs.compose.material3.adaptive.layout)
            implementation(libs.compose.material3.adaptive.navigation)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.koin.compose.viewmodel)
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
            implementation(libs.compose.material3.adaptive.layout)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
    add("jvmTestImplementation", testFixtures(projects.feature.lmuWindowsNarrator))
    add("jvmTestImplementation", testFixtures(projects.feature.readoutList))
    testFixturesApi(testFixtures(projects.feature.lmuWindowsNarrator))
    testFixturesApi(testFixtures(projects.feature.readoutList))
}

apply(from = rootProject.file("gradle/roborazzi.gradle.kts"))
