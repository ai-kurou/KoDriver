package kurou.kodriver.presentation

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Test
import kotlin.test.assertEquals

class ConnectionBannerTest {
    @Test
    fun `CONNECTED状態でメッセージが表示される`() =
        composeScreenshotTest {
            setContent {
                ConnectionBannerContent(
                    uiState =
                        ConnectionBannerUiState(
                            status = ConnectionBannerStatus.CONNECTED,
                            message = "接続済み",
                            iconType = ConnectionBannerIconType.NETWORK,
                        ),
                )
            }

            onNodeWithText("接続済み").assertIsDisplayed()
        }

    @Test
    fun `DISCONNECTED状態でメッセージが表示される`() =
        composeScreenshotTest {
            setContent {
                ConnectionBannerContent(
                    uiState =
                        ConnectionBannerUiState(
                            status = ConnectionBannerStatus.DISCONNECTED,
                            message = "切断中",
                            iconType = ConnectionBannerIconType.NETWORK,
                        ),
                )
            }

            onNodeWithText("切断中").assertIsDisplayed()
        }

    @Test
    fun `UNCHECKED状態でメッセージが表示される`() =
        composeScreenshotTest {
            setContent {
                ConnectionBannerContent(
                    uiState =
                        ConnectionBannerUiState(
                            status = ConnectionBannerStatus.UNCHECKED,
                            message = "確認中",
                            iconType = ConnectionBannerIconType.NETWORK,
                        ),
                )
            }

            onNodeWithText("確認中").assertIsDisplayed()
        }

    @Test
    fun `SIMULATORアイコンタイプかつCONNECTEDでメッセージが表示される`() =
        composeScreenshotTest {
            setContent {
                ConnectionBannerContent(
                    uiState =
                        ConnectionBannerUiState(
                            status = ConnectionBannerStatus.CONNECTED,
                            message = "LMU接続済み",
                            iconType = ConnectionBannerIconType.SIMULATOR,
                        ),
                )
            }

            onNodeWithText("LMU接続済み").assertIsDisplayed()
        }

    @Test
    fun `SIMULATORアイコンタイプかつDISCONNECTEDでメッセージが表示される`() =
        composeScreenshotTest {
            setContent {
                ConnectionBannerContent(
                    uiState =
                        ConnectionBannerUiState(
                            status = ConnectionBannerStatus.DISCONNECTED,
                            message = "LMU未接続",
                            iconType = ConnectionBannerIconType.SIMULATOR,
                        ),
                )
            }

            onNodeWithText("LMU未接続").assertIsDisplayed()
        }

    @Test
    fun `isTappableかつonClickがある場合にタップ可能でシェブロンが表示される`() =
        composeScreenshotTest {
            var clicked = false
            setContent {
                ConnectionBannerContent(
                    uiState =
                        ConnectionBannerUiState(
                            status = ConnectionBannerStatus.UNCHECKED,
                            message = "IPアドレスが未設定です",
                            iconType = ConnectionBannerIconType.NETWORK,
                            isTappable = true,
                        ),
                    onClick = { clicked = true },
                )
            }

            onNodeWithText("IPアドレスが未設定です").assertHasClickAction()
            onNodeWithText("IPアドレスが未設定です").performClick()
            assertEquals(true, clicked)
        }

    @Test
    fun `isTappableがfalseの場合はタップ不可でシェブロンが表示されない`() =
        composeScreenshotTest {
            setContent {
                ConnectionBannerContent(
                    uiState =
                        ConnectionBannerUiState(
                            status = ConnectionBannerStatus.UNCHECKED,
                            message = "確認中",
                            iconType = ConnectionBannerIconType.NETWORK,
                            isTappable = false,
                        ),
                    onClick = {},
                )
            }

            onNodeWithText("確認中").assertHasNoClickAction()
        }

    @Test
    fun `isTappableがtrueでもonClickがnullの場合はタップ不可`() =
        composeScreenshotTest {
            setContent {
                ConnectionBannerContent(
                    uiState =
                        ConnectionBannerUiState(
                            status = ConnectionBannerStatus.UNCHECKED,
                            message = "確認中",
                            iconType = ConnectionBannerIconType.NETWORK,
                            isTappable = true,
                        ),
                    onClick = null,
                )
            }

            onNodeWithText("確認中").assertHasNoClickAction()
        }

    @Test
    fun `pulseScaleはprogress0で最小値progress1で1になる`() =
        composeScreenshotTest {
            assertEquals(0.85f, pulseScale(0f))
            assertEquals(1f, pulseScale(1f))
        }

    @Test
    fun `pulseAlphaはprogress0で最小値progress1で1になる`() =
        composeScreenshotTest {
            assertEquals(0.45f, pulseAlpha(0f))
            assertEquals(1f, pulseAlpha(1f))
        }
}
