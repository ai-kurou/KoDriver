import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kover)
}

kotlin {
    jvm()

    android {
        namespace = "kurou.kodriver.core.devicevolumedata"
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
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutinesCore)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }
        jvmMain.dependencies {
            implementation(libs.jna)
            implementation(libs.jna.platform)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.testJunit)
            implementation(libs.junit)
            implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.mockk)
        }
        named("androidHostTest") {
            dependencies {
                implementation(libs.kotlin.testJunit)
                implementation(libs.junit)
                implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
                implementation(libs.kotlinx.coroutinesTest)
                implementation(libs.mockk)
            }
        }
    }
}
