plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kover)
}

dependencies {
    implementation(libs.jna)
    implementation(libs.jna.platform)
    implementation(platform(libs.sentry.bom))
    implementation(libs.sentry)
    implementation(platform(libs.kotlinx.coroutines.bom))
    implementation(libs.kotlinx.coroutinesCore)

    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(platform(libs.kotlinx.coroutines.bom))
}

val testArtifacts: Configuration by configurations.creating

val testJar by tasks.registering(Jar::class) {
    archiveClassifier.set("test")
    from(sourceSets["test"].output)
}

artifacts {
    add("testArtifacts", testJar)
}
