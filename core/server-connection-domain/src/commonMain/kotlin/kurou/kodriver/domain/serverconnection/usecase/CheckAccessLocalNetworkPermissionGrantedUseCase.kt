package kurou.kodriver.domain.serverconnection.usecase

import kurou.kodriver.domain.serverconnection.repository.AccessLocalNetworkPermissionRepository

public class CheckAccessLocalNetworkPermissionGrantedUseCase(
    private val repository: AccessLocalNetworkPermissionRepository,
) {
    public operator fun invoke(): Boolean = repository.isGranted()
}
