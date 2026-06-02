package kurou.kodriver.feature.readout

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class ReadoutContentTest {

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

        rule.setContent {
            ReadoutContent(
                scaffoldDirective = singlePaneDirective,
                backHandler = { enabled, onBack ->
                    backEnabled = enabled
                    capturedOnBack = onBack
                },
            )
        }

        assertFalse(backEnabled)

        // ドロップダウンを開く
        rule.onNodeWithText("Le Mans Ultimate").performClick()
        rule.waitForIdle()

        // ドロップダウンのアイテムを選択してリストを表示
        rule.onAllNodesWithText("Le Mans Ultimate")[1].performClick()
        rule.waitForIdle()

        // LazyColumn のアイテムをタップして詳細ペインへ遷移
        rule.onNodeWithText("車両接近").performClick()
        rule.waitForIdle()

        assertTrue(backEnabled)

        rule.runOnIdle { capturedOnBack?.invoke() }
        rule.waitForIdle()

        assertFalse(backEnabled)
    }
}
