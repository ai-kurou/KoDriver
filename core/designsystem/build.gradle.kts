import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kover)
}

kotlin {
    jvm()

    android {
        namespace = "kurou.kodriver.core.designsystem"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()

        androidResources {
            enable = true
        }

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        lint {
            abortOnError = true
            warningsAsErrors = false
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("nonAndroid") {
                withJvm()
            }
        }
    }

    sourceSets {
        jvmMain.dependencies {
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material3.adaptive.layout)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.components.resources)
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
        jvmTest {
            kotlin.srcDir(
                rootProject.layout.projectDirectory.dir("build-logic/src/featureComposeScreenshotJvmTest/kotlin"),
            )
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.core.designsystem.generated.resources"
    publicResClass = true
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

tasks.named("compileTestKotlinJvm") {
    (this as org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>).compilerOptions {
        freeCompilerArgs.add("-opt-in=androidx.compose.ui.test.ExperimentalTestApi")
    }
}

apply(from = rootProject.file("gradle/roborazzi.gradle.kts"))
