import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.net.URI
import java.security.MessageDigest
import javax.inject.Inject

/**
 * Voskの日本語モデルを配布元から取得してSHA-256を検証し、`vosk-model-ja`ディレクトリとして展開する。
 *
 * 展開先の`outputDir`をCompose Desktopの`appResourcesRootDir`にし、`common/vosk-model-ja`をインストール先へ実フォルダのまま同梱する
 * （VoskのModelはJAR内のリソースを読めず、ディレクトリのパスを要求するため）。
 */
abstract class PrepareVoskModelTask
    @Inject
    constructor(
        private val archives: ArchiveOperations,
        private val fileSystem: FileSystemOperations,
    ) : DefaultTask() {
        @get:Input
        abstract val modelUrl: Property<String>

        @get:Input
        abstract val sha256: Property<String>

        @get:OutputDirectory
        abstract val outputDir: DirectoryProperty

        @TaskAction
        fun prepare() {
            val zip = temporaryDir.resolve("vosk-model.zip")
            URI(modelUrl.get()).toURL().openStream().use { input ->
                zip.outputStream().use { output -> input.copyTo(output) }
            }
            val actual =
                MessageDigest
                    .getInstance("SHA-256")
                    .digest(zip.readBytes())
                    .joinToString("") { "%02x".format(it) }
            check(actual == sha256.get()) {
                "Voskモデルのチェックサムが一致しません。期待値=${sha256.get()} 実際=$actual"
            }
            val target = outputDir.get().asFile.resolve("common/vosk-model-ja")
            fileSystem.delete { delete(target) }
            fileSystem.copy {
                from(archives.zipTree(zip))
                into(target)
                includeEmptyDirs = false
                // zip直下のディレクトリ（vosk-model-small-ja-0.22）を取り除き、モデルの中身を直接配置する
                eachFile { relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray()) }
            }
        }
    }

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
    implementation(projects.core.lmuWindowsRestApiData)
    implementation(projects.core.gt7Ps5Data)
    implementation(projects.core.aceWindowsData)
    implementation(projects.core.deviceVolumeData)
    implementation(projects.core.speechRecognitionData)
    implementation(projects.core.textToSpeechData)
    implementation(projects.core.windowsStartupData)
    implementation(projects.core.windowsHotkeyData)
    implementation(projects.server)

    implementation(compose.desktop.currentOs)
    implementation(platform(libs.kotlinx.coroutines.bom))
    implementation(libs.kotlinx.coroutinesSwing)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    testImplementation(platform(libs.koin.bom))
    testImplementation(libs.koin.test)
    implementation(platform(libs.sentry.bom))
    implementation(libs.sentry)

    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.junit)
    testImplementation(platform(libs.kotlinx.coroutines.bom))
    testImplementation(libs.kotlinx.coroutinesTest)
    testImplementation(libs.compose.uiTest)
    testImplementation(libs.compose.uiTestJunit4)
    testImplementation(compose.desktop.currentOs)
    testImplementation(testFixtures(projects.app.shared))

    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.components.resources)
}

compose.resources {
    packageOfResClass = "kurou.kodriver.generated.resources"
}

val prepareVoskModel =
    tasks.register<PrepareVoskModelTask>("prepareVoskModel") {
        modelUrl.set("https://alphacephei.com/vosk/models/vosk-model-small-ja-0.22.zip")
        sha256.set("efa092d280153a77615e9e0c7d7283e93e600de3d19d3bec686c57ef19d52eac")
        outputDir.set(layout.buildDirectory.dir("voskModel"))
    }

val generateAppVersion =
    tasks.register("generateAppVersion") {
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

tasks.named("compileTestKotlin") {
    (this as org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>).compilerOptions {
        freeCompilerArgs.add("-opt-in=androidx.compose.ui.test.ExperimentalTestApi")
    }
}

compose.desktop {
    application {
        mainClass = "kurou.kodriver.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "KoDriver"
            packageVersion = providers.gradleProperty("appVersion").get()
            // Voskの日本語モデルをインストール先へ同梱する（common配下は全OS共通のリソースになる）
            appResourcesRootDir.set(prepareVoskModel.flatMap { it.outputDir })
            windows {
                shortcut = true
                perUserInstall = true
                iconFile.set(project.file("src/main/resources/launcher.ico"))
                upgradeUuid = "885EA480-3A3E-49DF-BB86-558A47B331D7"
            }
        }
    }
}
