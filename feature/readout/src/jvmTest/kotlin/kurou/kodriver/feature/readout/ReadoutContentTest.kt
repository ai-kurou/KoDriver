package kurou.kodriver.feature.readout

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kodriver.feature.readout.generated.resources.Res
import kodriver.feature.readout.generated.resources.item_vehicle_approach
import org.jetbrains.compose.resources.stringResource
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
        var vehicleApproachText by mutableStateOf("")
        var selectedItem by mutableStateOf<ReadoutItemType?>(null)

        rule.setContent {
            vehicleApproachText = stringResource(Res.string.item_vehicle_approach)
            ReadoutContent(
                uiState = ReadoutListUiState(
                    simulators = listOf("lmu"),
                    selectedSimulator = "lmu",
                    items = listOf("vehicle_approach", "laps_remaining"),
                    selectedItem = selectedItem,
                ),
                onSimulatorSelected = {},
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onItemSelected = { selectedItem = ReadoutItemType.fromId(it) },
                onClearSelectedItem = { selectedItem = null },
                scaffoldDirective = singlePaneDirective,
                backHandler = { enabled, onBack ->
                    backEnabled = enabled
                    capturedOnBack = onBack
                },
            )
        }

        assertFalse(backEnabled)

        // LazyColumn のアイテムをタップして詳細ペインへ遷移
        rule.onNodeWithText(vehicleApproachText).performClick()
        rule.waitForIdle()

        assertTrue(backEnabled)

        rule.runOnIdle { capturedOnBack?.invoke() }
        rule.waitForIdle()

        assertFalse(backEnabled)
    }
}
