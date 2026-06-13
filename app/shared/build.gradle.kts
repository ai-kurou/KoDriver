import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.aboutlibraries)
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
    
    androidLibrary {
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
            implementation(projects.feature.lmuConnection)
            implementation(projects.feature.narrator)
            implementation(projects.feature.otherDetail)
            implementation(projects.feature.otherList)
            implementation(projects.feature.readout)
            implementation(projects.feature.readoutVehicleApproach)
            implementation(projects.feature.readoutFlagDetail)
            implementation(projects.feature.lmuReadoutVehicleDamageDetail)
            implementation(libs.aboutlibraries.compose.m3)
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
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
    add("jvmTestImplementation", testFixtures(projects.app.shared))
    testFixturesApi(testFixtures(projects.feature.narrator))
    testFixturesApi(testFixtures(projects.feature.readout))
}

apply(from = rootProject.file("gradle/roborazzi.gradle.kts"))
