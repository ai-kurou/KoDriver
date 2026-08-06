package kurou.kodriver.presentation

import androidx.compose.runtime.mutableStateOf
import kurou.kodriver.feature.otherlist.OtherListItemType
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppScreenEffectsTest {
    @Test
    fun `タブ再選択時に選択中の項目がある場合は選択を解除する`() =
        composeScreenshotTest {
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
    fun `タブ再選択時に選択中の項目がない場合は先頭スクロールを要求する`() =
        composeScreenshotTest {
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
    fun `接続バナーがタップ可能な場合は対応するその他項目を選択する`() =
        composeScreenshotTest {
            val bannerUiState =
                mutableStateOf(
                    ConnectionBannerUiState(
                        isTappable = true,
                        tapNavigationTarget = ConnectionBannerNavigationTarget.ServerIp,
                    ),
                )
            var selectedItemType: OtherListItemType? = null
            var onBannerTap: (() -> Unit)? = null

            setContent {
                onBannerTap =
                    rememberConnectionBannerTap(
                        bannerUiState = bannerUiState.value,
                        onSelectOtherItem = { selectedItemType = it },
                    )
            }

            onBannerTap?.invoke()
            waitForIdle()

            assertEquals(OtherListItemType.ServerIp, selectedItemType)
        }

    @Test
    fun `接続バナーがタップ不可の場合はタップ処理を作らない`() =
        composeScreenshotTest {
            var onBannerTap: (() -> Unit)? = {}

            setContent {
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

            waitForIdle()

            assertNull(onBannerTap)
        }
}
