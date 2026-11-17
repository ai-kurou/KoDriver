package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.AccessLocalNetworkPermissionRepository

public class CheckAccessLocalNetworkPermissionGrantedUseCase(
    private val repository: AccessLocalNetworkPermissionRepository,
) {
    public operator fun invoke(): Boolean = repository.isGranted()
}
