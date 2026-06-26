plugins {
    id("feature-compose")
    `java-test-fixtures`
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.lmuwindowsnarrator"
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
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.lmuwindowsnarrator.generated.resources"
}

dependencies {
    testFixturesImplementation(projects.core.domain)
    testFixturesImplementation(libs.koin.core)
}
