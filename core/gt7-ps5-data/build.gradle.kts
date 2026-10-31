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
        namespace = "kurou.kodriver.core.gt7ps5data"
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
                implementation(projects.core.gt7Ps5Domain)
                implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
                implementation(libs.kotlinx.coroutinesCore)
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                implementation(libs.androidx.datastore.core)
                implementation(project.dependencies.platform(libs.kotlinx.serialization.bom))
                implementation(libs.kotlinx.serialization.protobuf)
                implementation(project.dependencies.platform(libs.sentry.bom))
                implementation(libs.sentry)
            }
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
            implementation(libs.kotlin.testJunit)
            implementation(libs.junit)
            implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.androidx.datastore.core)
        }
    }
}
