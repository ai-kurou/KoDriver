plugins {
    id("feature-kmp")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

private val libs = versionCatalogs.named("libs")

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
