package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsDebugStateDetailPaneWiringTest {
    @Before
    fun `ReadoutItemKeyを登録する`() {
        registerLmuWindowsReadoutItemKeys()
    }

    private val rule = createComposeRule()

    @get:Rule
    val rules: TestRule =
        RuleChain
            .outerRule(
                object : ExternalResource() {
                    override fun before() {
                        Dispatchers.setMain(UnconfinedTestDispatcher())
                    }

                    override fun after() {
                        Dispatchers.resetMain()
                    }
                },
            ).around(rule)

    private val viewModel: LmuWindowsDebugStateDetailViewModel = mockk()

    private val onBack: () -> Unit = mockk()

    @Test
    fun `PaneはKoinの状態を表示しカード移動と戻る操作を通知する`() {
        val cardOrder = listOf(LmuWindowsDebugStateCardKey.SIMULATOR, LmuWindowsDebugStateCardKey.VEHICLE_CLASS)
        val state =
            MutableStateFlow(
                LmuWindowsDebugStateDetailUiState(
                    cardOrder = cardOrder,
                    enabledCardKeys = cardOrder.toSet(),
                    lmuWindows =
                        LmuWindowsDebugState(
                            vehicleClass = LmuWindowsVehicleClassData.fromRawValue("Hypercar"),
                        ),
                ),
            )
        every { viewModel.uiState } returns state
        every { viewModel.moveCard(0, 1) } answers {
            state.update { it.copy(cardOrder = cardOrder.reversed()) }
        }
        every { onBack() } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                KoDriverTheme {
                    LmuWindowsDebugStateDetailPane(
                        true,
                        onBack,
                        Modifier.requiredSize(360.dp, 640.dp).testTag("lmu-debug-pane"),
                    )
                }
            }
            rule.onNodeWithTag("lmu-debug-pane").assertExists()
            rule.onNodeWithText("Le Mans Ultimate（Windows版）").assertIsDisplayed()
            rule.onNodeWithText("Hypercar").assertIsDisplayed()

            val grid = rule.onNodeWithTag(DEBUG_STATE_GRID_TEST_TAG)
            val gridTopLeft = grid.fetchSemanticsNode().boundsInRoot.topLeft
            val from =
                rule
                    .onNodeWithText("選択中のシミュレーター")
                    .fetchSemanticsNode()
                    .boundsInRoot.center - gridTopLeft
            val to =
                rule
                    .onNodeWithText("車両クラス")
                    .fetchSemanticsNode()
                    .boundsInRoot.center - gridTopLeft
            grid.performTouchInput { down(from) }
            rule.mainClock.advanceTimeBy(1_000)
            grid.performTouchInput {
                repeat(10) { step ->
                    moveTo(from + Offset(0f, (to.y - from.y) * (step + 1) / 10))
                }
                up()
            }
            rule.waitForIdle()
            rule.onNodeWithContentDescription("戻る").performClick()

            // 再コンポーズ回数に依存するため下限のみ検証する
            verify(atLeast = 1) { viewModel.uiState }
            verify(exactly = 1) { viewModel.moveCard(0, 1) }
            verify(exactly = 1) { onBack() }
            confirmVerified(viewModel, onBack)
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `Paneは状態の更新を表示し戻れない場合は戻る操作を隠す`() {
        val state =
            MutableStateFlow(
                LmuWindowsDebugStateDetailUiState(cardOrder = listOf(LmuWindowsDebugStateCardKey.VEHICLE_CLASS)),
            )
        every { viewModel.uiState } returns state
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                KoDriverTheme {
                    LmuWindowsDebugStateDetailPane(false, onBack)
                }
            }
            rule.onNodeWithText("未取得").assertIsDisplayed()
            rule.onNodeWithContentDescription("戻る").assertDoesNotExist()
            state.update {
                it.copy(
                    lmuWindows = LmuWindowsDebugState(vehicleClass = LmuWindowsVehicleClassData.fromRawValue("LMP2")),
                    enabledCardKeys =
                        setOf(
                            LmuWindowsDebugStateCardKey.SIMULATOR,
                            LmuWindowsDebugStateCardKey.VEHICLE_CLASS,
                        ),
                )
            }
            rule.onNodeWithText("LMP2").assertIsDisplayed()

            verify(atLeast = 1) { viewModel.uiState }
            verify(exactly = 0) { onBack() }
            confirmVerified(viewModel, onBack)
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `ホットキーカードはグリッド内でキー選択と動作確認をViewModelへ通知する`() {
        val state =
            MutableStateFlow(
                LmuWindowsDebugStateDetailUiState(
                    readoutHotkey = ReadoutHotkeyUiState(registrationState = HotkeyRegistrationState.Registered),
                ),
            )
        excludeRecords { viewModel.uiState }
        every { viewModel.uiState } returns state
        every { viewModel.selectHotkey(0x71) } answers {
            state.update { it.copy(readoutHotkey = it.readoutHotkey.copy(virtualKeyCode = 0x71)) }
        }
        every { viewModel.checkHotkey() } answers {
            state.update { it.copy(readoutHotkey = it.readoutHotkey.copy(checkState = HotkeyCheckState.Waiting)) }
        }
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                KoDriverTheme {
                    LmuWindowsDebugStateDetailPane(false, {}, Modifier.requiredSize(1560.dp, 1080.dp))
                }
            }
            val card = rule.onNodeWithTag("readout_hotkey_card").fetchSemanticsNode().boundsInRoot
            val grid = rule.onNodeWithTag(DEBUG_STATE_GRID_TEST_TAG).fetchSemanticsNode().boundsInRoot
            assertEquals(true, card.top >= grid.top && card.bottom <= grid.bottom)
            rule.onNodeWithTag("readout_hotkey_key").performClick()
            rule.onNodeWithText("キーを押してください").assertIsDisplayed()
            rule.onNodeWithTag("readout_hotkey_key").performKeyInput {
                pressKey(Key.ShiftLeft)
                pressKey(Key.Enter)
                pressKey(Key.F2)
            }
            rule.onNodeWithText("Ctrl + Alt + Shift + F2").assertIsDisplayed()
            rule.onNodeWithText("動作確認").performClick()
            rule.onNodeWithText("ホットキーを押してください（最大10秒）").assertIsDisplayed()
            verify(exactly = 1) { viewModel.selectHotkey(0x71) }
            verify(exactly = 1) { viewModel.checkHotkey() }
            confirmVerified(viewModel)
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `読み上げ要求カードはグリッド内にあり項目選択と入力と送信をViewModelへ通知する`() {
        val keys = ReadoutItemKey.topLevelEntriesOf(Simulator.LmuWindows)
        val state = MutableStateFlow(LmuWindowsDebugStateDetailUiState())
        excludeRecords { viewModel.uiState }
        every { viewModel.uiState } returns state
        every { viewModel.selectReadoutItem(keys.last().value) } answers {
            state.update { it.copy(selectedReadoutItemKey = keys.last()) }
        }
        every { viewModel.updateDebugReadoutText("入力 {laps}") } answers {
            state.update { it.copy(debugReadoutText = "入力 {laps}") }
        }
        every { viewModel.requestReadout() } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                KoDriverTheme {
                    LmuWindowsDebugStateDetailPane(
                        canNavigateBack = false,
                        onBack = {},
                        modifier = Modifier.requiredSize(1560.dp, 1080.dp),
                    )
                }
            }
            val card = rule.onNodeWithTag("debug_readout_request_card").fetchSemanticsNode().boundsInRoot
            val grid = rule.onNodeWithTag(DEBUG_STATE_GRID_TEST_TAG).fetchSemanticsNode().boundsInRoot
            assertEquals(true, card.top >= grid.top && card.bottom <= grid.bottom)
            rule.onNodeWithTag("debug_readout_request_item").performClick()
            rule.onNodeWithTag("debug_readout_item_${keys.last().value}").performClick()
            rule.onNodeWithTag("debug_readout_request_text").performTextReplacement("入力 {laps}")
            rule.onNodeWithText("読み上げ").performClick()
            verify(exactly = 1) { viewModel.selectReadoutItem(keys.last().value) }
            verify(exactly = 1) { viewModel.updateDebugReadoutText("入力 {laps}") }
            verify(exactly = 1) { viewModel.requestReadout() }
            confirmVerified(viewModel)
        } finally {
            stopKoin()
        }
    }
}
