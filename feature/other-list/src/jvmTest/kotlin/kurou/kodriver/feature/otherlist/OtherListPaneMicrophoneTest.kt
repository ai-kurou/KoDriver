package kurou.kodriver.feature.otherlist

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.domain.microphone.model.MICROPHONE_CUT_QUIET_SOUND_LEVEL_DEFAULT
import kurou.kodriver.domain.microphone.model.MICROPHONE_CUT_QUIET_SOUND_LEVEL_MIN
import org.junit.Rule
import org.junit.Test

class OtherListPaneMicrophoneTest {
    @get:Rule
    val rule = createComposeRule()

    private val onItemClick: (OtherListItemType) -> Unit = mockk()

    @Test
    fun `マイク設定カテゴリに小さい音をカットを表示する`() {
        setPane()

        rule.onNodeWithText("マイク設定").assertIsDisplayed()
        rule
            .onNodeWithText("小さい音をカット")
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertIsNotSelected()
        confirmVerified(onItemClick)
    }

    @Test
    fun `小さい音をカットをクリックすると選択を通知する`() {
        every { onItemClick(OtherListItemType.MicrophoneCutQuietSound) } returns Unit
        setPane()

        rule.onNodeWithText("小さい音をカット").performClick()

        verify(exactly = 1) { onItemClick(OtherListItemType.MicrophoneCutQuietSound) }
        confirmVerified(onItemClick)
    }

    @Test
    fun `保存されたレベルをサブ項目に表示する`() {
        setPane(level = 3)

        rule.onNodeWithText("レベル 3").assertIsDisplayed()
        confirmVerified(onItemClick)
    }

    @Test
    fun `レベルが最小値ならオフをサブ項目に表示する`() {
        setPane(level = MICROPHONE_CUT_QUIET_SOUND_LEVEL_MIN)

        rule.onNodeWithText("オフ（カットしない）").assertIsDisplayed()
        confirmVerified(onItemClick)
    }

    private fun setPane(level: Int = MICROPHONE_CUT_QUIET_SOUND_LEVEL_DEFAULT) {
        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.MicrophoneCutQuietSound),
                        microphoneCutQuietSoundLevel = level,
                    ),
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
