plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kover)
}

dependencies {
    implementation(projects.core.domain)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.jna)
    implementation(libs.jna.platform)
    implementation(platform(libs.sentry.bom))
    implementation(libs.sentry)
    implementation(platform(libs.kotlinx.coroutines.bom))
    implementation(libs.kotlinx.coroutinesCore)

    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.junit)
    testImplementation(platform(libs.kotlinx.coroutines.bom))
    testImplementation(libs.kotlinx.coroutinesTest)
}

kover {
    reports {
        filters {
            excludes {
                classes("kurou.kodriver.core.windowshotkeydata.JnaHotkeyNativeApi")
            }
        }
    }
}
