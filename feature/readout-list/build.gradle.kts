plugins {
    id("feature-compose-screenshot")
    `java-test-fixtures`
}

kotlin {
    android {
        namespace = "kurou.kodriver.feature.readoutlist"
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(libs.compose.material3.adaptive.layout)
            implementation(libs.compose.material3.adaptive.navigation)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.reorderable)
            implementation(libs.navigation3.ui)
        }
        jvmTest.dependencies {
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.mockk)
        }
        jvmTest {
            kotlin.srcDir(
                rootProject.layout.projectDirectory.dir("build-logic/src/paneDirectiveJvmTest/kotlin"),
            )
        }
    }
}

compose.resources {
    packageOfResClass = "kurou.kodriver.feature.readoutlist.generated.resources"
}

dependencies {
    testFixturesImplementation(projects.core.domain)
    testFixturesImplementation(libs.koin.core)
    testFixturesImplementation(libs.kotlinx.coroutinesCore)
}
