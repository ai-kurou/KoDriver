package kurou.kodriver.presentation

import org.junit.Test
import kotlin.test.assertEquals

class ConnectionBannerNavigationTest {
    @Test
    fun `Android版LMUではサーバーIP設定へ遷移する`() {
        assertEquals(
            ConnectionBannerNavigationTarget.ServerIp,
            connectionBannerNavigationTarget(
                isGt7 = false,
                supportsLmuServerIpNavigation = true,
            ),
        )
    }

    @Test
    fun `Android版GT7ではコンソールIP設定へ遷移する`() {
        assertEquals(
            ConnectionBannerNavigationTarget.ConsoleIp,
            connectionBannerNavigationTarget(
                isGt7 = true,
                supportsLmuServerIpNavigation = true,
            ),
        )
    }

    @Test
    fun `Desktop版LMUでは画面遷移しない`() {
        assertEquals(
            null,
            connectionBannerNavigationTarget(
                isGt7 = false,
                supportsLmuServerIpNavigation = false,
            ),
        )
    }

    @Test
    fun `Desktop版GT7ではコンソールIP設定へ遷移する`() {
        assertEquals(
            ConnectionBannerNavigationTarget.ConsoleIp,
            connectionBannerNavigationTarget(
                isGt7 = true,
                supportsLmuServerIpNavigation = false,
            ),
        )
    }
}
