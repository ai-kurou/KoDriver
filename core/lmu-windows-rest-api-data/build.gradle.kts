plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kover)
}

dependencies {
    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.junit)
    testImplementation(platform(libs.kotlinx.coroutines.bom))
    testImplementation(libs.kotlinx.coroutinesTest)
}
