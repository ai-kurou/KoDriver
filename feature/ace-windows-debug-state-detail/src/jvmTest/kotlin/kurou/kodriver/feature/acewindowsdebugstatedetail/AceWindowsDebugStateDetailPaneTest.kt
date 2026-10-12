package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AceWindowsDebugStateDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルを表示して戻る操作を通知する`() {
        var backCount = 0
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState = AceWindowsDebugStateDetailUiState(),
                    canNavigateBack = true,
                    onBack = { backCount++ },
                )
            }
        }

        rule.onNodeWithText("デバッグ状態").assertIsDisplayed()
        rule.onNode(hasContentDescription("戻る")).performClick()

        assertEquals(1, backCount)
    }

    @Test
    fun `戻れない場合は戻るボタンを表示しない`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState = AceWindowsDebugStateDetailUiState(),
                    canNavigateBack = false,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("デバッグ状態").assertIsDisplayed()
        rule.onNode(hasContentDescription("戻る")).assertDoesNotExist()
    }

    @Test
    fun `ACEの7枚だけを表示し未受信の6枚は未取得になる`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState = AceWindowsDebugStateDetailUiState(),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("Assetto Corsa EVO（Windows版）").assertIsDisplayed()
        listOf(
            "選択中のシミュレーター",
            "車両位置",
            "フラッグ情報",
            "並走車両",
            "自己ベストラップ",
            "タイヤカーカス温度",
            "燃料消費",
        ).forEach { title ->
            rule.onNodeWithText(title).assertIsDisplayed()
        }
        rule.onAllNodesWithText("未取得").assertCountEquals(6)
        rule.onNodeWithText("車両クラス").assertDoesNotExist()
    }

    @Test
    fun `cardOrderを入れ替えても両方のカードが指定順に表示される`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState =
                        AceWindowsDebugStateDetailUiState(
                            cardOrder =
                                listOf(
                                    AceWindowsDebugStateCardKey.FLAG_INFO,
                                    AceWindowsDebugStateCardKey.SIMULATOR,
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("フラッグ情報").assertIsDisplayed()
        rule.onNodeWithText("選択中のシミュレーター").assertIsDisplayed()
        val flag = rule.onNodeWithText("フラッグ情報").fetchSemanticsNode().boundsInRoot
        val simulator = rule.onNodeWithText("選択中のシミュレーター").fetchSemanticsNode().boundsInRoot
        val input = rule.onNodeWithText("オーバーレイ表示テスト").fetchSemanticsNode().boundsInRoot
        assertTrue(input.top < simulator.top)
        assertTrue(flag.top < simulator.top || flag.left < simulator.left)
    }

    @Test
    fun `画面幅の境界に合わせて1列から3列に切り替える`() {
        assertEquals(1, calculateDebugStateColumns(399.dp))
        assertEquals(2, calculateDebugStateColumns(400.dp))
        assertEquals(2, calculateDebugStateColumns(699.dp))
        assertEquals(3, calculateDebugStateColumns(700.dp))
    }

    @Test
    fun `先頭の入力カードは入力と選択と送信を通知する`() {
        val state = mutableStateOf(AceWindowsDebugStateDetailUiState(cardOrder = emptyList()))
        var sent = 0
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState = state.value,
                    canNavigateBack = false,
                    onBack = {},
                    onDebugOverlayTextChange = { state.value = state.value.copy(debugOverlayText = it) },
                    onDebugOverlayKeySelect = { state.value = state.value.copy(debugOverlaySelectedKey = it) },
                    onSendDebugOverlayText = { sent++ },
                )
            }
        }
        rule.onNodeWithText("オーバーレイ表示テスト").assertIsDisplayed()
        rule.onNodeWithText("送信").assertIsNotEnabled()
        rule.onNodeWithText("表示する文字列").performTextInput("表示テスト")
        rule.onNodeWithText("送信").assertIsEnabled().performClick()
        assertEquals("表示テスト", state.value.debugOverlayText)
        assertEquals(1, sent)
        val keys = ReadoutItemKey.entriesOf(Simulator.AceWindows)
        val initial = keys.first().value
        val selected = keys[1].value
        rule.onNodeWithText(" ($initial)", substring = true).performClick()
        rule.onNodeWithText(" ($selected)", substring = true).performClick()
        assertEquals(keys[1], state.value.debugOverlaySelectedKey)
    }
}
