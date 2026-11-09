import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    id("kmp-library")
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    android {
        namespace = "kurou.kodriver.core.acewindowsdata"
        withHostTest { isIncludeAndroidResources = true }
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
                implementation(projects.core.aceWindowsDomain)
                implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
                implementation(libs.kotlinx.coroutinesCore)
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                implementation(libs.androidx.datastore.core)
                implementation(project.dependencies.platform(libs.kotlinx.serialization.bom))
                implementation(libs.kotlinx.serialization.protobuf)
                implementation(libs.kotlinx.serialization.json)
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
