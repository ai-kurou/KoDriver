package kurou.kodriver.feature.otherlist

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

class OtherListPaneVoicePitchTest {
    @get:Rule
    val rule = createComposeRule()

    private val onItemClick: (OtherListItemType) -> Unit = mockk()

    @Test
    fun `読み上げ設定に既定ピッチを選択状態にせず表示する`() {
        setPane(OtherListUiState(items = listOf(OtherListItemType.VoicePitch)))

        rule.onNodeWithText("読み上げ設定").assertIsDisplayed()
        rule.onNodeWithText("1.0倍").assertIsDisplayed()
        rule
            .onNodeWithText("声の高さ")
            .assertIsDisplayed()
            .assertIsNotSelected()

        verify(exactly = 0) { onItemClick(OtherListItemType.VoicePitch) }
        confirmVerified(onItemClick)
    }

    @Test
    fun `声の高さにはクリック操作を付けず他の項目には付ける`() {
        setPane(OtherListUiState(items = listOf(OtherListItemType.VoiceSpeed, OtherListItemType.VoicePitch)))

        rule.onNodeWithText("声の高さ").assertHasNoClickAction()
        rule.onNodeWithText("読み上げ速度").assertHasClickAction()
        confirmVerified(onItemClick)
    }

    @Test
    fun `保存済みピッチを小数1桁に丸めて表示する`() {
        setPane(OtherListUiState(items = listOf(OtherListItemType.VoicePitch), voicePitch = 1.26f))

        rule.onNodeWithText("1.3倍").assertIsDisplayed()
        confirmVerified(onItemClick)
    }

    @Test
    fun `整数のピッチにも小数1桁を表示する`() {
        setPane(OtherListUiState(items = listOf(OtherListItemType.VoicePitch), voicePitch = 2f))

        rule.onNodeWithText("2.0倍").assertIsDisplayed()
        confirmVerified(onItemClick)
    }

    @Test
    fun `TTS警告があっても声の高さを読み上げ設定の最下部に表示する`() {
        setPane(
            OtherListUiState(
                items =
                    listOf(
                        OtherListItemType.TtsEngineMissing,
                        OtherListItemType.VoiceSpeed,
                        OtherListItemType.VoicePitch,
                        OtherListItemType.Theme,
                    ),
            ),
        )

        val speed = rule.onNodeWithText("読み上げ速度").fetchSemanticsNode().boundsInRoot
        val pitch = rule.onNodeWithText("声の高さ").fetchSemanticsNode().boundsInRoot
        val appSettings = rule.onNodeWithText("アプリ設定").fetchSemanticsNode().boundsInRoot
        assertTrue(speed.bottom <= pitch.top)
        assertTrue(pitch.bottom <= appSettings.top)
        confirmVerified(onItemClick)
    }

    private fun setPane(uiState: OtherListUiState) {
        rule.setContent {
            OtherListPane(
                uiState = uiState,
                onItemClick = onItemClick,
                onOverlayVisibleChange = {},
                onKeepScreenOnChange = {},
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
            )
        }
    }
}
