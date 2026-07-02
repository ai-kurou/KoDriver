import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

val generatedAppVersionDir = layout.buildDirectory.dir("generated/source/appVersion/commonMain/kotlin")
val generatedAppVersionFile = generatedAppVersionDir.map {
    it.file("kurou/kodriver/feature/otherlist/GeneratedAppVersion.kt")
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
            package kurou.kodriver.feature.otherlist

            internal const val GENERATED_APP_VERSION = "$appVersion"
            """.trimIndent() + "\n",
        )
    }
}

plugins {
    id("feature-compose-screenshot")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.otherlist"
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(generatedAppVersionDir)
        }
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(libs.compose.material3.adaptive.layout)
            implementation(libs.compose.material3.adaptive.navigation)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutinesTest)
        }
        named("androidHostTest") {
            dependencies {
                implementation(libs.kotlin.testJunit)
                implementation(libs.junit)
                implementation(libs.roborazzi.compose)
                implementation(libs.robolectric)
                implementation(libs.roborazzi.core)
            }
        }
    }
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    dependsOn(generateAppVersionSource)
}

compose.resources {
    packageOfResClass = "kodriver.feature.otherlist.generated.resources"
    publicResClass = true
}
