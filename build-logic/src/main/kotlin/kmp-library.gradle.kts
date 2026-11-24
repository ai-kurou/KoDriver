import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlinx.kover")
}

private val libs = versionCatalogs.named("libs")

private fun versionAsInt(alias: String): Int {
    val version = libs.findVersion(alias).get()
    return version.requiredVersion.toInt()
}

kotlin {
    explicitApi()

    jvm()

    android {
        compileSdk = versionAsInt("android-compileSdk")
        minSdk = versionAsInt("android-minSdk")

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        lint {
            abortOnError = true
            warningsAsErrors = false
            // minSdk 未満の API を SDK_INT ガードなしで使うと古い端末でのみクラッシュするため、重大度を error に固定する（方針は docs/coding-conventions.md）
            error += setOf("NewApi", "InlinedApi")
        }
    }
}
