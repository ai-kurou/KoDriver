package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import kurou.kodriver.core.designsystem.KoDriverTheme
import kotlin.test.assertEquals

internal fun ComposeContentTestRule.assertPickerCategories(
    itemKeys: List<String>,
    expectedTexts: List<String>,
) {
    setContent {
        KoDriverTheme {
            ManualReadoutItemPickerSheet(itemKeys, 0, {}, {})
        }
    }
    val texts =
        onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text))
            .fetchSemanticsNodes()
            .flatMap { node -> node.config[SemanticsProperties.Text].map { it.text } }
    assertEquals(expectedTexts, texts)
}
