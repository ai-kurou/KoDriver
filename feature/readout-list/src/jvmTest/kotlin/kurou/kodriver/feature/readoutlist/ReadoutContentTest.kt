package kurou.kodriver.feature.readoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.HingeInfo
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
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

    private val singlePaneDirective =
        PaneScaffoldDirective(
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
            tyreTemperatureText = itemDisplayName(LmuWindowsReadoutItemKey.TyreTemperature.Root)
            itemTexts =
                listOf(
                    itemDisplayName(LmuWindowsReadoutItemKey.VehicleApproach.Root),
                    itemDisplayName(LmuWindowsReadoutItemKey.Flag.Root),
                    itemDisplayName(LmuWindowsReadoutItemKey.VehicleDamage.Root),
                    itemDisplayName(LmuWindowsReadoutItemKey.MyBestLap.Root),
                )
            ReadoutContent(
                uiState =
                    ReadoutListUiState(
                        selectedSimulator = Simulator.LmuWindows,
                        items =
                            listOf(
                                LmuWindowsReadoutItemKey.Flag.Root,
                                LmuWindowsReadoutItemKey.VehicleApproach.Root,
                                LmuWindowsReadoutItemKey.VehicleDamage.Root,
                                LmuWindowsReadoutItemKey.TyreTemperature.Root,
                                LmuWindowsReadoutItemKey.MyBestLap.Root,
                            ),
                        readoutEnabledStates =
                            mapOf(
                                LmuWindowsReadoutItemKey.Flag.Root to true,
                                LmuWindowsReadoutItemKey.VehicleApproach.Root to true,
                                LmuWindowsReadoutItemKey.VehicleDamage.Root to true,
                                LmuWindowsReadoutItemKey.TyreTemperature.Root to true,
                                LmuWindowsReadoutItemKey.MyBestLap.Root to true,
                            ),
                        selectedItem = selectedItem,
                    ),
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { _, _ -> },
                onStartSoundEnabledChanged = { _, _ -> },
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
            tyreWearText = itemDisplayName(LmuWindowsReadoutItemKey.TyreWear.Root)
            ReadoutContent(
                uiState =
                    ReadoutListUiState(
                        selectedSimulator = Simulator.LmuWindows,
                        items = listOf(LmuWindowsReadoutItemKey.TyreWear.Root),
                        readoutEnabledStates = mapOf(LmuWindowsReadoutItemKey.TyreWear.Root to true),
                        selectedItem = LmuWindowsReadoutListItemType.TyreWear,
                    ),
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { _, _ -> },
                onStartSoundEnabledChanged = { _, _ -> },
                onItemSelected = {},
                onClearSelectedItem = {},
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
            )
        }

        rule.onNodeWithText(tyreWearText).assertExists()
    }

    @Test
    fun `detailPane表示中にテーブルトップ姿勢になると選択解除コールバックを呼ぶ`() {
        var selectedItem by mutableStateOf<ReadoutListItemType?>(LmuWindowsReadoutListItemType.TyreWear)
        var windowPosture by mutableStateOf(Posture())
        var clearSelectedItemCallCount = 0

        rule.setContent {
            ReadoutContent(
                uiState =
                    ReadoutListUiState(
                        selectedSimulator = Simulator.LmuWindows,
                        items = listOf(LmuWindowsReadoutItemKey.TyreWear.Root),
                        readoutEnabledStates = mapOf(LmuWindowsReadoutItemKey.TyreWear.Root to true),
                        selectedItem = selectedItem,
                    ),
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { _, _ -> },
                onStartSoundEnabledChanged = { _, _ -> },
                onItemSelected = {},
                onClearSelectedItem = {
                    clearSelectedItemCallCount++
                    selectedItem = null
                },
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                windowPosture = windowPosture,
            )
        }

        rule.runOnIdle { windowPosture = Posture(isTabletop = true) }

        rule.waitUntil { clearSelectedItemCallCount == 1 }
        assertEquals(null, selectedItem)
    }

    @Test
    fun `detailPane表示中に平らでない縦ヒンジの姿勢になると選択解除コールバックを呼ぶ`() {
        var selectedItem by mutableStateOf<ReadoutListItemType?>(LmuWindowsReadoutListItemType.TyreWear)
        var windowPosture by mutableStateOf(Posture())
        var clearSelectedItemCallCount = 0

        rule.setContent {
            ReadoutContent(
                uiState =
                    ReadoutListUiState(
                        selectedSimulator = Simulator.LmuWindows,
                        items = listOf(LmuWindowsReadoutItemKey.TyreWear.Root),
                        readoutEnabledStates = mapOf(LmuWindowsReadoutItemKey.TyreWear.Root to true),
                        selectedItem = selectedItem,
                    ),
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { _, _ -> },
                onStartSoundEnabledChanged = { _, _ -> },
                onItemSelected = {},
                onClearSelectedItem = {
                    clearSelectedItemCallCount++
                    selectedItem = null
                },
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                windowPosture = windowPosture,
            )
        }

        rule.runOnIdle {
            windowPosture =
                Posture(
                    hingeList =
                        listOf(
                            HingeInfo(
                                bounds = Rect(left = 400f, top = 0f, right = 420f, bottom = 800f),
                                isFlat = false,
                                isVertical = true,
                                isSeparating = true,
                                isOccluding = false,
                            ),
                        ),
                )
        }

        rule.waitUntil { clearSelectedItemCallCount == 1 }
        assertEquals(null, selectedItem)
    }

    @Test
    fun `tyre_temperatureの項目を選択すると詳細ペインのタイトルにタイヤ温度を表示する`() {
        var tyreTemperatureText by mutableStateOf("")

        rule.setContent {
            tyreTemperatureText = itemDisplayName(LmuWindowsReadoutItemKey.TyreTemperature.Root)
            ReadoutContent(
                uiState =
                    ReadoutListUiState(
                        selectedSimulator = Simulator.LmuWindows,
                        items = listOf(LmuWindowsReadoutItemKey.TyreTemperature.Root),
                        readoutEnabledStates = mapOf(LmuWindowsReadoutItemKey.TyreTemperature.Root to true),
                        selectedItem = LmuWindowsReadoutListItemType.TyreTemperature,
                    ),
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { _, _ -> },
                onStartSoundEnabledChanged = { _, _ -> },
                onItemSelected = {},
                onClearSelectedItem = {},
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
            )
        }

        rule.onAllNodesWithText(tyreTemperatureText).onFirst().assertExists()
    }

    @Test
    fun `バーチャルエナジー残量をタップすると選択コールバックを呼ぶ`() {
        var veText by mutableStateOf("")
        val selected = mutableListOf<ReadoutItemKey>()

        rule.setContent {
            veText = itemDisplayName(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root)
            ReadoutContent(
                uiState =
                    ReadoutListUiState(
                        selectedSimulator = Simulator.LmuWindows,
                        items = listOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root),
                        readoutEnabledStates =
                            mapOf(
                                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to false,
                            ),
                    ),
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { _, _ -> },
                onStartSoundEnabledChanged = { _, _ -> },
                onItemSelected = { selected.add(it) },
                onClearSelectedItem = {},
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
            )
        }

        rule.onNodeWithText(veText).performClick()
        rule.waitForIdle()

        assertEquals(listOf<ReadoutItemKey>(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root), selected)
    }

    @Test
    fun `ace_windowsの燃料残量項目を選択すると詳細ペインのタイトルに燃料残量を表示する`() {
        var remainingFuelText by mutableStateOf("")

        rule.setContent {
            remainingFuelText = itemDisplayName(AceWindowsReadoutItemKey.RemainingFuel.Root)
            ReadoutContent(
                uiState =
                    ReadoutListUiState(
                        selectedSimulator = Simulator.AceWindows,
                        items = listOf(AceWindowsReadoutItemKey.RemainingFuel.Root),
                        readoutEnabledStates = mapOf(AceWindowsReadoutItemKey.RemainingFuel.Root to true),
                        selectedItem = AceWindowsReadoutListItemType.RemainingFuel,
                    ),
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { _, _ -> },
                onStartSoundEnabledChanged = { _, _ -> },
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
            remainingFuelText = itemDisplayName(Gt7Ps5ReadoutItemKey.RemainingFuel.Root)
            ReadoutContent(
                uiState =
                    ReadoutListUiState(
                        selectedSimulator = Simulator.Gt7Ps5,
                        items = listOf(Gt7Ps5ReadoutItemKey.RemainingFuel.Root),
                        readoutEnabledStates = mapOf(Gt7Ps5ReadoutItemKey.RemainingFuel.Root to true),
                    ),
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { _, _ -> },
                onStartSoundEnabledChanged = { _, _ -> },
                onItemSelected = { selected.add(it) },
                onClearSelectedItem = {},
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
            )
        }

        rule.onNodeWithText(remainingFuelText).performClick()
        rule.waitForIdle()

        assertEquals(listOf<ReadoutItemKey>(Gt7Ps5ReadoutItemKey.RemainingFuel.Root), selected)
    }

    @Test
    fun `gt7_ps5の詳細ペインに遷移後にbackHandlerのコールバックを呼ぶと一覧に戻る`() {
        var backEnabled = false
        var capturedOnBack: (() -> Unit)? = null
        var itemTexts by mutableStateOf(emptyList<String>())
        var selectedItem by mutableStateOf<ReadoutListItemType?>(null)

        rule.setContent {
            itemTexts =
                listOf(
                    itemDisplayName(Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root),
                    itemDisplayName(Gt7Ps5ReadoutItemKey.RemainingFuel.Root),
                    itemDisplayName(Gt7Ps5ReadoutItemKey.MyBestLap.Root),
                )
            ReadoutContent(
                uiState =
                    ReadoutListUiState(
                        selectedSimulator = Simulator.Gt7Ps5,
                        items =
                            listOf(
                                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
                                Gt7Ps5ReadoutItemKey.RemainingFuel.Root,
                                Gt7Ps5ReadoutItemKey.MyBestLap.Root,
                            ),
                        readoutEnabledStates =
                            mapOf(
                                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root to true,
                                Gt7Ps5ReadoutItemKey.RemainingFuel.Root to true,
                                Gt7Ps5ReadoutItemKey.MyBestLap.Root to true,
                            ),
                        selectedItem = selectedItem,
                    ),
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { _, _ -> },
                onStartSoundEnabledChanged = { _, _ -> },
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
            tyreTemperatureText = itemDisplayName(LmuWindowsReadoutItemKey.TyreTemperature.Root)
            ReadoutContent(
                uiState =
                    ReadoutListUiState(
                        selectedSimulator = Simulator.LmuWindows,
                        items =
                            listOf(
                                LmuWindowsReadoutItemKey.TyreTemperature.Root,
                                LmuWindowsReadoutItemKey.Flag.Root,
                            ),
                        readoutEnabledStates =
                            mapOf(
                                LmuWindowsReadoutItemKey.TyreTemperature.Root to true,
                                LmuWindowsReadoutItemKey.Flag.Root to true,
                            ),
                    ),
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { item, enabled -> changedItems += item to enabled },
                onQueueEnabledChanged = { _, _ -> },
                onStartSoundEnabledChanged = { _, _ -> },
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

        assertTrue(changedItems.contains(LmuWindowsReadoutItemKey.TyreTemperature.Root to false))
        assertTrue(changedItems.contains(LmuWindowsReadoutItemKey.Flag.Root to false))
    }

    @Test
    fun `キュー追加トグルをクリックするとON_OFF変更コールバックを呼ぶ`() {
        val changedItems = mutableListOf<Pair<ReadoutItemKey, Boolean>>()
        var tyreTemperatureText by mutableStateOf("")

        rule.setContent {
            tyreTemperatureText = itemDisplayName(LmuWindowsReadoutItemKey.TyreTemperature.Root)
            ReadoutContent(
                uiState =
                    ReadoutListUiState(
                        selectedSimulator = Simulator.LmuWindows,
                        items =
                            listOf(
                                LmuWindowsReadoutItemKey.TyreTemperature.Root,
                                LmuWindowsReadoutItemKey.Flag.Root,
                            ),
                        readoutEnabledStates =
                            mapOf(
                                LmuWindowsReadoutItemKey.TyreTemperature.Root to true,
                                LmuWindowsReadoutItemKey.Flag.Root to true,
                            ),
                        queueEnabledStates =
                            mapOf(
                                LmuWindowsReadoutItemKey.TyreTemperature.Root to false,
                                LmuWindowsReadoutItemKey.Flag.Root to false,
                            ),
                    ),
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { item, enabled -> changedItems += item to enabled },
                onStartSoundEnabledChanged = { _, _ -> },
                onItemSelected = {},
                onClearSelectedItem = {},
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                backHandler = { _, _, _ -> },
            )
        }

        rule.onNodeWithText(tyreTemperatureText).assertExists()
        rule.onAllNodes(hasQueueToggleRole()).assertCountEquals(4)
        rule
            .onNodeWithTag("readoutListQueueTouchTarget:${LmuWindowsReadoutItemKey.TyreTemperature.Root.value}")
            .assertIsEnabled()
            .performClick()
        rule
            .onNodeWithTag("readoutListQueueTouchTarget:${LmuWindowsReadoutItemKey.Flag.Root.value}")
            .assertIsEnabled()
            .performClick()

        assertTrue(changedItems.contains(LmuWindowsReadoutItemKey.TyreTemperature.Root to true))
        assertTrue(changedItems.contains(LmuWindowsReadoutItemKey.Flag.Root to true))
    }

    @Test
    fun `読み上げスイッチがOFFの項目はキュー追加トグルもdisableになりクリックしてもコールバックを呼ばない`() {
        val changedItems = mutableListOf<Pair<ReadoutItemKey, Boolean>>()
        var tyreTemperatureText by mutableStateOf("")

        rule.setContent {
            tyreTemperatureText = itemDisplayName(LmuWindowsReadoutItemKey.TyreTemperature.Root)
            ReadoutContent(
                uiState =
                    ReadoutListUiState(
                        selectedSimulator = Simulator.LmuWindows,
                        items =
                            listOf(
                                LmuWindowsReadoutItemKey.TyreTemperature.Root,
                                LmuWindowsReadoutItemKey.Flag.Root,
                            ),
                        readoutEnabledStates =
                            mapOf(
                                LmuWindowsReadoutItemKey.TyreTemperature.Root to false,
                                LmuWindowsReadoutItemKey.Flag.Root to true,
                            ),
                        queueEnabledStates =
                            mapOf(
                                LmuWindowsReadoutItemKey.TyreTemperature.Root to false,
                                LmuWindowsReadoutItemKey.Flag.Root to false,
                            ),
                    ),
                onMove = { _, _ -> },
                onReadoutEnabledChanged = { _, _ -> },
                onQueueEnabledChanged = { item, enabled -> changedItems += item to enabled },
                onStartSoundEnabledChanged = { _, _ -> },
                onItemSelected = {},
                onClearSelectedItem = {},
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                backHandler = { _, _, _ -> },
            )
        }

        rule.onNodeWithText(tyreTemperatureText).assertExists()
        rule.onAllNodes(hasQueueToggleRole()).assertCountEquals(4)
        rule
            .onNodeWithTag("readoutListQueueTouchTarget:${LmuWindowsReadoutItemKey.TyreTemperature.Root.value}")
            .assertIsNotEnabled()
            .performClick()
        rule
            .onNodeWithTag("readoutListQueueTouchTarget:${LmuWindowsReadoutItemKey.Flag.Root.value}")
            .assertIsEnabled()
            .performClick()

        assertFalse(changedItems.contains(LmuWindowsReadoutItemKey.TyreTemperature.Root to true))
        assertTrue(changedItems.contains(LmuWindowsReadoutItemKey.Flag.Root to true))
    }

    @Test
    fun `リストを下にスクロールすると先頭へ戻るボタンを表示して先頭へ戻れる`() {
        val scrollToTopText = "先頭へ"
        val priorityHintLabelText = "読み上げ優先度"
        var lastItemText by mutableStateOf("")
        val items =
            listOf(
                LmuWindowsReadoutItemKey.Flag.Root,
                LmuWindowsReadoutItemKey.Flag.BlueFlag,
                LmuWindowsReadoutItemKey.Flag.SectorYellowFlag,
                LmuWindowsReadoutItemKey.Flag.FullCourseYellow,
                LmuWindowsReadoutItemKey.Flag.RedFlag,
                LmuWindowsReadoutItemKey.VehicleApproach.Root,
                LmuWindowsReadoutItemKey.VehicleDamage.Root,
                LmuWindowsReadoutItemKey.VehicleDamage.Overheat,
                LmuWindowsReadoutItemKey.TyreTemperature.Root,
                LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning,
                LmuWindowsReadoutItemKey.TyreTemperature.LowWarning,
                LmuWindowsReadoutItemKey.MyBestLap.Root,
            )

        rule.setContent {
            lastItemText = itemDisplayName(LmuWindowsReadoutItemKey.MyBestLap.Root)
            Box(modifier = Modifier.height(240.dp)) {
                ReadoutListPane(
                    uiState =
                        ReadoutListUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            items = items,
                            readoutEnabledStates = items.associateWith { true },
                        ),
                    onMove = { _, _ -> },
                    onReadoutEnabledChanged = { _, _ -> },
                    onQueueEnabledChanged = { _, _ -> },
                    onStartSoundEnabledChanged = { _, _ -> },
                    onItemClick = {},
                )
            }
        }

        rule.onNode(hasScrollAction()).performScrollToNode(hasText(lastItemText))
        rule.onNodeWithText(scrollToTopText).assertExists().performClick()

        rule.waitUntil {
            rule.onAllNodes(hasText(priorityHintLabelText)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `scrollToTopRequestが増えるとリストを先頭へ戻す`() {
        val priorityHintLabelText = "読み上げ優先度"
        var lastItemText by mutableStateOf("")
        var scrollToTopRequest by mutableIntStateOf(0)
        val items =
            listOf(
                LmuWindowsReadoutItemKey.Flag.Root,
                LmuWindowsReadoutItemKey.Flag.BlueFlag,
                LmuWindowsReadoutItemKey.Flag.SectorYellowFlag,
                LmuWindowsReadoutItemKey.Flag.FullCourseYellow,
                LmuWindowsReadoutItemKey.Flag.RedFlag,
                LmuWindowsReadoutItemKey.VehicleApproach.Root,
                LmuWindowsReadoutItemKey.VehicleDamage.Root,
                LmuWindowsReadoutItemKey.VehicleDamage.Overheat,
                LmuWindowsReadoutItemKey.TyreTemperature.Root,
                LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning,
                LmuWindowsReadoutItemKey.TyreTemperature.LowWarning,
                LmuWindowsReadoutItemKey.MyBestLap.Root,
            )

        rule.setContent {
            lastItemText = itemDisplayName(LmuWindowsReadoutItemKey.MyBestLap.Root)
            Box(modifier = Modifier.height(240.dp)) {
                ReadoutListPane(
                    uiState =
                        ReadoutListUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            items = items,
                            readoutEnabledStates = items.associateWith { true },
                        ),
                    onMove = { _, _ -> },
                    onReadoutEnabledChanged = { _, _ -> },
                    onQueueEnabledChanged = { _, _ -> },
                    onStartSoundEnabledChanged = { _, _ -> },
                    onItemClick = {},
                    scrollToTopRequest = scrollToTopRequest,
                )
            }
        }

        rule.onNode(hasScrollAction()).performScrollToNode(hasText(lastItemText))
        rule.runOnIdle { scrollToTopRequest++ }

        rule.waitUntil {
            rule.onAllNodes(hasText(priorityHintLabelText)).fetchSemanticsNodes().isNotEmpty()
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
