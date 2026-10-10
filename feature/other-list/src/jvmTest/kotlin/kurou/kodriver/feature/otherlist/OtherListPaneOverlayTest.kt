package kurou.kodriver.feature.otherlist

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.domain.model.OverlayTextSize
import org.junit.Rule
import org.junit.Test

class OtherListPaneOverlayTest {
    @get:Rule
    val rule = createComposeRule()

    private val onItemClick: (OtherListItemType) -> Unit = mockk()

    @Test
    fun `オーバーレイ設定に既定の文字サイズと背景透明度を表示する`() {
        setPane(
            OtherListUiState(
                items = listOf(OtherListItemType.OverlayTextSize, OtherListItemType.OverlayBackgroundOpacity),
            ),
        )

        rule.onNodeWithText("オーバーレイ設定").assertIsDisplayed()
        rule.onNode(hasText("文字サイズ") and hasText("中")).assertIsDisplayed().assertIsNotSelected()
        rule.onNode(hasText("背景の透明度") and hasText("50%")).assertIsDisplayed().assertIsNotSelected()
        verify(exactly = 0) { onItemClick(OtherListItemType.OverlayTextSize) }
        verify(exactly = 0) { onItemClick(OtherListItemType.OverlayBackgroundOpacity) }
        confirmVerified(onItemClick)
    }

    @Test
    fun `文字サイズが極小の場合は極小を表示する`() {
        assertTextSizeLabel(OverlayTextSize.EXTRA_SMALL, "極小")
    }

    @Test
    fun `文字サイズが小の場合は小を表示する`() {
        assertTextSizeLabel(OverlayTextSize.SMALL, "小")
    }

    @Test
    fun `文字サイズが中の場合は中を表示する`() {
        assertTextSizeLabel(OverlayTextSize.MEDIUM, "中")
    }

    @Test
    fun `文字サイズが大の場合は大を表示する`() {
        assertTextSizeLabel(OverlayTextSize.LARGE, "大")
    }

    @Test
    fun `文字サイズが特大の場合は特大を表示する`() {
        assertTextSizeLabel(OverlayTextSize.EXTRA_LARGE, "特大")
    }

    @Test
    fun `文字サイズが超特大の場合は超特大を表示する`() {
        assertTextSizeLabel(OverlayTextSize.HUGE, "超特大")
    }

    @Test
    fun `文字サイズが最大の場合は最大を表示する`() {
        assertTextSizeLabel(OverlayTextSize.MAXIMUM, "最大")
    }

    @Test
    fun `背景透明度が0の場合は0パーセントを表示する`() {
        setPane(
            OtherListUiState(
                items = listOf(OtherListItemType.OverlayBackgroundOpacity),
                overlayBackgroundOpacity = 0,
            ),
        )

        rule.onNode(hasText("背景の透明度") and hasText("0%")).assertIsDisplayed()
        confirmVerified(onItemClick)
    }

    @Test
    fun `背景透明度が100の場合は100パーセントを表示する`() {
        setPane(
            OtherListUiState(
                items = listOf(OtherListItemType.OverlayBackgroundOpacity),
                overlayBackgroundOpacity = 100,
            ),
        )

        rule.onNode(hasText("背景の透明度") and hasText("100%")).assertIsDisplayed()
        confirmVerified(onItemClick)
    }

    @Test
    fun `文字サイズをクリックすると選択を通知する`() {
        every { onItemClick(OtherListItemType.OverlayTextSize) } returns Unit
        setPane(OtherListUiState(items = listOf(OtherListItemType.OverlayTextSize)))

        rule.onNodeWithText("文字サイズ").assertHasClickAction().performClick()

        verify(exactly = 1) { onItemClick(OtherListItemType.OverlayTextSize) }
        confirmVerified(onItemClick)
    }

    @Test
    fun `背景の透明度をクリックすると選択を通知する`() {
        every { onItemClick(OtherListItemType.OverlayBackgroundOpacity) } returns Unit
        setPane(OtherListUiState(items = listOf(OtherListItemType.OverlayBackgroundOpacity)))

        rule.onNodeWithText("背景の透明度").assertHasClickAction().performClick()

        verify(exactly = 1) { onItemClick(OtherListItemType.OverlayBackgroundOpacity) }
        confirmVerified(onItemClick)
    }

    private fun assertTextSizeLabel(
        textSize: OverlayTextSize,
        label: String,
    ) {
        setPane(
            OtherListUiState(
                items = listOf(OtherListItemType.OverlayTextSize),
                overlayTextSize = textSize,
            ),
        )

        rule.onNode(hasText("文字サイズ") and hasText(label)).assertIsDisplayed()
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
