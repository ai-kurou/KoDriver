package kurou.kodriver.feature.othervolumedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import org.junit.Test
import kotlin.test.assertEquals

class OtherVolumeDetailPaneTest {
    @Test
    fun `スライダー操作を完了すると変更後の音量を通知する`() =
        composeScreenshotTest {
            var changedVolume: Int? = null
            setContent {
                MaterialTheme {
                    OtherVolumeDetailPaneContent(
                        uiState = OtherVolumeDetailUiState(volume = 80),
                        onVolumeChanged = { changedVolume = it },
                    )
                }
            }

            onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 80f, range = 0f..100f, steps = 99)),
            ).performSemanticsAction(SemanticsActions.SetProgress) { it(50f) }

            assertEquals(50, changedVolume)
        }

    @Test
    fun `戻るボタンをタップするとonBackが呼ばれる`() =
        composeScreenshotTest {
            var backCount = 0
            setContent {
                MaterialTheme {
                    OtherVolumeDetailPaneContent(
                        uiState = OtherVolumeDetailUiState(),
                        onBack = { backCount++ },
                    )
                }
            }

            onNode(hasContentDescription("戻る")).performClick()

            assertEquals(1, backCount)
        }
}
