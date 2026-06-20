plugins {
    id("feature-kmp")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    `java-test-fixtures`
}

kotlin {
    androidLibrary {
        namespace = "kurou.kodriver.feature.lmuwindowsnarrator"
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
            implementation(libs.sentry)
        }
        jvmMain.dependencies {
            implementation(libs.compose.uiTooling)
            implementation(libs.sentry)
        }
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(libs.compose.runtime)
            implementation(libs.compose.components.resources)
            implementation(libs.kotlinx.coroutinesCore)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.koin.compose.viewmodel)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.testJunit)
            implementation(libs.junit)
            implementation(libs.kotlinx.coroutinesTest)
        }
        androidUnitTest.dependencies {
            implementation(libs.kotlin.testJunit)
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.lmuwindowsnarrator.generated.resources"
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
    testFixturesImplementation(projects.core.domain)
    testFixturesImplementation(libs.koin.core)
}
