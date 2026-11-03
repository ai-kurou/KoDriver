import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

val generatedAppVersionDir = layout.buildDirectory.dir("generated/source/appVersion/commonMain/kotlin")
val generatedAppVersionFile =
    generatedAppVersionDir.map {
        it.file("kurou/kodriver/domain/util/GeneratedAppVersion.kt")
    }
val generateAppVersionSource =
    tasks.register("generateAppVersionSource") {
        val appVersion = providers.gradleProperty("appVersion").get()
        val outputFile = generatedAppVersionFile.get().asFile

        inputs.property("appVersion", appVersion)
        outputs.file(outputFile)

        doLast {
            outputFile.parentFile.mkdirs()
            outputFile.writeText(
                """
                package kurou.kodriver.domain.util

                /** `gradle.properties` の `appVersion` からビルド時に生成されるアプリのバージョン。 */
                const val APP_VERSION = "$appVersion"
                """.trimIndent() + "\n",
            )
        }
    }

plugins {
    id("kmp-library")
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    android {
        namespace = "kurou.kodriver.core.domain"
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(generatedAppVersionDir)
        }
        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutinesCore)
            implementation(project.dependencies.platform(libs.kotlinx.serialization.bom))
            implementation(libs.kotlinx.serialization.json)
        }
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.sentry.bom))
            implementation(libs.sentry)
        }
        jvmMain.dependencies {
            implementation(project.dependencies.platform(libs.sentry.bom))
            implementation(libs.sentry)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        getByName("jvmTest") {
            dependencies {
                implementation(libs.kotlin.testJunit)
                implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
                implementation(libs.kotlinx.coroutinesTest)
                implementation(libs.mockk)
            }
        }
    }
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    dependsOn(generateAppVersionSource)
}
