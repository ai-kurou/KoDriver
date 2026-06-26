import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlinx.kover")
}

private val libs = versionCatalogs.named("libs")

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
        compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
        minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        lint {
            abortOnError = true
            warningsAsErrors = false
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(libs.findLibrary("androidx-lifecycle-viewmodelCompose").get())
            implementation(libs.findLibrary("koin-compose-viewmodel").get())
        }
        commonTest.dependencies {
            implementation(libs.findLibrary("kotlin-test").get())
        }
    }
}
