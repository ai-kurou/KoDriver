plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kover)
}

dependencies {
    implementation(projects.core.domain)
}
