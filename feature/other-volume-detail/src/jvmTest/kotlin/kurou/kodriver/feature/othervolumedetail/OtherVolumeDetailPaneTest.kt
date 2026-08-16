package kurou.kodriver.feature.othervolumedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherVolumeDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    private val hasSliderProgressBarRangeInfo =
        SemanticsMatcher("ProgressBarRangeInfoを持つスライダー") {
            it.config.contains(SemanticsProperties.ProgressBarRangeInfo)
        }

    @Test
    fun `音量スライダー操作を完了すると変更後の音量を通知する`() {
        var changedVolume: Int? = null
        rule.setContent {
            MaterialTheme {
                OtherVolumeDetailPaneContent(
                    uiState = OtherVolumeDetailUiState(volume = 80, deviceVolume = 60),
                    onVolumeChanged = { changedVolume = it },
                )
            }
        }

        rule
            .onAllNodes(hasSliderProgressBarRangeInfo)[0]
            .performSemanticsAction(SemanticsActions.SetProgress) { it(50f) }

        assertEquals(50, changedVolume)
    }

    @Test
    fun `端末のマスター音量スライダー操作を完了すると変更後の音量を通知する`() {
        var changedDeviceVolume: Int? = null
        rule.setContent {
            MaterialTheme {
                OtherVolumeDetailPaneContent(
                    uiState = OtherVolumeDetailUiState(volume = 80, deviceVolume = 60),
                    onDeviceVolumeChanged = { changedDeviceVolume = it },
                )
            }
        }

        rule
            .onAllNodes(hasSliderProgressBarRangeInfo)[1]
            .performSemanticsAction(SemanticsActions.SetProgress) { it(30f) }

        assertEquals(30, changedDeviceVolume)
    }

    @Test
    fun `試聴チップをタップするとonPreviewClickedが呼ばれる`() {
        var previewCount = 0
        rule.setContent {
            MaterialTheme {
                OtherVolumeDetailPaneContent(
                    uiState = OtherVolumeDetailUiState(volume = 80, deviceVolume = 60),
                    onPreviewClicked = { previewCount++ },
                )
            }
        }

        rule.onNode(hasText("試聴")).performClick()

        assertEquals(1, previewCount)
    }

    @Test
    fun `戻るボタンをタップするとonBackが呼ばれる`() {
        var backCount = 0
        rule.setContent {
            MaterialTheme {
                OtherVolumeDetailPaneContent(
                    uiState = OtherVolumeDetailUiState(),
                    onBack = { backCount++ },
                )
            }
        }

        rule.onNode(hasContentDescription("戻る")).performClick()

        assertEquals(1, backCount)
    }
}
