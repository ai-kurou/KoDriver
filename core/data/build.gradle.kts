plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kover)
}

dependencies {
    implementation(projects.core.domain)
    implementation(libs.kotlinx.coroutinesCore)
    implementation(libs.jna)
    implementation(libs.jna.platform)

    testImplementation(libs.kotlin.testJunit)
}
