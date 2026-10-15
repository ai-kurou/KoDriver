package kurou.kodriver.feature.manualreadoutlist

internal actual fun isManualReadoutHotkeySupported(): Boolean = isWindowsOsName(System.getProperty("os.name"))
