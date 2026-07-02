import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

val generatedAppVersionDir = layout.buildDirectory.dir("generated/source/appVersion/commonMain/kotlin")
val generatedAppVersionFile = generatedAppVersionDir.map {
    it.file("kurou/kodriver/feature/main/GeneratedAppVersion.kt")
}
val generateAppVersionSource = tasks.register("generateAppVersionSource") {
    val appVersion = providers.gradleProperty("appVersion").get()
    val outputFile = generatedAppVersionFile.get().asFile

    inputs.property("appVersion", appVersion)
    outputs.file(outputFile)

    doLast {
        outputFile.parentFile.mkdirs()
        outputFile.writeText(
            """
            package kurou.kodriver.feature.main

            internal const val GENERATED_APP_VERSION = "$appVersion"
            """.trimIndent() + "\n",
        )
    }
}

plugins {
    id("feature-kmp")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.main"
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(generatedAppVersionDir)
        }
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutinesCore)
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
            }
        }
    }
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    dependsOn(generateAppVersionSource)
}
