package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
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
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5VehicleClassData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.registerGt7Ps5ReadoutItemKeys
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
class Gt7Ps5DebugStateDetailPaneWiringTest {
    @Before
    fun registerReadoutKeys() {
        registerGt7Ps5ReadoutItemKeys()
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

    private val viewModel: Gt7Ps5DebugStateDetailViewModel = mockk()

    private val onBack: () -> Unit = mockk()

    @Test
    fun `PaneはKoinの状態を表示しカード移動と戻る操作を通知する`() {
        val cardOrder = listOf(Gt7Ps5DebugStateCardKey.SIMULATOR, Gt7Ps5DebugStateCardKey.VEHICLE_CLASS)
        val state =
            MutableStateFlow(
                Gt7Ps5DebugStateDetailUiState(
                    cardOrder = cardOrder,
                    enabledCardKeys = cardOrder.toSet(),
                    gt7Ps5 =
                        Gt7Ps5DebugState(
                            vehicleClass = Gt7Ps5VehicleClassData("GR3"),
                        ),
                ),
            )
        excludeRecords { viewModel.uiState }
        every { viewModel.uiState } returns state
        every { viewModel.moveCard(0, 1) } answers {
            state.update { it.copy(cardOrder = cardOrder.reversed()) }
        }
        every { onBack() } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                KoDriverTheme {
                    Gt7Ps5DebugStateDetailPane(
                        true,
                        onBack,
                        Modifier.requiredSize(360.dp, 640.dp).testTag("gt7-debug-pane"),
                    )
                }
            }
            rule.onNodeWithTag("gt7-debug-pane").assertExists()
            rule.onNodeWithText("Gran Turismo 7（PS5）").assertIsDisplayed()
            rule.onNodeWithText("GR3").assertIsDisplayed()

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

            // 状態の getter は表示用スタブ。操作のみを呼び出し検証の対象にする。
            verify(exactly = 1) { viewModel.moveCard(0, 1) }
            verify(exactly = 1) { onBack() }
            confirmVerified(viewModel, onBack)
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `PaneはKoinの状態と更新を表示して戻る操作を通知する`() {
        val state = MutableStateFlow(Gt7Ps5DebugStateDetailUiState())
        every { viewModel.uiState } returns state
        every { onBack() } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                KoDriverTheme {
                    Gt7Ps5DebugStateDetailPane(
                        true,
                        onBack,
                        Modifier.requiredSize(900.dp, 600.dp).testTag("gt7-debug-pane"),
                    )
                }
            }
            rule.onNodeWithTag("gt7-debug-pane").assertExists()
            rule.onNodeWithText("Gran Turismo 7（PS5）").assertIsDisplayed()
            rule.runOnIdle {
                state.update {
                    it.copy(
                        enabledCardKeys = gt7Ps5SupportedCardKeys,
                        gt7Ps5 = Gt7Ps5DebugState(sampleGt7Ps5Telemetry(), Gt7Ps5VehicleClassData("GR3")),
                    )
                }
            }
            rule.onNodeWithText("GR3").assertIsDisplayed()
            rule.onNodeWithText("1:30.000").assertIsDisplayed()
            rule.onNodeWithContentDescription("戻る").performClick()

            // 状態取得は表示のためのスタブ。戻るコールバックの配線を検証する。
            verify(exactly = 1) { onBack() }
            confirmVerified(onBack)
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `固定カードはグリッドの上にあり項目選択と入力と送信をViewModelへ通知する`() {
        val keys = ReadoutItemKey.topLevelEntriesOf(Simulator.Gt7Ps5)
        val state = MutableStateFlow(Gt7Ps5DebugStateDetailUiState())
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
                    Gt7Ps5DebugStateDetailPane(
                        canNavigateBack = false,
                        onBack = {},
                        modifier = Modifier.requiredSize(1560.dp, 1080.dp),
                    )
                }
            }
            val card = rule.onNodeWithTag("debug_readout_request_card").fetchSemanticsNode().boundsInRoot
            val grid = rule.onNodeWithTag(DEBUG_STATE_GRID_TEST_TAG).fetchSemanticsNode().boundsInRoot
            assertEquals(true, card.bottom <= grid.top)
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
