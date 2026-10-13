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
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()

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
        jvmMain.dependencies {
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(projects.feature.desktopSplash)
            implementation(projects.feature.main)
            implementation(projects.feature.lmuWindowsConnection)
            implementation(projects.feature.gt7Ps5Connection)
            implementation(projects.feature.aceWindowsConnection)
            implementation(projects.feature.serverConnection)
            implementation(projects.feature.lmuWindowsNarrator)
            implementation(projects.feature.gt7Ps5Narrator)
            implementation(projects.feature.aceWindowsNarrator)
            implementation(projects.feature.narratorOverlay)
            implementation(projects.feature.lmuWindowsDebugStateDetail)
            implementation(projects.feature.gt7Ps5DebugStateDetail)
            implementation(projects.feature.aceWindowsDebugStateDetail)
            implementation(projects.feature.otherLicenseDetail)
            implementation(projects.feature.otherList)
            implementation(projects.feature.otherFeedbackDetail)
            implementation(projects.feature.otherServerIpDetail)
            implementation(projects.feature.otherConsoleIpDetail)
            implementation(projects.feature.otherReadoutStartSoundDetail)
            implementation(projects.feature.otherThemeDetail)
            implementation(projects.feature.otherOverlayTextColorDetail)
            implementation(projects.feature.otherOverlayTextSizeDetail)
            implementation(projects.feature.otherOverlayBackgroundOpacityDetail)
            implementation(projects.feature.otherVolumeDetail)
            implementation(projects.feature.otherVoiceDetail)
            implementation(projects.feature.otherVoiceSpeedDetail)
            implementation(projects.feature.otherVoicePitchDetail)
            implementation(projects.feature.telemetryLogList)
            implementation(projects.feature.telemetryLogDetail)
            implementation(projects.feature.autoReadoutList)
            implementation(projects.feature.lmuWindowsAutoReadoutVehicleApproachDetail)
            implementation(projects.feature.lmuWindowsAutoReadoutFlagDetail)
            implementation(projects.feature.lmuWindowsAutoReadoutMyBestLapDetail)
            implementation(projects.feature.lmuWindowsAutoReadoutVehicleDamageDetail)
            implementation(projects.feature.lmuWindowsAutoReadoutTyreTemperatureDetail)
            implementation(projects.feature.lmuWindowsAutoReadoutPitTimingDetail)
            implementation(projects.feature.lmuWindowsAutoReadoutRemainingVirtualEnergyDetail)
            implementation(projects.feature.lmuWindowsAutoReadoutTyreWearDetail)
            implementation(projects.feature.lmuWindowsAutoReadoutBrakeTemperatureDetail)
            implementation(projects.feature.lmuWindowsAutoReadoutBrakeWearDetail)
            implementation(projects.feature.gt7Ps5AutoReadoutMyBestLapDetail)
            implementation(projects.feature.gt7Ps5AutoReadoutRemainingFuelDetail)
            implementation(projects.feature.gt7Ps5AutoReadoutRemainingFuelLapsDetail)
            implementation(projects.feature.gt7Ps5AutoReadoutTyreTemperatureDetail)
            implementation(projects.feature.aceWindowsAutoReadoutRemainingFuelDetail)
            implementation(projects.feature.aceWindowsAutoReadoutFlagDetail)
            implementation(projects.feature.aceWindowsAutoReadoutTyreTemperatureDetail)
            implementation(projects.feature.aceWindowsAutoReadoutVehicleApproachDetail)
            implementation(projects.feature.aceWindowsAutoReadoutMyBestLapDetail)
            implementation(projects.feature.aceWindowsAutoReadoutRemainingFuelLapsDetail)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material3.adaptive.navigation.suite)
            implementation(libs.compose.material3.adaptive.layout)
            implementation(libs.compose.material3.adaptive.navigation)
            implementation(libs.navigation3.ui)
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
            implementation(libs.mockk)
            implementation(libs.compose.uiTest)
            implementation(libs.compose.uiTestJunit4)
            implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.kotlin.testJunit)
            implementation(compose.desktop.currentOs)
            implementation(libs.roborazzi.composeDesktop)
            implementation(libs.compose.material3.adaptive.layout)
        }
        jvmTest {
            kotlin.srcDir(
                rootProject.layout.projectDirectory.dir("build-logic/src/featureComposeScreenshotJvmTest/kotlin"),
            )
            kotlin.srcDir(
                rootProject.layout.projectDirectory.dir("build-logic/src/paneDirectiveJvmTest/kotlin"),
            )
        }
        named("androidHostTest") {
            kotlin.srcDir(
                rootProject.layout.projectDirectory.dir(
                    "build-logic/src/featureComposeScreenshotAndroidHostTest/kotlin",
                ),
            )
            kotlin.srcDir(
                rootProject.layout.projectDirectory.dir(
                    "build-logic/src/paneDirectiveAndroidHostTest/kotlin",
                ),
            )
            dependencies {
                implementation(libs.kotlin.testJunit)
                implementation(libs.junit)
                implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
                implementation(libs.kotlinx.coroutinesTest)
                implementation(libs.compose.uiTestJunit4)
                implementation(libs.androidx.activity.compose)
                implementation(libs.roborazzi.compose)
                implementation(libs.compose.material3.adaptive.layout)
                implementation(libs.robolectric)
                implementation(libs.roborazzi.core)
            }
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
    add("jvmTestImplementation", testFixtures(projects.feature.lmuWindowsNarrator))
    add("jvmTestImplementation", testFixtures(projects.feature.gt7Ps5Narrator))
    add("jvmTestImplementation", testFixtures(projects.feature.autoReadoutList))
    add("jvmTestImplementation", testFixtures(projects.feature.telemetryLogList))
    add("jvmTestImplementation", testFixtures(projects.feature.main))
    add("jvmTestImplementation", testFixtures(projects.feature.otherThemeDetail))
    add("jvmTestImplementation", testFixtures(projects.feature.otherReadoutStartSoundDetail))
    add("jvmTestImplementation", testFixtures(projects.feature.otherConsoleIpDetail))
    add("jvmTestImplementation", testFixtures(projects.feature.otherVolumeDetail))
    add("jvmTestImplementation", testFixtures(projects.feature.otherVoiceDetail))
    add("jvmTestImplementation", testFixtures(projects.feature.otherList))
    add("androidHostTestImplementation", testFixtures(projects.feature.otherThemeDetail))
    testFixturesApi(testFixtures(projects.feature.lmuWindowsNarrator))
    testFixturesApi(testFixtures(projects.feature.gt7Ps5Narrator))
    testFixturesApi(testFixtures(projects.feature.autoReadoutList))
    testFixturesApi(testFixtures(projects.feature.telemetryLogList))
    testFixturesApi(testFixtures(projects.feature.main))
    testFixturesApi(testFixtures(projects.feature.otherOverlayTextColorDetail))
    testFixturesApi(testFixtures(projects.feature.otherThemeDetail))
    testFixturesApi(testFixtures(projects.feature.otherReadoutStartSoundDetail))
    testFixturesApi(testFixtures(projects.feature.otherConsoleIpDetail))
    testFixturesApi(testFixtures(projects.feature.otherVolumeDetail))
    testFixturesApi(testFixtures(projects.feature.otherVoiceDetail))
    testFixturesApi(testFixtures(projects.feature.otherList))
}

compose.resources {
    packageOfResClass = "kurou.kodriver.app.shared.generated.resources"
}

tasks.named("compileTestKotlinJvm") {
    (this as org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>).compilerOptions {
        freeCompilerArgs.add("-opt-in=androidx.compose.ui.test.ExperimentalTestApi")
    }
}

apply(from = rootProject.file("gradle/roborazzi.gradle.kts"))
