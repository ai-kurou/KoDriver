package kurou.kodriver.feature.other

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class OtherContentTest {

    @get:Rule
    val rule = createComposeRule()

    private val singlePaneDirective = PaneScaffoldDirective(
        maxHorizontalPartitions = 1,
        horizontalPartitionSpacerSize = 0.dp,
        maxVerticalPartitions = 1,
        verticalPartitionSpacerSize = 0.dp,
        defaultPanePreferredWidth = 360.dp,
        excludedBounds = emptyList(),
    )

    @Test
    fun `詳細ペインに遷移後にbackHandlerのコールバックを呼ぶと一覧に戻る`() {
        var backEnabled = false
        var capturedOnBack: (() -> Unit)? = null
        var selectedItem by mutableStateOf<OtherItemType?>(null)

        rule.setContent {
            OtherContent(
                uiState = OtherListUiState(selectedItem = selectedItem),
                onItemSelected = { selectedItem = OtherItemType.fromId(it) },
                onClearSelectedItem = { selectedItem = null },
                scaffoldDirective = singlePaneDirective,
                backHandler = { enabled, onBack ->
                    backEnabled = enabled
                    capturedOnBack = onBack
                },
                detailContent = {},
            )
        }

        assertFalse(backEnabled)

        rule.onNodeWithTag("other_item_0").performClick()
        rule.waitForIdle()

        assertTrue(backEnabled)

        rule.runOnIdle { capturedOnBack?.invoke() }
        rule.waitUntil { !backEnabled }

        assertFalse(backEnabled)
    }
}
