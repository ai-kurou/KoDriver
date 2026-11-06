plugins {
    id("kmp-library")
}

kotlin {
    android {
        namespace = "kurou.kodriver.core.speechrecognitiondata"
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutinesCore)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(project.dependencies.platform(libs.kotlinx.serialization.bom))
            implementation(libs.kotlinx.serialization.json)
        }
        jvmMain.dependencies {
            implementation(libs.vosk)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.vosk.android)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.testJunit)
            implementation(libs.junit)
            implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.mockk)
        }
        named("androidHostTest") {
            dependencies {
                implementation(libs.kotlin.testJunit)
                implementation(libs.junit)
                implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
                implementation(libs.kotlinx.coroutinesTest)
                implementation(libs.robolectric)
                implementation(libs.androidx.testExt.junit)
                implementation(libs.mockk)
            }
        }
    }
}
