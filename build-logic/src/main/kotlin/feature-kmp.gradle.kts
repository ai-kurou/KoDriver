import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("kmp-library")
}

private val libs = versionCatalogs.named("libs")

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("nonAndroid") {
                withJvm()
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(libs.findLibrary("androidx-lifecycle-viewmodelCompose").get())
            implementation(project.dependencies.platform(libs.findLibrary("koin-bom").get()))
            implementation(libs.findLibrary("koin-compose-viewmodel").get())
        }
        commonTest.dependencies {
            implementation(libs.findLibrary("kotlin-test").get())
        }
    }
}
