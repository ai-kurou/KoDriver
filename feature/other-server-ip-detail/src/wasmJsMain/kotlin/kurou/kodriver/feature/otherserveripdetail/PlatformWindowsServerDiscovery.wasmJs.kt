package kurou.kodriver.feature.otherserveripdetail

import kotlinx.coroutines.flow.emptyFlow
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformWindowsServerDiscoveryModule: Module =
    module {
    factory<WindowsServerDiscovery> { WindowsServerDiscovery { emptyFlow() } }
}
