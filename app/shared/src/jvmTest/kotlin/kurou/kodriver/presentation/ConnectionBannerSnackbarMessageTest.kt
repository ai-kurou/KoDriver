package kurou.kodriver.presentation

import kurou.kodriver.app.shared.generated.resources.Res
import kurou.kodriver.app.shared.generated.resources.ace_connected
import kurou.kodriver.app.shared.generated.resources.ace_disconnected
import kurou.kodriver.app.shared.generated.resources.gt7_connected
import kurou.kodriver.app.shared.generated.resources.gt7_disconnected
import kurou.kodriver.app.shared.generated.resources.lmu_connected
import kurou.kodriver.app.shared.generated.resources.lmu_disconnected
import org.junit.Test
import kotlin.test.assertEquals

class ConnectionBannerSnackbarMessageTest {
    @Test
    fun `GT7選択時はGT7用の接続文言を返す`() {
        assertEquals(
            Res.string.gt7_connected,
            connectionBannerSnackbarConnectedMessageRes(isGt7 = true, isAceWindows = false),
        )
        assertEquals(
            Res.string.gt7_disconnected,
            connectionBannerSnackbarDisconnectedMessageRes(isGt7 = true, isAceWindows = false),
        )
    }

    @Test
    fun `ACE選択時はACE用の接続文言を返す`() {
        assertEquals(
            Res.string.ace_connected,
            connectionBannerSnackbarConnectedMessageRes(isGt7 = false, isAceWindows = true),
        )
        assertEquals(
            Res.string.ace_disconnected,
            connectionBannerSnackbarDisconnectedMessageRes(isGt7 = false, isAceWindows = true),
        )
    }

    @Test
    fun `LMU選択時はLMU用の接続文言を返す`() {
        assertEquals(
            Res.string.lmu_connected,
            connectionBannerSnackbarConnectedMessageRes(isGt7 = false, isAceWindows = false),
        )
        assertEquals(
            Res.string.lmu_disconnected,
            connectionBannerSnackbarDisconnectedMessageRes(isGt7 = false, isAceWindows = false),
        )
    }
}
