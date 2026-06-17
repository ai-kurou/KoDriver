package kurou.kodriver.presentation

actual fun currentAppVersion(): String =
    AppVersion::class.java.`package`?.implementationVersion ?: ""

private object AppVersion
