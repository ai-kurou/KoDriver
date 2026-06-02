plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kover)
}

dependencies {
    implementation(projects.core.domain)
    implementation(libs.kotlinx.coroutinesCore)
    implementation(libs.koin.core)
    implementation(libs.jna)
    implementation(libs.jna.platform)
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.kotlinx.coroutinesTest)
}
