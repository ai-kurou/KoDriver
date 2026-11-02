plugins {
    id("feature-compose-screenshot")
    `java-test-fixtures`
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.othervoicedetail"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(libs.compose.material.icons.extended)
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.othervoicedetail.generated.resources"
}

dependencies {
    testFixturesApi(projects.core.domain)
    testFixturesImplementation(platform(libs.koin.bom))
    testFixturesImplementation(libs.koin.core)
    testFixturesImplementation(platform(libs.kotlinx.coroutines.bom))
    testFixturesImplementation(libs.kotlinx.coroutinesCore)
}
