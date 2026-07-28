plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kover)
}

dependencies {
    implementation(libs.jna)
    implementation(libs.jna.platform)
    implementation(libs.sentry)
    implementation(libs.kotlinx.coroutinesCore)

    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutinesTest)
}
