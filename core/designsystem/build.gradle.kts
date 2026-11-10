import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("kmp-library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    android {
        namespace = "kurou.kodriver.core.designsystem"
        androidResources {
            enable = true
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
            implementation(libs.reorderable)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmTest.dependencies {
            implementation(libs.compose.uiTest)
            implementation(libs.compose.uiTestJunit4)
            implementation(libs.kotlin.testJunit)
            implementation(libs.mockk)
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
