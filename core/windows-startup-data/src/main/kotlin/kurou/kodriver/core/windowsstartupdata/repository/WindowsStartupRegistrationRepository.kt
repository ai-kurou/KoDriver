package kurou.kodriver.core.windowsstartupdata.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kurou.kodriver.core.windowsstartupdata.windows.RegistryStartupRegistrySource
import kurou.kodriver.core.windowsstartupdata.windows.WindowsStartupRegistrySource
import kurou.kodriver.domain.repository.StartupRegistrationRepository

internal class WindowsStartupRegistrationRepository(
    private val source: WindowsStartupRegistrySource = RegistryStartupRegistrySource(),
) : StartupRegistrationRepository {
    override suspend fun isEnabled(): Boolean =
        withContext(Dispatchers.IO) {
            source.isRegistered()
        }

    override suspend fun setEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            if (enabled) source.register() else source.unregister()
        }
    }
}
