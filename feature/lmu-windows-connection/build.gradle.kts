plugins {
    id("feature-kmp")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.lmuwindowsconnection"
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutinesCore)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutinesTest)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.testJunit)
        }
    }
}
