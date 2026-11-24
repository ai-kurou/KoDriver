package kurou.kodriver.feature.othermicrophonecutquietsounddetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherMicrophoneCutQuietSoundDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    private val hasSliderProgressBarRangeInfo =
        SemanticsMatcher("ProgressBarRangeInfoを持つスライダー") {
            it.config.contains(SemanticsProperties.ProgressBarRangeInfo)
        }

    private fun setPane(
        level: Int = 6,
        cutInCommand: Boolean = false,
        onLevelChanged: (Int) -> Unit = {},
        onCutInCommandChanged: (Boolean) -> Unit = {},
        onBack: () -> Unit = {},
    ) {
        rule.setContent {
            MaterialTheme {
                OtherMicrophoneCutQuietSoundDetailPaneContent(
                    uiState = OtherMicrophoneCutQuietSoundDetailUiState(level = level, cutInCommand = cutInCommand),
                    onLevelChanged = onLevelChanged,
                    onCutInCommandChanged = onCutInCommandChanged,
                    onBack = onBack,
                )
            }
        }
    }

    @Test
    fun `段階値をレベルとして表示する`() {
        setPane(level = 4)

        rule.onNodeWithText("レベル 4").assertIsDisplayed()
    }

    @Test
    fun `段階値が0のときはオフと表示する`() {
        setPane(level = 0)

        rule.onNodeWithText("オフ（カットしない）").assertIsDisplayed()
    }

    @Test
    fun `スライダー操作を完了すると変更後の段階値を通知する`() {
        var changed: Int? = null
        setPane(onLevelChanged = { changed = it })

        rule
            .onNode(hasSliderProgressBarRangeInfo)
            .performSemanticsAction(SemanticsActions.SetProgress) { it(9f) }

        assertEquals(9, changed)
    }

    @Test
    fun `既定値と異なるときにリセットすると既定の段階値を通知する`() {
        var changed: Int? = null
        setPane(level = 9, onLevelChanged = { changed = it })

        rule.onNode(hasContentDescription("既定のレベルに戻す")).performClick()

        assertEquals(6, changed)
    }

    @Test
    fun `コマンド受付中もカットするカードをタップすると反転した値を通知する`() {
        var changed: Boolean? = null
        setPane(cutInCommand = false, onCutInCommandChanged = { changed = it })

        rule.onNodeWithText("コマンド受付中もカットする").performClick()

        assertEquals(true, changed)
    }

    @Test
    fun `コマンド受付中もカットする設定がオンのときにカードをタップするとオフを通知する`() {
        var changed: Boolean? = null
        setPane(cutInCommand = true, onCutInCommandChanged = { changed = it })

        rule.onNodeWithText("コマンド受付中もカットする").performClick()

        assertEquals(false, changed)
    }

    @Test
    fun `戻るボタンをタップするとonBackが呼ばれる`() {
        var backCount = 0
        setPane(onBack = { backCount++ })

        rule.onNode(hasContentDescription("戻る")).performClick()

        assertEquals(1, backCount)
    }
}
