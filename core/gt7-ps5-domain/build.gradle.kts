plugins {
    id("kmp-library")
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    android {
        namespace = "kurou.kodriver.core.gt7ps5domain"
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutinesCore)
            implementation(project.dependencies.platform(libs.kotlinx.serialization.bom))
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        getByName("jvmTest") {
            dependencies {
                implementation(libs.kotlin.testJunit)
                implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
                implementation(libs.kotlinx.coroutinesTest)
                implementation(libs.mockk)
            }
        }
    }
}
