package kurou.kodriver.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createComposeRule
import kurou.kodriver.feature.otherlist.OtherListItemType
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppScreenEffectsTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タブ再選択時に選択中の項目がある場合は選択を解除する`() {
        var clearSelectedItemCount = 0
        var requestScrollToTopCount = 0

        handleTabReselected(
            selectedItem = "selected",
            clearSelectedItem = { clearSelectedItemCount++ },
            requestScrollToTop = { requestScrollToTopCount++ },
        )

        assertEquals(1, clearSelectedItemCount)
        assertEquals(0, requestScrollToTopCount)
    }

    @Test
    fun `タブ再選択時に選択中の項目がない場合は先頭スクロールを要求する`() {
        var clearSelectedItemCount = 0
        var requestScrollToTopCount = 0

        handleTabReselected(
            selectedItem = null,
            clearSelectedItem = { clearSelectedItemCount++ },
            requestScrollToTop = { requestScrollToTopCount++ },
        )

        assertEquals(0, clearSelectedItemCount)
        assertEquals(1, requestScrollToTopCount)
    }

    @Test
    fun `接続バナーがタップ可能な場合は対応するその他項目を選択する`() {
        val bannerUiState =
            mutableStateOf(
                ConnectionBannerUiState(
                    isTappable = true,
                    tapNavigationTarget = ConnectionBannerNavigationTarget.ServerIp,
                ),
            )
        var selectedItemType: OtherListItemType? = null
        var onBannerTap: (() -> Unit)? = null

        rule.setContent {
            onBannerTap =
                rememberConnectionBannerTap(
                    bannerUiState = bannerUiState.value,
                    onSelectOtherItem = { selectedItemType = it },
                )
        }

        onBannerTap?.invoke()
        rule.waitForIdle()

        assertEquals(OtherListItemType.ServerIp, selectedItemType)
    }

    @Test
    fun `接続バナーがタップ不可の場合はタップ処理を作らない`() {
        var onBannerTap: (() -> Unit)? = {}

        rule.setContent {
            onBannerTap =
                rememberConnectionBannerTap(
                    bannerUiState =
                        ConnectionBannerUiState(
                            isTappable = false,
                            tapNavigationTarget = ConnectionBannerNavigationTarget.ConsoleIp,
                        ),
                    onSelectOtherItem = {},
                )
        }

        rule.waitForIdle()

        assertNull(onBannerTap)
    }
}
