import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.modulesGraphAssert)
    alias(libs.plugins.kotlinxSerialization) apply false
}

plugins.withType<YarnPlugin> {
    extensions.configure<YarnRootExtension> {
        resolution("body-parser", "1.20.6")
        resolution("brace-expansion", "5.0.8")
        resolution("diff", "8.0.3")
        resolution("serialize-javascript", "7.0.5")
        resolution("fast-uri", "3.1.4")
        resolution("js-yaml", "4.3.0")
        resolution("shell-quote", "1.10.0")
        resolution("uuid", "11.1.1")
        resolution("webpack-dev-server", "5.2.6")
        resolution("webpack", "5.104.1")
        resolution("ws", "8.21.0")
    }
}

plugins.withType<NodeJsRootPlugin> {
    extensions.configure<NodeJsRootExtension> {
        versions.webpack.version = "5.104.1"
        versions.webpackDevServer.version = "5.2.6"
    }
}

val isCI = System.getenv("CI") != null
data class RoborazziAggregateTask(
    val aggregateTaskName: String,
    val childTaskName: String,
    val requiredTaskName: String,
)

val roborazziAggregateTasks = listOf(
    RoborazziAggregateTask("recordRoborazziJvmTests", "recordRoborazziJvmTest", "jvmTest"),
    RoborazziAggregateTask("verifyRoborazziJvmTests", "verifyRoborazziJvmTest", "jvmTest"),
    RoborazziAggregateTask("recordRoborazziAndroidHostTests", "recordRoborazziAndroidHostTest", "testAndroidHostTest"),
    RoborazziAggregateTask("verifyRoborazziAndroidHostTests", "verifyRoborazziAndroidHostTest", "testAndroidHostTest"),
)

detekt {
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = !isCI
}

subprojects {
    // macOS でテスト用 JVM が Dock に表示されて画面フォーカスが奪われるのを防ぐ
    tasks.withType<Test>().configureEach {
        jvmArgs("-Dapple.awt.UIElement=true")
    }
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        autoCorrect = !isCI
    }
    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set(rootProject.libs.versions.ktlintCore.get())
    }
    tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask>().configureEach {
        exclude { it.file.path.replace(File.separatorChar, '/').contains("/build/generated/") }
    }
    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        ignoreFailures = false
        exclude { it.file.absolutePath.replace(File.separatorChar, '/').contains("/build/generated/") }
    }
    // KMP では commonMain のソースセットは detektMetadataCommonMain タスクで解析される。
    // detekt タスクがこれを依存に含めないと commonMain が未検査になるため明示的に追加する。
    afterEvaluate {
        tasks.findByName("detekt")?.dependsOn(
            tasks.withType<io.gitlab.arturbosch.detekt.Detekt>()
                .matching { it.name != "detekt" },
        )
    }
    dependencies {
        "detektPlugins"(rootProject.libs.detekt.formatting)
    }
    pluginManager.withPlugin("org.jetbrains.kotlinx.kover") {
        extensions.configure<kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension> {
            reports {
                filters {
                    excludes {
                        annotatedBy("androidx.compose.ui.tooling.preview.Preview")
                        classes("*ComposableSingletons\$*")
                        classes("*Empty*")
                        classes("*PreviewKt")
                        classes("*PreviewKt\$*")
                        classes("*PreviewParameterProvider")
                    }
                }
            }
        }
    }
}

moduleGraphAssert {
    maxHeight = 3
    configurations = setOf(
        // JVM・Android プロジェクトの標準依存関係
        "api",
        "implementation",
        "testImplementation",
        "testFixturesApi",
        "testFixturesImplementation",
        // Kotlin Multiplatform の本番 source set
        "commonMainApi",
        "commonMainImplementation",
        "androidMainApi",
        "androidMainImplementation",
        "jvmMainApi",
        "jvmMainImplementation",
        "jsMainApi",
        "jsMainImplementation",
        "wasmJsMainApi",
        "wasmJsMainImplementation",
        "nonAndroidMainApi",
        "nonAndroidMainImplementation",
        "jvmAndroidMainApi",
        "jvmAndroidMainImplementation",
        // Kotlin Multiplatform のテスト source set
        "commonTestApi",
        "commonTestImplementation",
        "androidHostTestApi",
        "androidHostTestImplementation",
        "androidUnitTestApi",
        "androidUnitTestImplementation",
        "jvmTestApi",
        "jvmTestImplementation",
        "jsTestApi",
        "jsTestImplementation",
        "wasmJsTestApi",
        "wasmJsTestImplementation",
        "nonAndroidTestApi",
        "nonAndroidTestImplementation",
        "jvmAndroidTestApi",
        "jvmAndroidTestImplementation",
    )
    allowed = arrayOf(
        // app エントリーポイント → app:shared
        ":app:.*App -> :app:shared",
        // app エントリーポイント → core:data 系（composition root で DI バインドするための参照）
        // .*data にマッチ: core:data, core:*-data。core:domain / core:designsystem は除外される
        ":app:.*App -> :core:.*data",
        // Desktop app → server（同一プロセスで Ktor サーバーを起動するため）
        ":app:desktopApp -> :server",
        // app:shared → feature
        ":app:shared -> :feature:.*",
        // feature → core:domain
        ":feature:.* -> :core:domain",
        // feature → core:designsystem（共通 UI コンポーネントの利用）
        ":feature:.* -> :core:designsystem",
        // core:data 系 → core:domain（.*data にマッチ: core:data, core:*-data。core:designsystem は除外される）
        ":core:.*data -> :core:domain",
        // Windows共有メモリ系データモジュール → core:windows-shared-memory（Windows共有メモリI/Oの共通基盤）
        ":core:.*windows.*data -> :core:windows-shared-memory",
        // narrator系featureモジュール → core:narrator（WAV音声再生・SoundPlayer・WavNarratorEngineの共通基盤。
        // core:narrator自体はcore:domainに依存させず、SpeechEvent等はfeature側が型パラメータとして渡す）
        ":feature:.*narrator.* -> :core:narrator",
        ":server -> :core:domain",
    )
    restricted = arrayOf(
        // app エントリーポイント（feature 層・domain 層への直接参照禁止）
        ":app:.*App -X> :feature:.*",
        ":app:.*App -X> :core:domain",
        // app:shared（上位 app・core・server への参照禁止）
        ":app:shared -X> :app:.*",
        ":app:shared -X> :core:.*",
        ":app:shared -X> :server",
        // feature（上位 app・他 feature への参照禁止）
        ":feature:.* -X> :app:.*",
        ":feature:.* -X> :feature:.*",
        // core（上位層への参照禁止・逆方向依存禁止）
        ":core:domain -X> :core:data",
        ":core:.* -X> :feature:.*",
        ":core:.* -X> :app:.*",
        ":core:.* -X> :server",
        // server（app・feature への参照禁止）
        ":server -X> :app:.*",
        ":server -X> :feature:.*",
    )
}

// CLAUDE.md 冒頭のモジュール構成ツリーへの記載漏れ（#911 で発生）を機械的に検出するタスク。
// settings.gradle.kts の include() 一覧と CLAUDE.md のツリー内に列挙された、
// 説明文付きの葉モジュール名（グループ見出しの core/ feature/ app/ 自体は対象外）を突き合わせる。
tasks.register("assertClaudeMdModuleList") {
    group = "verification"
    description = "Verifies settings.gradle.kts modules and CLAUDE.md's module tree are in sync."

    val settingsFile = layout.projectDirectory.file("settings.gradle.kts").asFile
    val claudeMdFile = layout.projectDirectory.file("CLAUDE.md").asFile
    inputs.file(settingsFile)
    inputs.file(claudeMdFile)

    doLast {
        val settingsModules = settingsFile
            .readLines()
            .mapNotNull { line -> Regex("""include\("([^"]+)"\)""").find(line)?.groupValues?.get(1) }
            .map { path -> path.substringAfterLast(":") }
            .toSet()

        val claudeMdLines = claudeMdFile.readLines()
        val headingIndex = claudeMdLines.indexOfFirst { it.trim() == "## モジュール構成" }
        val treeStart = claudeMdLines
            .drop(headingIndex + 1)
            .indexOfFirst { it.trim() == "```" }
            .let { relativeIndex -> if (relativeIndex == -1) -1 else headingIndex + 1 + relativeIndex }
        val treeEnd = claudeMdLines
            .drop(treeStart + 1)
            .indexOfFirst { it.trim() == "```" }
            .let { relativeIndex -> if (relativeIndex == -1) -1 else treeStart + 1 + relativeIndex }
        check(headingIndex != -1 && treeStart != -1 && treeEnd != -1) {
            "CLAUDE.md の '## モジュール構成' コードブロックが見つかりません"
        }

        val treeDrawingChars = charArrayOf('│', '├', '└', '─', ' ')
        val documentedModules = claudeMdLines
            .subList(treeStart + 2, treeEnd) // "```" と先頭の "KoDriver/" 行を除く
            .mapNotNull { line ->
                val stripped = line.trimStart(*treeDrawingChars)
                val slashIndex = stripped.indexOf('/')
                if (slashIndex == -1) return@mapNotNull null
                val name = stripped.substring(0, slashIndex)
                val rest = stripped.substring(slashIndex + 1)
                // グループ見出し（core/ feature/ app/）は説明文を持たないため除外する
                name.takeIf { rest.isNotBlank() }
            }.toSet()

        val missingFromClaudeMd = settingsModules - documentedModules
        val staleInClaudeMd = documentedModules - settingsModules
        check(missingFromClaudeMd.isEmpty() && staleInClaudeMd.isEmpty()) {
            buildString {
                appendLine("CLAUDE.md のモジュール構成ツリーと settings.gradle.kts が一致していません。")
                if (missingFromClaudeMd.isNotEmpty()) {
                    appendLine("  CLAUDE.md に記載がないモジュール: ${missingFromClaudeMd.sorted()}")
                }
                if (staleInClaudeMd.isNotEmpty()) {
                    appendLine("  settings.gradle.kts に存在しない記載: ${staleInClaudeMd.sorted()}")
                }
            }
        }
    }
}

tasks.register("generateModuleGraphImages") {
    group = "documentation"
    description = "Generates SVG module dependency graphs and updates each module's README.md"
    notCompatibleWithConfigurationCache("Uses Project references at execution time")

    doLast {
        val dotBinary = listOf("/opt/homebrew/bin/dot", "/usr/bin/dot", "/usr/local/bin/dot")
            .firstOrNull { File(it).exists() }
            ?: error(
                "Graphviz 'dot' not found. " +
                    "Install: brew install graphviz (Mac) / apt-get install graphviz (Linux)",
            )

        val graphsDir = file("docs/graphs")
        graphsDir.mkdirs()

        fun runCommand(vararg args: String, dir: File = rootDir) {
            val result = ProcessBuilder(*args)
                .directory(dir)
                .inheritIO()
                .start()
                .waitFor()
            check(result == 0) { "Command failed (exit $result): ${args.joinToString(" ")}" }
        }

        // Gradle プロジェクト構造から直接依存を収集（KMP sourceSets の依存も正しく検出）
        val seen = mutableSetOf<Pair<String, String>>()
        val parsedEdges = mutableListOf<Triple<String, String, String>>()
        rootProject.subprojects.forEach { proj ->
            proj.configurations.forEach { config ->
                config.dependencies
                    .filterIsInstance<org.gradle.api.artifacts.ProjectDependency>()
                    .forEach { dep ->
                        val from = proj.path
                        val to = dep.path
                        if (from != to && seen.add(Pair(from, to))) {
                            parsedEdges.add(Triple(from, to, ""))
                        }
                    }
            }
        }

        val allModules = (parsedEdges.map { it.first } + parsedEdges.map { it.second }).toSet()

        fun moduleCountLabel(count: Int): String = if (count == 1) "1 module" else "$count modules"

        val featureGroup = "feature"
        val featureModuleCount = allModules.count { it.startsWith(":feature:") }

        fun overviewNode(module: String): String =
            if (module.startsWith(":feature:")) featureGroup else module

        val overviewEdges = parsedEdges
            .map { (from, to, _) -> overviewNode(from) to overviewNode(to) }
            .filter { (from, to) -> from != to }
            .toSet()
            .sortedWith(compareBy<Pair<String, String>> { it.first }.thenBy { it.second })

        val fullGvFile = file("$graphsDir/full-graph.gv")
        fullGvFile.writeText(
            buildString {
                appendLine("digraph G {")
                appendLine("  rankdir=TB")
                appendLine("  graph [ranksep=1.2, nodesep=0.6]")
                appendLine("  node [shape=box, style=rounded]")
                appendLine("  \"$featureGroup\" [label=\"feature\\n${moduleCountLabel(featureModuleCount)}\"]")
                overviewEdges.forEach { (from, to) ->
                    appendLine("  \"$from\" -> \"$to\"")
                }
                append("}")
            },
        )

        runCommand(dotBinary, "-Tsvg", fullGvFile.absolutePath, "-o", "$graphsDir/full-graph.svg")
        println("Generated: docs/graphs/full-graph.svg")

        allModules.forEach { module ->
            val neighborhood = parsedEdges
                .filter { (f, t, _) -> f == module || t == module }
                .flatMap { (f, t, _) -> listOf(f, t) }
                .toSet()
            val subEdges = parsedEdges.filter { (f, t, _) -> f in neighborhood && t in neighborhood }

            val gvContent = buildString {
                appendLine("digraph G {")
                appendLine("  rankdir=TB")
                subEdges.forEach { (from, to, attrs) ->
                    val attrPart = if (attrs.isNotEmpty()) " $attrs" else ""
                    appendLine("  \"$from\" -> \"$to\"$attrPart")
                }
                append("}")
            }

            val svgName = module.removePrefix(":").replace(":", "-")
            val moduleGvFile = file("$graphsDir/$svgName.gv")
            moduleGvFile.writeText(gvContent)

            runCommand(dotBinary, "-Tsvg", moduleGvFile.absolutePath, "-o", "$graphsDir/$svgName.svg")
            println("Generated: docs/graphs/$svgName.svg")
        }

        val startMarker = "<!-- MODULE-GRAPH-START -->"
        val endMarker = "<!-- MODULE-GRAPH-END -->"

        fun upsertReadme(readmeFile: File, svgRelativePath: String, heading: String) {
            val imgTag = "![Module Graph]($svgRelativePath)"
            val block = "$startMarker\n$heading\n\n$imgTag\n$endMarker"
            if (readmeFile.exists()) {
                val original = readmeFile.readText()
                val updated = if (original.contains(startMarker)) {
                    original.replace(
                        Regex("""$startMarker.*?$endMarker""", RegexOption.DOT_MATCHES_ALL),
                        block,
                    )
                } else {
                    original.trimEnd() + "\n\n$block\n"
                }
                readmeFile.writeText(updated)
            } else {
                readmeFile.writeText("# ${readmeFile.parentFile.name}\n\n$block\n")
            }
        }

        upsertReadme(file("README.md"), "docs/graphs/full-graph.svg", "## Module Graph")
        println("Updated: README.md")

        rootProject.subprojects.forEach { proj ->
            val svgName = proj.path.removePrefix(":").replace(":", "-")
            val svgFile = file("$graphsDir/$svgName.svg")
            if (!svgFile.exists()) return@forEach

            val readmeFile = proj.file("README.md")
            val relPath = readmeFile.parentFile.toPath()
                .relativize(svgFile.toPath())
                .toString()
                .replace('\\', '/')

            upsertReadme(readmeFile, relPath, "## Module Dependencies")
            println("Updated: ${proj.path}/README.md")
        }

        println("\nDone. Commit docs/graphs/ and any updated README.md files.")
    }
}

roborazziAggregateTasks.forEach { roborazziTask ->
    val aggregateTask = tasks.register(roborazziTask.aggregateTaskName) {
        group = "roborazzi"
        description = "Runs ${roborazziTask.childTaskName} for all projects that define it."
    }

    gradle.projectsEvaluated {
        aggregateTask.configure {
            dependsOn(
                subprojects
                    .filter { project -> project.tasks.findByName(roborazziTask.requiredTaskName) != null }
                    .map { project ->
                        project.tasks.matching { it.name == roborazziTask.childTaskName }
                    },
            )
        }
    }
}

// 完了報告・PR 作成前に必須のチェック一式を 1 コマンドに集約する。
// CLAUDE.md「コード変更時の必須確認」に対応: 全モジュールの detekt・ktlint、
// モジュールグラフ検証、全ユニットテスト（Kover カバレッジ付き）、
// Android / デスクトップアプリのビルド、デスクトップアプリの統合テスト。
val preMergeCheck = tasks.register("preMergeCheck") {
    group = "verification"
    description = "Runs all mandatory pre-merge checks (detekt, ktlint, module graph, tests with coverage, app builds)."
    dependsOn(
        ":assertModuleGraph",
        ":assertClaudeMdModuleList",
        ":koverXmlReport",
        ":app:androidApp:assembleDebug",
        ":app:desktopApp:jar",
        ":app:desktopApp:test",
    )
}

gradle.projectsEvaluated {
    preMergeCheck.configure {
        dependsOn(allprojects.map { project -> project.tasks.matching { it.name == "detekt" } })
        dependsOn(allprojects.map { project -> project.tasks.matching { it.name == "ktlintCheck" } })
    }
}

dokka {
    moduleName.set("KoDriver")
    dokkaPublications.html {
        outputDirectory.set(rootDir.resolve("docs/api"))
    }
}

kover {
    reports {
        filters {
            excludes {
                annotatedBy("androidx.compose.ui.tooling.preview.Preview")
                classes("*ComposableSingletons\$*")
                classes("*Empty*")
                classes("*PreviewKt")
                classes("*PreviewKt\$*")
                classes("*PreviewParameterProvider")
            }
        }
    }
}

dependencies {
    kover(project(":core:domain"))
    kover(project(":core:data"))
    kover(project(":core:lmu-windows-data"))
    kover(project(":core:gt7-ps5-data"))
    kover(project(":core:ace-windows-data"))
    kover(project(":core:windows-shared-memory"))
    kover(project(":core:designsystem"))
    kover(project(":core:narrator"))
    kover(project(":feature:desktop-splash"))
    kover(project(":feature:lmu-windows-connection"))
    kover(project(":feature:main"))
    kover(project(":feature:server-connection"))
    kover(project(":feature:lmu-windows-narrator"))
    kover(project(":feature:other-license-detail"))
    kover(project(":feature:other-list"))
    kover(project(":feature:other-server-ip-detail"))
    kover(project(":feature:other-console-ip-detail"))
    kover(project(":feature:other-readout-start-sound-detail"))
    kover(project(":feature:other-theme-detail"))
    kover(project(":feature:other-volume-detail"))
    kover(project(":feature:other-feedback-detail"))
    kover(project(":feature:readout-list"))
    kover(project(":feature:lmu-windows-readout-vehicle-approach-detail"))
    kover(project(":feature:lmu-windows-readout-flag-detail"))
    kover(project(":feature:lmu-windows-readout-my-best-lap-detail"))
    kover(project(":feature:lmu-windows-readout-pit-timing-detail"))
    kover(project(":feature:lmu-windows-readout-vehicle-damage-detail"))
    kover(project(":feature:lmu-windows-readout-tyre-temperature-detail"))
    kover(project(":feature:lmu-windows-readout-remaining-virtual-energy-detail"))
    kover(project(":feature:lmu-windows-readout-tyre-wear-detail"))
    kover(project(":feature:gt7-ps5-connection"))
    kover(project(":feature:gt7-ps5-readout-my-best-lap-detail"))
    kover(project(":feature:gt7-ps5-readout-remaining-fuel-detail"))
    kover(project(":feature:gt7-ps5-readout-remaining-fuel-laps-detail"))
    kover(project(":feature:gt7-ps5-narrator"))
    kover(project(":feature:ace-windows-connection"))
    kover(project(":feature:ace-windows-narrator"))
    kover(project(":feature:ace-windows-readout-remaining-fuel-detail"))
    kover(project(":feature:ace-windows-readout-flag-detail"))
    kover(project(":feature:telemetry-log-list"))
    kover(project(":feature:telemetry-log-detail"))
    kover(project(":feature:debug-state-detail"))
    kover(project(":app:androidApp"))
    kover(project(":app:shared"))
    kover(project(":app:desktopApp"))
    kover(project(":server"))
    dokka(project(":core:domain"))
    dokka(project(":core:data"))
    dokka(project(":core:lmu-windows-data"))
    dokka(project(":core:gt7-ps5-data"))
    dokka(project(":core:ace-windows-data"))
    dokka(project(":core:windows-shared-memory"))
    dokka(project(":core:designsystem"))
    dokka(project(":core:narrator"))
    dokka(project(":feature:desktop-splash"))
    dokka(project(":feature:lmu-windows-connection"))
    dokka(project(":feature:main"))
    dokka(project(":feature:server-connection"))
    dokka(project(":feature:lmu-windows-narrator"))
    dokka(project(":feature:other-license-detail"))
    dokka(project(":feature:other-list"))
    dokka(project(":feature:other-server-ip-detail"))
    dokka(project(":feature:other-console-ip-detail"))
    dokka(project(":feature:other-readout-start-sound-detail"))
    dokka(project(":feature:other-theme-detail"))
    dokka(project(":feature:other-volume-detail"))
    dokka(project(":feature:other-feedback-detail"))
    dokka(project(":feature:readout-list"))
    dokka(project(":feature:lmu-windows-readout-vehicle-approach-detail"))
    dokka(project(":feature:lmu-windows-readout-flag-detail"))
    dokka(project(":feature:lmu-windows-readout-my-best-lap-detail"))
    dokka(project(":feature:lmu-windows-readout-pit-timing-detail"))
    dokka(project(":feature:lmu-windows-readout-vehicle-damage-detail"))
    dokka(project(":feature:lmu-windows-readout-tyre-temperature-detail"))
    dokka(project(":feature:lmu-windows-readout-remaining-virtual-energy-detail"))
    dokka(project(":feature:lmu-windows-readout-tyre-wear-detail"))
    dokka(project(":feature:gt7-ps5-connection"))
    dokka(project(":feature:gt7-ps5-readout-my-best-lap-detail"))
    dokka(project(":feature:gt7-ps5-readout-remaining-fuel-detail"))
    dokka(project(":feature:gt7-ps5-readout-remaining-fuel-laps-detail"))
    dokka(project(":feature:gt7-ps5-narrator"))
    dokka(project(":feature:ace-windows-connection"))
    dokka(project(":feature:ace-windows-narrator"))
    dokka(project(":feature:ace-windows-readout-remaining-fuel-detail"))
    dokka(project(":feature:ace-windows-readout-flag-detail"))
    dokka(project(":feature:telemetry-log-list"))
    dokka(project(":feature:telemetry-log-detail"))
    dokka(project(":feature:debug-state-detail"))
    dokka(project(":app:androidApp"))
    dokka(project(":app:shared"))
    dokka(project(":app:desktopApp"))
    dokka(project(":server"))
}
