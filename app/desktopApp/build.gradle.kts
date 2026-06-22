import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kover)
}

dependencies {
    implementation(projects.app.shared)
    implementation(projects.core.data)
    implementation(projects.core.lmuWindowsData)
    implementation(projects.core.gt7Ps5Data)
    implementation(projects.server)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.sentry)
    implementation(libs.koin.compose.viewmodel)

    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutinesTest)
    testImplementation(libs.compose.uiTest)
    testImplementation(libs.compose.uiTestJunit4)
    testImplementation(compose.desktop.currentOs)
    testImplementation(testFixtures(projects.app.shared))

    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.adaptive.navigation.suite)
    implementation(compose.materialIconsExtended)
    implementation(libs.compose.ui)
    implementation(libs.compose.uiToolingPreview)
}

val generateAppVersion by tasks.registering {
    val version = providers.gradleProperty("appVersion")
    val outputDir = layout.buildDirectory.dir("generated/appVersion/kotlin")
    inputs.property("appVersion", version)
    outputs.dir(outputDir)
    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()
        File(dir, "AppVersion.kt").writeText(
            """
            package kurou.kodriver

            internal const val APP_VERSION = "${version.get()}"
            """.trimIndent(),
        )
    }
}

kotlin.sourceSets.main {
    kotlin.srcDirs(generateAppVersion)
}

tasks.withType<Test>().configureEach {
    systemProperty("skiko.renderApi", "SOFTWARE_FAST")
}

compose.desktop {
    application {
        mainClass = "kurou.kodriver.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "KoDriver"
            packageVersion = providers.gradleProperty("appVersion").get()
            windows {
                shortcut = true
                iconFile.set(project.file("src/main/resources/launcher.ico"))
            }
        }
    }
}
