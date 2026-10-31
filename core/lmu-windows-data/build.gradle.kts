import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kover)
}

kotlin {
    jvm()

    android {
        namespace = "kurou.kodriver.core.lmuwindowsdata"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        compilerOptions { jvmTarget = JvmTarget.JVM_11 }
        withHostTest { isIncludeAndroidResources = true }
        lint {
            abortOnError = true
            warningsAsErrors = false
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("jvmAndroid") {
                withJvm()
                withCompilations { it.target.platformType == KotlinPlatformType.androidJvm }
            }
        }
    }

    sourceSets {
        named("jvmAndroidMain") {
            dependencies {
                implementation(projects.core.domain)
                implementation(projects.core.lmuWindowsDomain)
                implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
                implementation(libs.kotlinx.coroutinesCore)
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                implementation(libs.androidx.datastore.core)
                implementation(project.dependencies.platform(libs.kotlinx.serialization.bom))
                implementation(libs.kotlinx.serialization.protobuf)
                implementation(libs.kotlinx.serialization.json)
                implementation(project.dependencies.platform(libs.sentry.bom))
                implementation(libs.sentry)
            }
        }
        jvmMain {
            dependencies { implementation(projects.core.windowsSharedMemory) }
        }
        androidMain {
            dependencies {
                implementation(project.dependencies.platform(libs.ktor.bom))
                implementation(libs.ktor.clientCore)
                implementation(libs.ktor.clientOkhttp)
                implementation(libs.ktor.clientWebsocketsMultiplatform)
            }
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
            implementation(libs.kotlin.testJunit)
            implementation(libs.junit)
            implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.androidx.datastore.core)
            implementation(project(path = projects.core.windowsSharedMemory.path, configuration = "testArtifacts"))
        }
        named("androidHostTest") {
            dependencies {
                implementation(libs.kotlin.testJunit)
                implementation(libs.junit)
                implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
                implementation(libs.kotlinx.coroutinesTest)
                implementation(project.dependencies.platform(libs.okhttp.bom))
                implementation(libs.okhttp)
                implementation(libs.okhttp.mockwebserver)
            }
        }
    }
}
