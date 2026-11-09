package kurou.kodriver.feature.main

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class SpeechRecognitionDetailTest {
    @get:Rule
    val rule = createComposeRule()

    private fun setDetail(
        presentation: SpeechRecognitionDetailPresentation,
        uiState: SpeechRecognitionUiState = SpeechRecognitionUiState(),
        onEnabledChange: (Boolean) -> Unit = {},
    ) {
        rule.setContent {
            KoDriverTheme {
                SpeechRecognitionDetail(
                    presentation = presentation,
                    uiState = uiState,
                    onEnabledChange = onEnabledChange,
                    onDeviceMenuOpen = {},
                    onDeviceSelected = {},
                    onDismissRequest = {},
                )
            }
        }
    }

    @Test
    fun `ポップオーバーはスイッチと状態と認識結果を表示し操作を通知する`() {
        val changes = mutableListOf<Boolean>()
        setDetail(
            presentation = SpeechRecognitionDetailPresentation.Popover,
            uiState =
                SpeechRecognitionUiState(
                    status = SpeechRecognitionStatus.Listening,
                    level = 0.5f,
                    recognized = listOf(listOf("燃料", "然料"), listOf("ブレーキ")),
                ),
            onEnabledChange = { changes += it },
        )

        rule.onNodeWithText("燃料 / 然料").assertIsDisplayed()
        rule.onNodeWithText("ブレーキ").assertIsDisplayed()
        rule.onNodeWithContentDescription("マイクの入力音量").assertIsDisplayed()
        rule.onNodeWithContentDescription("音声認識の聞き取りを切り替え").assertIsOff().performClick()
        rule.runOnIdle { assertEquals(listOf(true), changes) }
    }

    @Test
    fun `ボトムシートはスイッチと状態と認識結果を表示し操作を通知する`() {
        val changes = mutableListOf<Boolean>()
        setDetail(
            presentation = SpeechRecognitionDetailPresentation.BottomSheet,
            uiState =
                SpeechRecognitionUiState(
                    status = SpeechRecognitionStatus.Listening,
                    level = 0.5f,
                    recognized = listOf(listOf("燃料", "然料")),
                ),
            onEnabledChange = { changes += it },
        )

        rule.onNodeWithText("燃料 / 然料").assertIsDisplayed()
        rule.onNodeWithContentDescription("マイクの入力音量").assertIsDisplayed()
        rule.onNodeWithContentDescription("音声認識の聞き取りを切り替え").assertIsOff().performClick()
        rule.runOnIdle { assertEquals(listOf(true), changes) }
    }

    @Test
    fun `ポップオーバーの位置はチップの上に右端をそろえる`() {
        val provider = AboveAnchorEndPositionProvider(gapPx = 8)

        val offset =
            provider.calculatePosition(
                anchorBounds = IntRect(left = 900, top = 700, right = 944, bottom = 744),
                windowSize = IntSize(width = 1000, height = 800),
                layoutDirection = LayoutDirection.Ltr,
                popupContentSize = IntSize(width = 360, height = 300),
            )

        assertEquals(IntOffset(x = 584, y = 392), offset)
    }

    @Test
    fun `ポップオーバーの位置は画面の外へはみ出さないよう内側へ寄せる`() {
        val provider = AboveAnchorEndPositionProvider(gapPx = 8)

        val offset =
            provider.calculatePosition(
                anchorBounds = IntRect(left = 0, top = 100, right = 44, bottom = 144),
                windowSize = IntSize(width = 300, height = 800),
                layoutDirection = LayoutDirection.Ltr,
                popupContentSize = IntSize(width = 360, height = 300),
            )

        assertEquals(IntOffset(x = 0, y = 0), offset)
    }
}
