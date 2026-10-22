plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kover)
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.lmuWindowsDomain)
    implementation(platform(libs.ktor.bom))
    implementation(libs.ktor.clientCore)
    implementation(libs.ktor.clientOkhttp)
    implementation(platform(libs.kotlinx.serialization.bom))
    implementation(libs.kotlinx.serialization.json)
    implementation(platform(libs.kotlinx.coroutines.bom))
    implementation(libs.kotlinx.coroutinesCore)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)

    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.junit)
    testImplementation(platform(libs.kotlinx.coroutines.bom))
    testImplementation(libs.kotlinx.coroutinesTest)
    testImplementation(platform(libs.ktor.bom))
    testImplementation(libs.ktor.clientMock)
}
