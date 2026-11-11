package kurou.kodriver.presentation

import kurou.kodriver.feature.otherlist.OtherListItemType
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ConnectionBannerAccessLocalNetworkPermissionTest {
    private val connected =
        ConnectionBannerUiState(
            status = ConnectionBannerStatus.CONNECTED,
            message = "接続中",
            iconType = ConnectionBannerIconType.SIMULATOR,
        )

    @Test
    fun `権限が許可済みの場合は表示状態をそのまま返す`() {
        val result = connected.withAccessLocalNetworkPermission(granted = true, requiredMessage = "許可が必要")

        assertSame(connected, result)
    }

    @Test
    fun `権限が未許可の場合は権限が必要な案内に差し替えタップで権限を要求できるようにする`() {
        val result = connected.withAccessLocalNetworkPermission(granted = false, requiredMessage = "許可が必要")

        assertEquals(
            ConnectionBannerUiState(
                status = ConnectionBannerStatus.UNCHECKED,
                message = "許可が必要",
                iconType = ConnectionBannerIconType.NETWORK,
                isTappable = true,
                tapNavigationTarget = ConnectionBannerNavigationTarget.AccessLocalNetworkPermission,
            ),
            result,
        )
    }

    @Test
    fun `ローカルネットワーク権限の遷移先はその他タブの権限項目に対応する`() {
        assertEquals(
            OtherListItemType.AccessLocalNetworkPermission,
            ConnectionBannerNavigationTarget.AccessLocalNetworkPermission.toOtherListItemType(),
        )
    }
}
