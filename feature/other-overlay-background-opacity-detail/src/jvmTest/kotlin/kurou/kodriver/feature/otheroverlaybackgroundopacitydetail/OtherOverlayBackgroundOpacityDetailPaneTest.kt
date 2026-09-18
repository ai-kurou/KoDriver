package kurou.kodriver.feature.otheroverlaybackgroundopacitydetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherOverlayBackgroundOpacityDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    private val hasSliderProgressBarRangeInfo =
        SemanticsMatcher("ProgressBarRangeInfoを持つスライダー") {
            it.config.contains(SemanticsProperties.ProgressBarRangeInfo)
        }

    @Test
    fun `透明度スライダー操作を完了すると変更後の透明度を通知する`() {
        var changedOpacity: Int? = null
        rule.setContent {
            MaterialTheme {
                OtherOverlayBackgroundOpacityDetailPaneContent(
                    uiState = OtherOverlayBackgroundOpacityDetailUiState(opacity = 50),
                    onOpacityChanged = { changedOpacity = it },
                )
            }
        }

        rule
            .onNode(hasSliderProgressBarRangeInfo)
            .performSemanticsAction(SemanticsActions.SetProgress) { it(80f) }

        assertEquals(80, changedOpacity)
    }

    @Test
    fun `戻るボタンをタップするとonBackが呼ばれる`() {
        var backCount = 0
        rule.setContent {
            MaterialTheme {
                OtherOverlayBackgroundOpacityDetailPaneContent(
                    uiState = OtherOverlayBackgroundOpacityDetailUiState(),
                    onBack = { backCount++ },
                )
            }
        }

        rule.onNode(hasContentDescription("戻る")).performClick()

        assertEquals(1, backCount)
    }
}
