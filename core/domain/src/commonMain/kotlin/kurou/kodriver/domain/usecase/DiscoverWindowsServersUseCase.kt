package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.DiscoveredServer
import kurou.kodriver.domain.repository.WindowsServerDiscoveryRepository

public class DiscoverWindowsServersUseCase(
    private val repository: WindowsServerDiscoveryRepository,
) {
    public operator fun invoke(): Flow<List<DiscoveredServer>> = repository.discover()
}
