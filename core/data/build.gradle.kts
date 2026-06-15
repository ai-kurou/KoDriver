import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kover)
}

kotlin {
    jvm()

    androidLibrary {
        namespace = "kurou.kodriver.core.data"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
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
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(libs.kotlinx.coroutinesCore)
            implementation(libs.koin.core)
        }
        val jvmAndroidMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.androidx.datastore.core)
                implementation(libs.kotlinx.serialization.protobuf)
            }
        }
        jvmMain {
            dependsOn(jvmAndroidMain)
            dependencies {
                implementation(libs.jna)
                implementation(libs.jna.platform)
            }
        }
        androidMain {
            dependsOn(jvmAndroidMain)
            dependencies {
                implementation(libs.androidx.datastore.preferences.android)
                implementation(libs.ktor.clientCore)
                implementation(libs.ktor.clientOkhttp)
                implementation(libs.ktor.clientWebsocketsMultiplatform)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.testJunit)
            implementation(libs.junit)
            implementation(libs.kotlinx.coroutinesTest)
        }
        named("androidHostTest") {
            dependencies {
                implementation(libs.kotlin.testJunit)
                implementation(libs.junit)
                implementation(libs.kotlinx.coroutinesTest)
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.okhttp.mockwebserver)
            }
        }
    }
}
