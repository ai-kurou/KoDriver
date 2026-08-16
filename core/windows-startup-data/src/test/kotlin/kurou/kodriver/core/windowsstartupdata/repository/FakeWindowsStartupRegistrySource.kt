package kurou.kodriver.core.windowsstartupdata.repository

import kurou.kodriver.core.windowsstartupdata.windows.WindowsStartupRegistrySource

class FakeWindowsStartupRegistrySource(
    private var registered: Boolean = false,
) : WindowsStartupRegistrySource {
    override fun isRegistered(): Boolean = registered

    override fun register() {
        registered = true
    }

    override fun unregister() {
        registered = false
    }
}
