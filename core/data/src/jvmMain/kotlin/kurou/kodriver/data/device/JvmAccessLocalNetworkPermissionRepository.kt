package kurou.kodriver.data.device

import kurou.kodriver.domain.repository.AccessLocalNetworkPermissionRepository

internal class JvmAccessLocalNetworkPermissionRepository : AccessLocalNetworkPermissionRepository {
    override fun isGranted(): Boolean = true
}
