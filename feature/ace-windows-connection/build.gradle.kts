plugins {
    id("feature-kmp")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.acewindowsconnection"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutinesCore)
        }
        jvmTest.dependencies {
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.kotlin.testJunit)
            implementation(libs.mockk)
        }
    }
}
