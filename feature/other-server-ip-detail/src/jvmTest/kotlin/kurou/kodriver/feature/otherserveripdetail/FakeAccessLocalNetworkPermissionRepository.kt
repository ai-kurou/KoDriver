package kurou.kodriver.feature.otherserveripdetail

import kurou.kodriver.domain.repository.AccessLocalNetworkPermissionRepository

/** テスト用の権限Repository。[granted] を書き換えて許可状態の変化を再現する。 */
internal class FakeAccessLocalNetworkPermissionRepository(
    var granted: Boolean = true,
) : AccessLocalNetworkPermissionRepository {
    override fun isGranted(): Boolean = granted
}
