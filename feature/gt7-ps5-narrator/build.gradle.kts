plugins {
    id("feature-compose")
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.gt7ps5narrator"
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.sentry)
        }
        jvmMain.dependencies {
            implementation(libs.sentry)
        }
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(libs.compose.runtime)
            implementation(libs.kotlinx.coroutinesCore)
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
        jsTest.dependencies {
            implementation(libs.kotlin.test)
        }
        wasmJsTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
        }
    }
}
