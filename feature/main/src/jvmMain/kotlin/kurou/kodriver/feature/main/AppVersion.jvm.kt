package kurou.kodriver.feature.main

actual fun currentAppVersion(): String =
    AppVersion::class.java.`package`?.implementationVersion ?: ""

private object AppVersion
