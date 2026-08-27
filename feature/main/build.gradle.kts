import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

val generatedAppVersionDir = layout.buildDirectory.dir("generated/source/appVersion/commonMain/kotlin")
val generatedAppVersionFile =
    generatedAppVersionDir.map {
        it.file("kurou/kodriver/feature/main/GeneratedAppVersion.kt")
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
                package kurou.kodriver.feature.main

                internal const val GENERATED_APP_VERSION = "$appVersion"
                """.trimIndent() + "\n",
            )
        }
    }

plugins {
    id("feature-compose-screenshot")
    `java-test-fixtures`
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
            implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutinesCore)
            implementation(projects.core.designsystem)
            implementation(libs.compose.material.icons.extended)
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

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    dependsOn(generateAppVersionSource)
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.main.generated.resources"
    publicResClass = true
}

dependencies {
    testFixturesApi(projects.core.domain)
    testFixturesImplementation(platform(libs.koin.bom))
    testFixturesImplementation(libs.koin.core)
    testFixturesImplementation(platform(libs.kotlinx.coroutines.bom))
    testFixturesImplementation(libs.kotlinx.coroutinesCore)
}
