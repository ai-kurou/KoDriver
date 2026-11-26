package kurou.kodriver.data.device

import kurou.kodriver.domain.serverconnection.repository.AccessLocalNetworkPermissionRepository

internal class JvmAccessLocalNetworkPermissionRepository : AccessLocalNetworkPermissionRepository {
    override fun isGranted(): Boolean = true
}
