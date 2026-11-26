package kurou.kodriver.domain.util

public actual fun isManualReadoutHotkeySupported(): Boolean = isWindowsOsName(System.getProperty("os.name"))
