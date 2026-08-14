plugins {
    id("feature-compose")
    alias(libs.plugins.kotlinxSerialization)
    `java-test-fixtures`
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.gt7ps5narrator"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(projects.core.narrator)
            implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutinesCore)
            implementation(libs.kotlinx.serialization.json)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.testJunit)
            implementation(libs.junit)
            implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.mockk)
        }
        androidUnitTest.dependencies {
            implementation(libs.kotlin.testJunit)
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.gt7ps5narrator.generated.resources"
}

dependencies {
    testFixturesImplementation(projects.core.domain)
    testFixturesImplementation(libs.koin.core)
}
