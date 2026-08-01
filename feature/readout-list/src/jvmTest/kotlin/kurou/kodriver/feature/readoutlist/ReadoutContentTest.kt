package kurou.kodriver.feature.readoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kodriver.feature.readoutlist.generated.resources.Res
import kodriver.feature.readoutlist.generated.resources.item_flag
import kodriver.feature.readoutlist.generated.resources.item_my_best_lap
import kodriver.feature.readoutlist.generated.resources.item_remaining_fuel
import kodriver.feature.readoutlist.generated.resources.item_remaining_fuel_laps
import kodriver.feature.readoutlist.generated.resources.item_remaining_virtual_energy
import kodriver.feature.readoutlist.generated.resources.item_tyre_temperature
import kodriver.feature.readoutlist.generated.resources.item_tyre_wear
import kodriver.feature.readoutlist.generated.resources.item_vehicle_approach
import kodriver.feature.readoutlist.generated.resources.item_vehicle_damage
import kodriver.feature.readoutlist.generated.resources.scroll_to_top
import kodriver.feature.readoutlist.generated.resources.simulator_label
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.jetbrains.compose.resources.stringResource
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
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
                        ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                        ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                        ReadoutItemKey.LmuWindows.TyreTemperature.Root,
                        ReadoutItemKey.LmuWindows.MyBestLap.Root,
                    ),
                    readoutEnabledStates = mapOf(
                        ReadoutItemKey.LmuWindows.Flag.Root to true,
                        ReadoutItemKey.LmuWindows.VehicleApproach.Root to true,
                        ReadoutItemKey.LmuWindows.VehicleDamage.Root to true,
                        ReadoutItemKey.LmuWindows.TyreTemperature.Root to true,
                        ReadoutItemKey.LmuWindows.MyBestLap.Root to true,
                    ),
                    selectedItem = selectedItem,
                ),
                onSimulatorSelected = {},
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { _, _ -> },
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
    fun `tyre_wearの項目を選択すると詳細ペインのタイトルにタイヤ摩耗を表示する`() {
        var tyreWearText by mutableStateOf("")

        rule.setContent {
            tyreWearText = stringResource(Res.string.item_tyre_wear)
            ReadoutContent(
                uiState = ReadoutListUiState(
                    simulators = listOf(Simulator.LmuWindows),
                    selectedSimulator = Simulator.LmuWindows,
                    items = listOf(ReadoutItemKey.LmuWindows.TyreWear.Root),
                    readoutEnabledStates = mapOf(ReadoutItemKey.LmuWindows.TyreWear.Root to true),
                    selectedItem = ReadoutListItemType.LmuWindows.TyreWear,
                ),
                onSimulatorSelected = {},
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { _, _ -> },
                onItemSelected = {},
                onClearSelectedItem = {},
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
            )
        }

        rule.onNodeWithText(tyreWearText).assertExists()
    }

    @Test
    fun `バーチャルエナジー残量をタップすると選択コールバックを呼ぶ`() {
        var veText by mutableStateOf("")
        val selected = mutableListOf<ReadoutItemKey>()

        rule.setContent {
            veText = stringResource(Res.string.item_remaining_virtual_energy)
            ReadoutContent(
                uiState = ReadoutListUiState(
                    simulators = listOf(Simulator.LmuWindows),
                    selectedSimulator = Simulator.LmuWindows,
                    items = listOf(ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root),
                    readoutEnabledStates = mapOf(
                        ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root to false,
                    ),
                ),
                onSimulatorSelected = {},
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { _, _ -> },
                onItemSelected = { selected.add(it) },
                onClearSelectedItem = {},
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
            )
        }

        rule.onNodeWithText(veText).performClick()
        rule.waitForIdle()

        assertEquals(listOf<ReadoutItemKey>(ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root), selected)
    }

    @Test
    fun `ace_windowsの燃料残量項目を選択すると詳細ペインのタイトルに燃料残量を表示する`() {
        var remainingFuelText by mutableStateOf("")

        rule.setContent {
            remainingFuelText = stringResource(Res.string.item_remaining_fuel)
            ReadoutContent(
                uiState = ReadoutListUiState(
                    simulators = listOf(Simulator.AceWindows),
                    selectedSimulator = Simulator.AceWindows,
                    items = listOf(ReadoutItemKey.AceWindows.RemainingFuel.Root),
                    readoutEnabledStates = mapOf(ReadoutItemKey.AceWindows.RemainingFuel.Root to true),
                    selectedItem = ReadoutListItemType.AceWindows.RemainingFuel,
                ),
                onSimulatorSelected = {},
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { _, _ -> },
                onItemSelected = {},
                onClearSelectedItem = {},
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
            )
        }

        rule.onNodeWithText(remainingFuelText).assertExists()
    }

    @Test
    fun `gt7_ps5の燃料残量をタップすると選択コールバックを呼ぶ`() {
        var remainingFuelText by mutableStateOf("")
        val selected = mutableListOf<ReadoutItemKey>()

        rule.setContent {
            remainingFuelText = stringResource(Res.string.item_remaining_fuel)
            ReadoutContent(
                uiState = ReadoutListUiState(
                    simulators = listOf(Simulator.Gt7Ps5),
                    selectedSimulator = Simulator.Gt7Ps5,
                    items = listOf(ReadoutItemKey.Gt7Ps5.RemainingFuel.Root),
                    readoutEnabledStates = mapOf(ReadoutItemKey.Gt7Ps5.RemainingFuel.Root to true),
                ),
                onSimulatorSelected = {},
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { _, _ -> },
                onItemSelected = { selected.add(it) },
                onClearSelectedItem = {},
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
            )
        }

        rule.onNodeWithText(remainingFuelText).performClick()
        rule.waitForIdle()

        assertEquals(listOf<ReadoutItemKey>(ReadoutItemKey.Gt7Ps5.RemainingFuel.Root), selected)
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
                stringResource(Res.string.item_remaining_fuel),
                stringResource(Res.string.item_my_best_lap),
            )
            ReadoutContent(
                uiState = ReadoutListUiState(
                    simulators = listOf(Simulator.Gt7Ps5),
                    selectedSimulator = Simulator.Gt7Ps5,
                    items = listOf(
                        ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
                        ReadoutItemKey.Gt7Ps5.RemainingFuel.Root,
                        ReadoutItemKey.Gt7Ps5.MyBestLap.Root,
                    ),
                    readoutEnabledStates = mapOf(
                        ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root to true,
                        ReadoutItemKey.Gt7Ps5.RemainingFuel.Root to true,
                        ReadoutItemKey.Gt7Ps5.MyBestLap.Root to true,
                    ),
                    selectedItem = selectedItem,
                ),
                onSimulatorSelected = {},
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { _, _ -> },
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
                    items = listOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root, ReadoutItemKey.LmuWindows.Flag.Root),
                    readoutEnabledStates = mapOf(
                        ReadoutItemKey.LmuWindows.TyreTemperature.Root to true,
                        ReadoutItemKey.LmuWindows.Flag.Root to true,
                    ),
                ),
                onSimulatorSelected = {},
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { item, enabled -> changedItems += item to enabled },
                onQueueEnabledChanged = { _, _ -> },
                onItemSelected = {},
                onClearSelectedItem = {},
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                backHandler = { _, _, _ -> },
            )
        }

        rule.onNodeWithText(tyreTemperatureText).assertExists()
        rule.onAllNodes(hasSwitchRole()).assertCountEquals(2)
        rule
            .onAllNodes(hasSwitchRole())
            .get(0)
            .assertIsEnabled()
            .performClick()
        rule
            .onAllNodes(hasSwitchRole())
            .get(1)
            .assertIsEnabled()
            .performClick()

        assertTrue(changedItems.contains(ReadoutItemKey.LmuWindows.TyreTemperature.Root to false))
        assertTrue(changedItems.contains(ReadoutItemKey.LmuWindows.Flag.Root to false))
    }

    @Test
    fun `キュー追加トグルをクリックするとON_OFF変更コールバックを呼ぶ`() {
        val changedItems = mutableListOf<Pair<ReadoutItemKey, Boolean>>()
        var tyreTemperatureText by mutableStateOf("")

        rule.setContent {
            tyreTemperatureText = stringResource(Res.string.item_tyre_temperature)
            ReadoutContent(
                uiState = ReadoutListUiState(
                    simulators = listOf(Simulator.LmuWindows),
                    selectedSimulator = Simulator.LmuWindows,
                    items = listOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root, ReadoutItemKey.LmuWindows.Flag.Root),
                    readoutEnabledStates = mapOf(
                        ReadoutItemKey.LmuWindows.TyreTemperature.Root to true,
                        ReadoutItemKey.LmuWindows.Flag.Root to true,
                    ),
                    queueEnabledStates = mapOf(
                        ReadoutItemKey.LmuWindows.TyreTemperature.Root to false,
                        ReadoutItemKey.LmuWindows.Flag.Root to false,
                    ),
                ),
                onSimulatorSelected = {},
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { item, enabled -> changedItems += item to enabled },
                onItemSelected = {},
                onClearSelectedItem = {},
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                backHandler = { _, _, _ -> },
            )
        }

        rule.onNodeWithText(tyreTemperatureText).assertExists()
        rule.onAllNodes(hasQueueToggleRole()).assertCountEquals(2)
        rule
            .onAllNodes(hasQueueToggleRole())
            .get(0)
            .assertIsEnabled()
            .performClick()
        rule
            .onAllNodes(hasQueueToggleRole())
            .get(1)
            .assertIsEnabled()
            .performClick()

        assertTrue(changedItems.contains(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true))
        assertTrue(changedItems.contains(ReadoutItemKey.LmuWindows.Flag.Root to true))
    }

    @Test
    fun `読み上げスイッチがOFFの項目はキュー追加トグルもdisableになりクリックしてもコールバックを呼ばない`() {
        val changedItems = mutableListOf<Pair<ReadoutItemKey, Boolean>>()
        var tyreTemperatureText by mutableStateOf("")

        rule.setContent {
            tyreTemperatureText = stringResource(Res.string.item_tyre_temperature)
            ReadoutContent(
                uiState = ReadoutListUiState(
                    simulators = listOf(Simulator.LmuWindows),
                    selectedSimulator = Simulator.LmuWindows,
                    items = listOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root, ReadoutItemKey.LmuWindows.Flag.Root),
                    readoutEnabledStates = mapOf(
                        ReadoutItemKey.LmuWindows.TyreTemperature.Root to false,
                        ReadoutItemKey.LmuWindows.Flag.Root to true,
                    ),
                    queueEnabledStates = mapOf(
                        ReadoutItemKey.LmuWindows.TyreTemperature.Root to false,
                        ReadoutItemKey.LmuWindows.Flag.Root to false,
                    ),
                ),
                onSimulatorSelected = {},
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { item, enabled -> changedItems += item to enabled },
                onItemSelected = {},
                onClearSelectedItem = {},
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                backHandler = { _, _, _ -> },
            )
        }

        rule.onNodeWithText(tyreTemperatureText).assertExists()
        rule.onAllNodes(hasQueueToggleRole()).assertCountEquals(2)
        rule
            .onAllNodes(hasQueueToggleRole())
            .get(0)
            .assertIsNotEnabled()
            .performClick()
        rule
            .onAllNodes(hasQueueToggleRole())
            .get(1)
            .assertIsEnabled()
            .performClick()

        assertFalse(changedItems.contains(ReadoutItemKey.LmuWindows.TyreTemperature.Root to true))
        assertTrue(changedItems.contains(ReadoutItemKey.LmuWindows.Flag.Root to true))
    }

    @Test
    fun `リストを下にスクロールすると先頭へ戻るボタンを表示して先頭へ戻れる`() {
        var scrollToTopText by mutableStateOf("")
        var simulatorLabelText by mutableStateOf("")
        var lastItemText by mutableStateOf("")
        val items = listOf(
            ReadoutItemKey.LmuWindows.Flag.Root,
            ReadoutItemKey.LmuWindows.Flag.BlueFlag,
            ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag,
            ReadoutItemKey.LmuWindows.Flag.FullCourseYellow,
            ReadoutItemKey.LmuWindows.Flag.RedFlag,
            ReadoutItemKey.LmuWindows.VehicleApproach.Root,
            ReadoutItemKey.LmuWindows.VehicleDamage.Root,
            ReadoutItemKey.LmuWindows.VehicleDamage.Overheat,
            ReadoutItemKey.LmuWindows.TyreTemperature.Root,
            ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning,
            ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning,
            ReadoutItemKey.LmuWindows.MyBestLap.Root,
        )

        rule.setContent {
            scrollToTopText = stringResource(Res.string.scroll_to_top)
            simulatorLabelText = stringResource(Res.string.simulator_label)
            lastItemText = stringResource(Res.string.item_my_best_lap)
            Box(modifier = Modifier.height(240.dp)) {
                ReadoutListPane(
                    uiState = ReadoutListUiState(
                        simulators = listOf(Simulator.LmuWindows),
                        selectedSimulator = Simulator.LmuWindows,
                        items = items,
                        readoutEnabledStates = items.associateWith { true },
                    ),
                    onSimulatorSelected = {},
                    onMove = { _, _ -> },
                    onReadoutEnabledChanged = { _, _ -> },
                    onQueueEnabledChanged = { _, _ -> },
                    onItemClick = {},
                )
            }
        }

        rule.onNode(hasScrollAction()).performScrollToNode(hasText(lastItemText))
        rule.onNodeWithText(scrollToTopText).assertExists().performClick()

        rule.waitUntil {
            rule.onAllNodes(hasText(simulatorLabelText)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `scrollToTopRequestが増えるとリストを先頭へ戻す`() {
        var simulatorLabelText by mutableStateOf("")
        var lastItemText by mutableStateOf("")
        var scrollToTopRequest by mutableStateOf(0)
        val items = listOf(
            ReadoutItemKey.LmuWindows.Flag.Root,
            ReadoutItemKey.LmuWindows.Flag.BlueFlag,
            ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag,
            ReadoutItemKey.LmuWindows.Flag.FullCourseYellow,
            ReadoutItemKey.LmuWindows.Flag.RedFlag,
            ReadoutItemKey.LmuWindows.VehicleApproach.Root,
            ReadoutItemKey.LmuWindows.VehicleDamage.Root,
            ReadoutItemKey.LmuWindows.VehicleDamage.Overheat,
            ReadoutItemKey.LmuWindows.TyreTemperature.Root,
            ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning,
            ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning,
            ReadoutItemKey.LmuWindows.MyBestLap.Root,
        )

        rule.setContent {
            simulatorLabelText = stringResource(Res.string.simulator_label)
            lastItemText = stringResource(Res.string.item_my_best_lap)
            Box(modifier = Modifier.height(240.dp)) {
                ReadoutListPane(
                    uiState = ReadoutListUiState(
                        simulators = listOf(Simulator.LmuWindows),
                        selectedSimulator = Simulator.LmuWindows,
                        items = items,
                        readoutEnabledStates = items.associateWith { true },
                    ),
                    onSimulatorSelected = {},
                    onMove = { _, _ -> },
                    onReadoutEnabledChanged = { _, _ -> },
                    onQueueEnabledChanged = { _, _ -> },
                    onItemClick = {},
                    scrollToTopRequest = scrollToTopRequest,
                )
            }
        }

        rule.onNode(hasScrollAction()).performScrollToNode(hasText(lastItemText))
        rule.runOnIdle { scrollToTopRequest++ }

        rule.waitUntil {
            rule.onAllNodes(hasText(simulatorLabelText)).fetchSemanticsNodes().isNotEmpty()
        }
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

    private fun hasQueueToggleRole(): SemanticsMatcher =
        SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox)
}
