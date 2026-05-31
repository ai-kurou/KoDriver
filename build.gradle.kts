plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.modulesGraphAssert)
}

val isCI = System.getenv("CI") != null

detekt {
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = !isCI
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        autoCorrect = !isCI
    }
    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        ignoreFailures = false
    }
    dependencies {
        "detektPlugins"(rootProject.libs.detekt.formatting)
    }
}

moduleGraphAssert {
    maxHeight = 2
    allowed = arrayOf(
        ":app:androidApp -> :app:shared",
        ":app:desktopApp -> :app:shared",
        ":app:webApp -> :app:shared",
        ":app:shared -> :core",
        ":server -> :core",
    )
    restricted = arrayOf(
        ":core -X> :app:.*",
        ":core -X> :server",
        ":app:shared -X> :app:androidApp",
        ":app:shared -X> :app:desktopApp",
        ":app:shared -X> :app:webApp",
        ":app:shared -X> :server",
        ":server -X> :app:.*",
    )
}

tasks.register("generateModuleGraphImages") {
    group = "documentation"
    description = "Generates SVG module dependency graphs and updates each module's README.md"
    notCompatibleWithConfigurationCache("Uses Project references at execution time")

    doLast {
        val dotBinary = listOf("/opt/homebrew/bin/dot", "/usr/bin/dot", "/usr/local/bin/dot")
            .firstOrNull { File(it).exists() }
            ?: error("Graphviz 'dot' not found. Install: brew install graphviz (Mac) / apt-get install graphviz (Linux)")

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

        val fullGvFile = file("$graphsDir/full-graph.gv")
        runCommand("./gradlew", "generateModulesGraphvizText",
            "-Pmodules.graph.output.gv=${fullGvFile.absolutePath}")

        val fullGv = fullGvFile.readText()
        val edgeRegex = Regex(""""(:[^"]+)"\s*->\s*"(:[^"]+)"(.*)""")
        val parsedEdges = edgeRegex.findAll(fullGv).map { m ->
            Triple(m.groupValues[1], m.groupValues[2], m.groupValues[3].trim())
        }.toList()

        runCommand(dotBinary, "-Tsvg", fullGvFile.absolutePath, "-o", "$graphsDir/full-graph.svg")
        println("Generated: docs/graphs/full-graph.svg")

        val allModules = (parsedEdges.map { it.first } + parsedEdges.map { it.second }).toSet()
        allModules.forEach { module ->
            val neighborhood = parsedEdges
                .filter { (f, t, _) -> f == module || t == module }
                .flatMap { (f, t, _) -> listOf(f, t) }
                .toSet()
            val subEdges = parsedEdges.filter { (f, t, _) -> f in neighborhood && t in neighborhood }

            val gvContent = buildString {
                appendLine("digraph G {")
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
                        block
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

dependencies {
    kover(project(":core"))
    kover(project(":app:shared"))
    kover(project(":app:desktopApp"))
    kover(project(":server"))
}