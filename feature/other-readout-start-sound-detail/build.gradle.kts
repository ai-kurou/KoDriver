plugins {
    id("feature-kmp")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidLibrary {
        namespace = "kurou.kodriver.feature.otherreadoutstartsounddetail"
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

        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
        }
        jvmMain.dependencies {
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.testJunit)
            implementation(libs.kotlinx.coroutinesTest)
        }
    }
}

compose.resources {
    packageOfResClass = "kodriver.feature.otherreadoutstartsounddetail.generated.resources"
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
