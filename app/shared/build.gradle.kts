import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kover)
}

kotlin {
    jvm()
    
    js {
        browser()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    
    androidLibrary {
       namespace = "kurou.kodriver.app.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()

       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       lint {
           abortOnError = true
           warningsAsErrors = false
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
        }
        jvmMain.dependencies {
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            api(projects.core)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material3.adaptive.layout)
            implementation(libs.compose.material3.adaptive.navigation)
            implementation(libs.compose.material3.adaptive.navigation.suite)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(compose.materialIconsExtended)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmTest.dependencies {
            implementation(libs.compose.uiTest)
            implementation(libs.compose.uiTestJunit4)
            implementation(libs.kotlin.testJunit)
            implementation(compose.desktop.currentOs)
            implementation(libs.roborazzi.composeDesktop)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

// recordRoborazziJvmTest / verifyRoborazziJvmTest のどちらで呼ばれたかを
// コンフィギュレーション時に判定してシステムプロパティに反映する
val startTaskNames = gradle.startParameter.taskNames
val isRecordMode = startTaskNames.any { it.contains("recordRoborazziJvmTest") }
val isVerifyMode = startTaskNames.any { it.contains("verifyRoborazziJvmTest") }

tasks.withType<Test>().configureEach {
    systemProperty("skiko.renderApi", "SOFTWARE_FAST")
    systemProperty("roborazzi.output.dir", "$projectDir/src/jvmTest/snapshots")
    if (isRecordMode) systemProperty("roborazzi.test.record", "true")
    if (isVerifyMode) systemProperty("roborazzi.test.verify", "true")
}

// recordRoborazziJvmTest: ゴールデン画像を更新する（画面変更後に実行してコミット）
tasks.register("recordRoborazziJvmTest") {
    group = "roborazzi"
    description = "スクリーンショットのゴールデン画像を更新する"
    dependsOn("jvmTest")
}

// verifyRoborazziJvmTest: ゴールデン画像と比較する（CI で使用）
tasks.register("verifyRoborazziJvmTest") {
    group = "roborazzi"
    description = "スクリーンショットをゴールデン画像と比較する"
    dependsOn("jvmTest")
}