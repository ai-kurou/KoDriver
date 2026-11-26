package kurou.kodriver.domain.util

/**
 * 手動読み上げのホットキーは Windows デスクトップのみ対応。
 */
public expect fun isManualReadoutHotkeySupported(): Boolean

internal fun isWindowsOsName(osName: String): Boolean = osName.lowercase().startsWith("windows")
