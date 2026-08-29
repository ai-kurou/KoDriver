package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.AccessLocalNetworkPermissionRepository

class CheckAccessLocalNetworkPermissionGrantedUseCase(
    private val repository: AccessLocalNetworkPermissionRepository,
) {
    operator fun invoke(): Boolean = repository.isGranted()
}
