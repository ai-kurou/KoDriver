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
            implementation(projects.core.designsystem)
            implementation(libs.kotlinx.coroutinesCore)
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

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.gt7ps5narrator.generated.resources"
}
