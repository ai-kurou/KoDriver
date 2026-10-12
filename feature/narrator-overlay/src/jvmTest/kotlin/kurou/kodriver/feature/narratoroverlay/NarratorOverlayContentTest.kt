package kurou.kodriver.feature.narratoroverlay

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import kurou.kodriver.domain.model.OVERLAY_TEXT_COLOR_DEFAULT
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class NarratorOverlayContentTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `既定色と保存したARGB文字色をTextに適用する`() {
        val state = mutableStateOf(NarratorOverlayUiState())
        rule.setContent { NarratorOverlayContent(uiState = state.value) }
        listOf(OVERLAY_TEXT_COLOR_DEFAULT, 0x80123ABCL, 0xFFFFFFFFL).forEach { argb ->
            rule.runOnIdle { state.value = NarratorOverlayUiState(overlayTextColor = argb) }
            val results = mutableListOf<TextLayoutResult>()
            rule.onNodeWithText("").performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
                action(results)
            }
            assertEquals(
                Color(argb),
                results
                    .single()
                    .layoutInput.style.color,
            )
        }
    }
}
