package kurou.kodriver.feature.manualreadoutsetdetail

internal actual fun isManualReadoutHotkeySupported(): Boolean = isWindowsOsName(System.getProperty("os.name"))
