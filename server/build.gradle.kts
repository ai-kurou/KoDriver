plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kover)
}

group = "kurou.kodriver"
version = "1.0.0"
application {
    mainClass = "kurou.kodriver.ApplicationKt"
}

val generateBuildConfig by tasks.registering {
    val appVersion = providers.gradleProperty("appVersion")
    val outputDir = layout.buildDirectory.dir("generated/buildConfig")
    inputs.property("appVersion", appVersion)
    outputs.dir(outputDir)
    doLast {
        val dir = outputDir.get().asFile.resolve("kurou/kodriver")
        dir.mkdirs()
        dir.resolve("BuildConfig.kt").writeText(
            """
            package kurou.kodriver

            internal object BuildConfig {
                const val APP_VERSION = "${appVersion.get()}"
            }
            """.trimIndent(),
        )
    }
}

kotlin.sourceSets.main {
    kotlin.srcDir(generateBuildConfig.map { layout.buildDirectory.dir("generated/buildConfig") })
}

dependencies {
    api(projects.core.domain)
    implementation(libs.logback)
    implementation(libs.koin.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverWebsockets)
    testImplementation(libs.ktor.clientWebsockets)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlinx.coroutinesTest)
    testImplementation(libs.kotlin.testJunit)
}
