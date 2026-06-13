import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
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

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    androidLibrary {
        namespace = "kurou.kodriver.feature.otherdetail"
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
            implementation(projects.core.domain)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material3.adaptive.layout)
            implementation(libs.compose.material3.adaptive.navigation)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.koin.compose.viewmodel)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutinesTest)
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
    packageOfResClass = "kodriver.feature.otherdetail.generated.resources"
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

apply(from = rootProject.file("gradle/roborazzi.gradle.kts"))
