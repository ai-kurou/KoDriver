plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kover)
}

dependencies {
    implementation(projects.core.domain)
    implementation(libs.androidx.datastore.core)
    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.jna)
    implementation(libs.jna.platform)
    implementation(libs.kotlinx.coroutinesCore)
    implementation(libs.koin.core)

    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutinesTest)
}
