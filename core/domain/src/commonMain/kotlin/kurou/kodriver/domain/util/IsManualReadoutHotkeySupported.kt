package kurou.kodriver.domain.util

/**
 * 手動読み上げのホットキーは Windows デスクトップのみ対応。
 */
expect fun isManualReadoutHotkeySupported(): Boolean

fun isWindowsOsName(osName: String): Boolean = osName.lowercase().startsWith("windows")
