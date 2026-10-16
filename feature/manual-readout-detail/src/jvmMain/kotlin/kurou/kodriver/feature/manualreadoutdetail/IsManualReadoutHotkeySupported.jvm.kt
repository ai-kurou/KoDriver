package kurou.kodriver.feature.manualreadoutdetail

internal actual fun isManualReadoutHotkeySupported(): Boolean = isWindowsOsName(System.getProperty("os.name"))
