package kurou.kodriver.domain.serverconnection.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.serverconnection.model.DiscoveredServer
import kurou.kodriver.domain.serverconnection.repository.WindowsServerDiscoveryRepository

public class DiscoverWindowsServersUseCase(
    private val repository: WindowsServerDiscoveryRepository,
) {
    public operator fun invoke(): Flow<List<DiscoveredServer>> = repository.discover()
}
