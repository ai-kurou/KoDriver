plugins {
    id("feature-kmp")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.github.skydoves.compose.stability.analyzer")
}

private val libs = versionCatalogs.named("libs")

composeStabilityAnalyzer {
    stabilityValidation {
        failOnStabilityChange.set(true)
    }
}

// stabilityCheck/stabilityDump は各ターゲットのコンパイル成果物(build/stability/<compileTask>/)を
// まとめて読むが、個々のコンパイルタスクへの依存を宣言していないため、
// 並列ビルドでは暗黙的依存の検証エラーになる。明示的に実行順序を固定する。
tasks.matching { it.name == "stabilityCheck" || it.name == "stabilityDump" }.configureEach {
    mustRunAfter(tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>())
}

kotlin {
    android {
        androidResources {
            enable = true
        }
    }

    sourceSets {
        val nonAndroidMain by creating {
            dependsOn(commonMain.get())
        }
        jvmMain.get().dependsOn(nonAndroidMain)
        jsMain.get().dependsOn(nonAndroidMain)
        wasmJsMain.get().dependsOn(nonAndroidMain)

        commonMain.dependencies {
            implementation(libs.findLibrary("compose-runtime").get())
            implementation(libs.findLibrary("compose-components-resources").get())
        }
        androidMain.dependencies {
            implementation(libs.findLibrary("compose-uiToolingPreview").get())
        }
        jvmMain.dependencies {
            implementation(libs.findLibrary("compose-uiTooling").get())
        }
    }
}

dependencies {
    "androidRuntimeClasspath"(libs.findLibrary("compose-uiTooling").get())
}
