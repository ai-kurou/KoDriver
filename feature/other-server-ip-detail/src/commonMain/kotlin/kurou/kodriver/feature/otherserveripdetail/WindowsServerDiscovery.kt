package kurou.kodriver.feature.otherserveripdetail

import kotlinx.coroutines.flow.Flow
import org.koin.core.module.Module

internal fun interface WindowsServerDiscovery {
    fun discover(): Flow<List<DiscoveredServer>>
}

internal expect val platformWindowsServerDiscoveryModule: Module
