package kurou.kodriver.feature.readoutlist

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kodriver.feature.readoutlist.generated.resources.Res
import kodriver.feature.readoutlist.generated.resources.item_flag
import kodriver.feature.readoutlist.generated.resources.item_my_best_lap
import kodriver.feature.readoutlist.generated.resources.item_remaining_fuel_laps
import kodriver.feature.readoutlist.generated.resources.item_vehicle_approach
import kodriver.feature.readoutlist.generated.resources.item_vehicle_damage
import kurou.kodriver.domain.model.ReadoutItemKey
import org.jetbrains.compose.resources.stringResource
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class ReadoutContentTest {

    @get:Rule
    val rule = createComposeRule()

    private val compactWindowSizeClass = WindowSizeClass.compute(400f, 800f)

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
        var itemTexts by mutableStateOf(emptyList<String>())
        var selectedItem by mutableStateOf<ReadoutListItemType?>(null)

        rule.setContent {
            itemTexts = listOf(
                stringResource(Res.string.item_vehicle_approach),
                stringResource(Res.string.item_flag),
                stringResource(Res.string.item_vehicle_damage),
            )
            ReadoutContent(
                uiState = ReadoutListUiState(
                    simulators = listOf("lmu_windows"),
                    selectedSimulator = "lmu_windows",
                    items = listOf(ReadoutItemKey.VEHICLE_APPROACH, ReadoutItemKey.FLAG, ReadoutItemKey.VEHICLE_DAMAGE),
                    selectedItem = selectedItem,
                ),
                onSimulatorSelected = {},
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onItemSelected = { selectedItem = ReadoutListItemType.fromId("lmu_windows", it) },
                onClearSelectedItem = { selectedItem = null },
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                backHandler = { enabled, onBack ->
                    backEnabled = enabled
                    capturedOnBack = onBack
                },
            )
        }

        assertAllItemsCanNavigateBack(itemTexts, { backEnabled }, { capturedOnBack?.invoke() })
    }

    @Test
    fun `gt7_ps5の詳細ペインに遷移後にbackHandlerのコールバックを呼ぶと一覧に戻る`() {
        var backEnabled = false
        var capturedOnBack: (() -> Unit)? = null
        var itemTexts by mutableStateOf(emptyList<String>())
        var selectedItem by mutableStateOf<ReadoutListItemType?>(null)

        rule.setContent {
            itemTexts = listOf(
                stringResource(Res.string.item_my_best_lap),
                stringResource(Res.string.item_remaining_fuel_laps),
            )
            ReadoutContent(
                uiState = ReadoutListUiState(
                    simulators = listOf("gt7_ps5"),
                    selectedSimulator = "gt7_ps5",
                    items = listOf(ReadoutItemKey.MY_BEST_LAP, ReadoutItemKey.REMAINING_FUEL_LAPS),
                    selectedItem = selectedItem,
                ),
                onSimulatorSelected = {},
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onItemSelected = { selectedItem = ReadoutListItemType.fromId("gt7_ps5", it) },
                onClearSelectedItem = { selectedItem = null },
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                backHandler = { enabled, onBack ->
                    backEnabled = enabled
                    capturedOnBack = onBack
                },
            )
        }

        assertAllItemsCanNavigateBack(itemTexts, { backEnabled }, { capturedOnBack?.invoke() })
    }

    private fun assertAllItemsCanNavigateBack(
        itemTexts: List<String>,
        backEnabled: () -> Boolean,
        onBack: () -> Unit,
    ) {
        assertFalse(backEnabled())

        itemTexts.forEach { itemText ->
            rule.onNodeWithText(itemText).performClick()
            rule.waitForIdle()

            assertTrue(backEnabled())

            rule.runOnIdle { onBack() }
            rule.waitForIdle()

            assertFalse(backEnabled())
        }
    }
}
