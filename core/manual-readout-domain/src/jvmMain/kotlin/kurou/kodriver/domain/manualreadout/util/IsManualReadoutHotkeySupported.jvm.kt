package kurou.kodriver.domain.manualreadout.util

public actual fun isManualReadoutHotkeySupported(): Boolean = isWindowsOsName(System.getProperty("os.name"))
