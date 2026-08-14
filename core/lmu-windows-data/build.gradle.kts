plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kover)
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.windowsSharedMemory)
    implementation(platform(libs.kotlinx.coroutines.bom))
    implementation(libs.kotlinx.coroutinesCore)
    implementation(libs.kotlinx.datetime)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)

    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.junit)
    testImplementation(platform(libs.kotlinx.coroutines.bom))
    testImplementation(libs.kotlinx.coroutinesTest)
}
