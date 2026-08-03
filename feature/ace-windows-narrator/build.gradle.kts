plugins {
    id("feature-compose")
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.acewindowsnarrator"
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
            implementation(libs.kotlinx.serialization.json)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.testJunit)
            implementation(libs.junit)
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.mockk)
        }
        androidUnitTest.dependencies {
            implementation(libs.kotlin.testJunit)
        }
    }
}

compose.resources {
    packageOfResClass = "kodriver.feature.acewindowsnarrator.generated.resources"
}
