package kurou.kodriver.feature.readoutlist

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kodriver.feature.readoutlist.generated.resources.Res
import kodriver.feature.readoutlist.generated.resources.item_flag
import kodriver.feature.readoutlist.generated.resources.item_my_best_lap
import kodriver.feature.readoutlist.generated.resources.item_remaining_fuel_laps
import kodriver.feature.readoutlist.generated.resources.item_tyre_temperature
import kodriver.feature.readoutlist.generated.resources.item_vehicle_approach
import kodriver.feature.readoutlist.generated.resources.item_vehicle_damage
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
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
    fun `lmu_windowsの詳細ペインに遷移後にbackHandlerのコールバックを呼ぶと一覧に戻る`() {
        var backEnabled = false
        var capturedOnBack: (() -> Unit)? = null
        var itemTexts by mutableStateOf(emptyList<String>())
        var tyreTemperatureText by mutableStateOf("")
        var selectedItem by mutableStateOf<ReadoutListItemType?>(null)

        rule.setContent {
            tyreTemperatureText = stringResource(Res.string.item_tyre_temperature)
            itemTexts = listOf(
                stringResource(Res.string.item_vehicle_approach),
                stringResource(Res.string.item_flag),
                stringResource(Res.string.item_vehicle_damage),
                stringResource(Res.string.item_my_best_lap),
            )
            ReadoutContent(
                uiState = ReadoutListUiState(
                    simulators = listOf(Simulator.LmuWindows),
                    selectedSimulator = Simulator.LmuWindows,
                    items = listOf(
                        ReadoutItemKey.LmuWindows.Flag.Root,
                        ReadoutItemKey.LmuWindows.VehicleApproach,
                        ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                        ReadoutItemKey.LmuWindows.TyreTemperature,
                        ReadoutItemKey.LmuWindows.MyBestLap,
                    ),
                    readoutEnabledStates = mapOf(
                        ReadoutItemKey.LmuWindows.Flag.Root to true,
                        ReadoutItemKey.LmuWindows.VehicleApproach to true,
                        ReadoutItemKey.LmuWindows.VehicleDamage.Root to true,
                        ReadoutItemKey.LmuWindows.TyreTemperature to true,
                        ReadoutItemKey.LmuWindows.MyBestLap to true,
                    ),
                    selectedItem = selectedItem,
                ),
                onSimulatorSelected = {},
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onItemSelected = { selectedItem = ReadoutListItemType.fromId(Simulator.LmuWindows, it) },
                onClearSelectedItem = { selectedItem = null },
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                backHandler = { enabled, _, onBack ->
                    backEnabled = enabled
                    capturedOnBack = onBack
                },
            )
        }

        rule.onNodeWithText(tyreTemperatureText).assertExists()
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
                stringResource(Res.string.item_remaining_fuel_laps),
                stringResource(Res.string.item_my_best_lap),
            )
            ReadoutContent(
                uiState = ReadoutListUiState(
                    simulators = listOf(Simulator.Gt7Ps5),
                    selectedSimulator = Simulator.Gt7Ps5,
                    items = listOf(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps, ReadoutItemKey.Gt7Ps5.MyBestLap),
                    readoutEnabledStates = mapOf(
                        ReadoutItemKey.Gt7Ps5.RemainingFuelLaps to true,
                        ReadoutItemKey.Gt7Ps5.MyBestLap to true,
                    ),
                    selectedItem = selectedItem,
                ),
                onSimulatorSelected = {},
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onItemSelected = { selectedItem = ReadoutListItemType.fromId(Simulator.Gt7Ps5, it) },
                onClearSelectedItem = { selectedItem = null },
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                backHandler = { enabled, _, onBack ->
                    backEnabled = enabled
                    capturedOnBack = onBack
                },
            )
        }

        assertAllItemsCanNavigateBack(itemTexts, { backEnabled }, { capturedOnBack?.invoke() })
    }

    @Test
    fun `tyre_temperatureとその他の項目のSwitchはON_OFF変更コールバックを呼ぶ`() {
        val changedItems = mutableListOf<Pair<ReadoutItemKey, Boolean>>()
        var tyreTemperatureText by mutableStateOf("")

        rule.setContent {
            tyreTemperatureText = stringResource(Res.string.item_tyre_temperature)
            ReadoutContent(
                uiState = ReadoutListUiState(
                    simulators = listOf(Simulator.LmuWindows),
                    selectedSimulator = Simulator.LmuWindows,
                    items = listOf(ReadoutItemKey.LmuWindows.TyreTemperature, ReadoutItemKey.LmuWindows.Flag.Root),
                    readoutEnabledStates = mapOf(
                        ReadoutItemKey.LmuWindows.TyreTemperature to true,
                        ReadoutItemKey.LmuWindows.Flag.Root to true,
                    ),
                ),
                onSimulatorSelected = {},
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { item, enabled -> changedItems += item to enabled },
                onItemSelected = {},
                onClearSelectedItem = {},
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                backHandler = { _, _, _ -> },
            )
        }

        rule.onNodeWithText(tyreTemperatureText).assertExists()
        rule.onAllNodes(hasSwitchRole()).assertCountEquals(2)
        rule.onAllNodes(hasSwitchRole()).get(0).assertIsEnabled().performClick()
        rule.onAllNodes(hasSwitchRole()).get(1).assertIsEnabled().performClick()

        assertTrue(changedItems.contains(ReadoutItemKey.LmuWindows.TyreTemperature to false))
        assertTrue(changedItems.contains(ReadoutItemKey.LmuWindows.Flag.Root to false))
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

    private fun hasSwitchRole(): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)
}
