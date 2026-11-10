package kurou.kodriver.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SliderPreviewDetailPaneContentTest {
    private val onValueChangeFinished: (Float) -> Unit = mockk()
    private val onPreviewClicked: (String) -> Unit = mockk()
    private val haptic: HapticFeedback = mockk()

    @get:Rule
    val rule = createComposeRule()

    private val texts =
        SliderPreviewDetailTexts(
            title = "テスト設定",
            description = "テスト設定の説明です。",
            sliderLabel = "%1\$.1f倍",
            resetContentDescription = "テスト設定をデフォルトに戻す",
            previewLabel = "試聴",
            previewStopLabel = "試聴を停止",
            previewSample = "試聴用の文言",
        )

    @Composable
    private fun Content(
        value: Float = 1.0f,
        isPreviewing: Boolean = false,
        mockHaptic: Boolean = false,
        onValueChangeFinished: (Float) -> Unit = {},
        onPreviewClicked: (String) -> Unit = {},
        canNavigateBack: Boolean = true,
        onBack: () -> Unit = {},
        modifier: Modifier = Modifier,
    ) {
        // 触覚フィードバックを検証しないテストでは、本物の LocalHapticFeedback をそのまま使う。
        val provided = if (mockHaptic) arrayOf(LocalHapticFeedback provides haptic) else emptyArray()
        CompositionLocalProvider(*provided) {
            KoDriverTheme {
                SliderPreviewDetailPaneContent(
                    texts = texts,
                    value = value,
                    valueRange = 0.5f..2.0f,
                    steps = 14,
                    defaultValue = 1.0f,
                    isPreviewing = isPreviewing,
                    onValueChangeFinished = onValueChangeFinished,
                    onPreviewClicked = onPreviewClicked,
                    canNavigateBack = canNavigateBack,
                    onBack = onBack,
                    modifier = modifier,
                )
            }
        }
    }

    private fun slider() =
        rule.onNode(
            SemanticsMatcher("ProgressBarRangeInfoを持つスライダー") {
                it.config.contains(SemanticsProperties.ProgressBarRangeInfo)
            },
        )

    @Test
    fun `タイトルと説明とスライダー値のラベルを表示して戻る操作を通知する`() {
        var backCount = 0
        rule.setContent { Content(onBack = { backCount++ }) }

        rule.onNodeWithText("テスト設定").assertIsDisplayed()
        rule.onNodeWithText("テスト設定の説明です。").assertIsDisplayed()
        rule.onNodeWithText("1.0倍").assertIsDisplayed()
        rule.onNode(hasContentDescription("戻る")).performClick()

        assertEquals(1, backCount)
    }

    @Test
    fun `戻れない場合は戻るボタンを表示しない`() {
        rule.setContent { Content(canNavigateBack = false) }

        rule.onNodeWithText("テスト設定").assertIsDisplayed()
        rule.onNode(hasContentDescription("戻る")).assertDoesNotExist()
    }

    @Test
    fun `渡したmodifierを詳細ペインに適用する`() {
        rule.setContent { Content(modifier = Modifier.testTag("slider-preview-detail")) }

        rule.onNodeWithTag("slider-preview-detail").assertIsDisplayed()
    }

    @Test
    fun `スライダー操作を完了すると変更後の値を通知する`() {
        every { onValueChangeFinished(2.0f) } returns Unit
        rule.setContent { Content(mockHaptic = true, onValueChangeFinished = onValueChangeFinished) }

        slider().performSemanticsAction(SemanticsActions.SetProgress) { it(2.0f) }

        verify(exactly = 1) { onValueChangeFinished(2.0f) }
        verify(exactly = 0) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(onValueChangeFinished, haptic)
        rule.onNodeWithText("2.0倍").assertIsDisplayed()
    }

    @Test
    fun `スライダーのドラッグは振動せず完了時だけ値を通知する`() {
        val changedValues = mutableListOf<Float>()
        rule.setContent { Content(mockHaptic = true, onValueChangeFinished = { changedValues.add(it) }) }

        val slider = slider()
        slider.performTouchInput {
            down(center)
            moveTo(center.copy(x = width * 0.8f))
        }
        rule.runOnIdle {
            assertTrue(changedValues.isEmpty())
            verify(exactly = 0) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        }
        slider.performTouchInput { up() }
        rule.waitForIdle()

        assertEquals(1, changedValues.size)
        assertTrue(changedValues.single() > 1.0f)
        verify(exactly = 0) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `リセット操作はデフォルト値を通知する`() {
        every { onValueChangeFinished(1.0f) } returns Unit
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        rule.setContent { Content(value = 1.5f, mockHaptic = true, onValueChangeFinished = onValueChangeFinished) }

        rule.onNode(hasContentDescription("テスト設定をデフォルトに戻す")).performClick()

        verify(exactly = 1) { onValueChangeFinished(1.0f) }
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(onValueChangeFinished, haptic)
    }

    @Test
    fun `デフォルト値ではリセット操作は無効になる`() {
        rule.setContent { Content() }

        rule.onNode(hasContentDescription("テスト設定をデフォルトに戻す")).assertIsNotEnabled()
    }

    @Test
    fun `試聴ボタンを押すと試聴用の文言を通知する`() {
        every { onPreviewClicked("試聴用の文言") } returns Unit
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        rule.setContent { Content(mockHaptic = true, onPreviewClicked = onPreviewClicked) }

        rule.onNodeWithText("試聴").performClick()

        verify(exactly = 1) { onPreviewClicked("試聴用の文言") }
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(onPreviewClicked, haptic)
    }

    @Test
    fun `試聴中は停止ボタンを表示して停止操作と振動を通知する`() {
        every { onPreviewClicked("試聴用の文言") } returns Unit
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        rule.setContent { Content(isPreviewing = true, mockHaptic = true, onPreviewClicked = onPreviewClicked) }

        rule.onNodeWithText("試聴を停止").assertIsDisplayed().performClick()
        rule.onNodeWithText("試聴").assertDoesNotExist()

        verify(exactly = 1) { onPreviewClicked("試聴用の文言") }
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(onPreviewClicked, haptic)
    }
}
