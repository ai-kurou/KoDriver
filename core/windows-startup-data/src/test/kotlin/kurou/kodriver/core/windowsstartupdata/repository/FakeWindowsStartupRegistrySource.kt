package kurou.kodriver.core.windowsstartupdata.repository

import kurou.kodriver.core.windowsstartupdata.windows.WindowsStartupRegistrySource

class FakeWindowsStartupRegistrySource(
    private var registered: Boolean = false,
    private val disabledByWindows: Boolean = false,
) : WindowsStartupRegistrySource {
    override fun isRegistered(): Boolean = registered

    override fun isDisabledByWindows(): Boolean = disabledByWindows

    override fun register() {
        registered = true
    }

    override fun unregister() {
        registered = false
    }
}
