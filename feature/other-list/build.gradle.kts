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
    id("feature-kmp")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidLibrary {
        namespace = "kurou.kodriver.feature.otherlist"
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
        val nonAndroidMain by creating {
            dependsOn(commonMain.get())
        }
        jvmMain.get().dependsOn(nonAndroidMain)
        jsMain.get().dependsOn(nonAndroidMain)
        wasmJsMain.get().dependsOn(nonAndroidMain)

        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
        }
        jvmMain.dependencies {
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material3.adaptive.layout)
            implementation(libs.compose.material3.adaptive.navigation)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.koin.compose.viewmodel)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.kotlin.test)
        }
        jvmTest.dependencies {
            implementation(libs.compose.uiTest)
            implementation(libs.compose.uiTestJunit4)
            implementation(libs.kotlin.testJunit)
            implementation(compose.desktop.currentOs)
            implementation(libs.roborazzi.composeDesktop)
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

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

apply(from = rootProject.file("gradle/roborazzi.gradle.kts"))
