plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kover)
}

group = "kurou.kodriver"
version = "1.0.0"
application {
    mainClass = "kurou.kodriver.ApplicationKt"
}

dependencies {
    api(projects.core.domain)
    implementation(libs.logback)
    implementation(libs.koin.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverWebsockets)
    testImplementation(libs.ktor.clientWebsockets)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlinx.coroutinesTest)
    testImplementation(libs.kotlin.testJunit)
}
