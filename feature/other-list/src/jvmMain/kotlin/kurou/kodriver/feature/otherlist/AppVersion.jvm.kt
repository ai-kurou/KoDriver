package kurou.kodriver.feature.otherlist

actual fun currentAppVersion(): String =
    AppVersion::class.java.`package`?.implementationVersion ?: ""

private object AppVersion
