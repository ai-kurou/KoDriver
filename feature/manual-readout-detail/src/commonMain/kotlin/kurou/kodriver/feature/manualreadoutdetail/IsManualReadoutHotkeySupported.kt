package kurou.kodriver.feature.manualreadoutdetail

/**
 * 手動読み上げのホットキーは Windows デスクトップのみ対応。
 */
internal expect fun isManualReadoutHotkeySupported(): Boolean

internal fun isWindowsOsName(osName: String): Boolean = osName.lowercase().startsWith("windows")
