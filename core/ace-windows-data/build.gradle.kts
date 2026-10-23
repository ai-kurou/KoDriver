import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kover)
}

kotlin {
    jvm()

    android {
        namespace = "kurou.kodriver.core.acewindowsdata"
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

    sourceSets {
        val jvmAndroidMain by creating {
            dependsOn(commonMain.get())
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
                implementation(project.dependencies.platform(libs.sentry.bom))
                implementation(libs.sentry)
            }
        }
        jvmMain {
            dependsOn(jvmAndroidMain)
            dependencies { implementation(projects.core.windowsSharedMemory) }
        }
        androidMain {
            dependsOn(jvmAndroidMain)
            dependencies {
                implementation(project.dependencies.platform(libs.ktor.bom))
                implementation(libs.ktor.clientCore)
                implementation(libs.ktor.clientOkhttp)
                implementation(libs.ktor.clientWebsocketsMultiplatform)
            }
        }
        jvmTest.dependencies {
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
