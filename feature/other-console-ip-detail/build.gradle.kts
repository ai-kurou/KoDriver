import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    id("feature-compose-screenshot")
    id("io.github.takahirom.roborazzi")
    `java-test-fixtures`
}

// ComposablePreviewScanner は JVM 17 のメタデータ/バイトコードで公開されているため、
// この feature モジュールの jvm ターゲットのみ JVM 17 に引き上げる
// (feature-kmp.gradle.kts で全体設定は変更しない、試験導入モジュール限定の対応)。
tasks.withType<KotlinCompilationTask<*>>().configureEach {
    if (name.contains("Jvm", ignoreCase = true)) {
        compilerOptions {
            if (this is org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions) {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }
    }
}
afterEvaluate {
    listOf("jvmTestCompileClasspath", "jvmTestRuntimeClasspath").forEach { configurationName ->
        configurations.findByName(configurationName)?.attributes {
            attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 17)
        }
    }
}

// jvm() (Desktop) ターゲット向けの Preview スキャン設定。
// Roborazzi Gradle プラグインを com.android.kotlin.multiplatform.library モジュールに適用すると
// src/androidHostTest の Compose スクリーンショットテストがコンパイルエラーになるため
// (docs/testing-guidelines.md 参照)、androidHostTest は追加せず jvm() 側のみで運用する。
@OptIn(com.github.takahirom.roborazzi.ExperimentalRoborazziApi::class)
roborazzi {
    generateComposePreviewDesktopTests {
        enable = true
        packages = listOf("kurou.kodriver.feature.otherconsoleipdetail")
        // 既存の @Preview は既存パターンに合わせて private fun で定義しているため有効化する
        includePrivatePreviews = true
    }
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.otherconsoleipdetail"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(libs.compose.material.icons.extended)
        }
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.sentry.bom))
            implementation(libs.sentry)
        }
        jvmMain.dependencies {
            implementation(project.dependencies.platform(libs.sentry.bom))
            implementation(libs.sentry)
        }
        jvmTest.dependencies {
            implementation(libs.roborazzi.composeDesktopPreviewScannerSupport)
            implementation(libs.composablePreviewScanner)
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.otherconsoleipdetail.generated.resources"
}

dependencies {
    testFixturesApi(projects.core.domain)
    testFixturesImplementation(platform(libs.koin.bom))
    testFixturesImplementation(libs.koin.core)
    testFixturesImplementation(platform(libs.kotlinx.coroutines.bom))
    testFixturesImplementation(libs.kotlinx.coroutinesCore)
}
