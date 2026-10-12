package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFlagType
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class FlagInfoContentTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `ACEでaceWindowsFlagがnullの場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState =
                        AceWindowsDebugStateDetailUiState(
                            cardOrder = listOf(AceWindowsDebugStateCardKey.FLAG_INFO),
                            aceWindows =
                                AceWindowsDebugState(
                                    flag = null,
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `ACEの場合はフラッグの表示名を表示する`() {
        val expected =
            mapOf(
                AceWindowsFlagType.NO_FLAG to "フラッグなし",
                AceWindowsFlagType.WHITE_FLAG to "ホワイトフラッグ",
                AceWindowsFlagType.GREEN_FLAG to "グリーンフラッグ",
                AceWindowsFlagType.RED_FLAG to "レッドフラッグ",
                AceWindowsFlagType.BLUE_FLAG to "ブルーフラッグ",
                AceWindowsFlagType.YELLOW_FLAG to "イエローフラッグ",
                AceWindowsFlagType.BLACK_FLAG to "ブラックフラッグ",
                AceWindowsFlagType.BLACK_WHITE_FLAG to "ブラック・ホワイトフラッグ",
                AceWindowsFlagType.CHECKERED_FLAG to "チェッカーフラッグ",
                AceWindowsFlagType.ORANGE_CIRCLE_FLAG to "オレンジボールフラッグ",
                AceWindowsFlagType.RED_YELLOW_STRIPES_FLAG to "レッド・イエローストライプフラッグ",
                AceWindowsFlagType.UNKNOWN to "未取得",
            )

        expected.forEach { (flag, displayName) ->
            rule.setContent {
                MaterialTheme {
                    AceWindowsDebugStateDetailPaneContent(
                        uiState =
                            AceWindowsDebugStateDetailUiState(
                                cardOrder = listOf(AceWindowsDebugStateCardKey.FLAG_INFO),
                                aceWindows =
                                    AceWindowsDebugState(
                                        flag = AceWindowsFlagData(flag = flag),
                                    ),
                            ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            rule.onNodeWithText(displayName).assertIsDisplayed()
        }
    }

    @Test
    fun `フラッグの種類だけでコンテンツキーが決まる`() {
        val green = AceWindowsFlagData(flag = AceWindowsFlagType.GREEN_FLAG)
        val red = AceWindowsFlagData(flag = AceWindowsFlagType.RED_FLAG)

        assertEquals(null, flagInfoContentKey(null))
        assertEquals(AceWindowsFlagType.GREEN_FLAG, flagInfoContentKey(green))
        assertEquals(flagInfoContentKey(green), flagInfoContentKey(green.copy()))
        assertNotEquals(flagInfoContentKey(green), flagInfoContentKey(red))
    }
}
