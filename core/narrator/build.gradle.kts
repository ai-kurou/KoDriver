plugins {
    id("kmp-library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    android {
        namespace = "kurou.kodriver.core.narrator"
        androidResources {
            enable = true
        }

        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.components.resources)
            implementation(libs.compose.runtime)
            implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutinesCore)
            implementation(project.dependencies.platform(libs.kotlinx.serialization.bom))
            implementation(libs.kotlinx.serialization.json)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.sentry.bom))
            implementation(libs.sentry)
        }
        jvmMain.dependencies {
            implementation(project.dependencies.platform(libs.sentry.bom))
            implementation(libs.sentry)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
            implementation(libs.kotlin.testJunit)
            implementation(libs.junit)
            implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutinesTest)
        }
        named("androidHostTest") {
            dependencies {
                implementation(libs.kotlin.testJunit)
                implementation(libs.junit)
                implementation(libs.mockk)
                implementation(libs.robolectric)
                implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
                implementation(libs.kotlinx.coroutinesTest)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.core.narrator.generated.resources"
    publicResClass = true
}
