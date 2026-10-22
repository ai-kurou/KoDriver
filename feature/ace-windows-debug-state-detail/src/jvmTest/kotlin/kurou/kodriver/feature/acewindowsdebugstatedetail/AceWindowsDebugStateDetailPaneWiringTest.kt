package kurou.kodriver.feature.acewindowsdebugstatedetail

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
import kurou.kodriver.domain.acewindows.model.AceWindowsCarLocation
import kurou.kodriver.domain.acewindows.model.AceWindowsStatusData
import kurou.kodriver.domain.acewindows.model.AceWindowsStatusType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.registerAceWindowsReadoutItemKeys
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
class AceWindowsDebugStateDetailPaneWiringTest {
    @Before
    fun `ReadoutItemKeyを登録する`() {
        registerAceWindowsReadoutItemKeys()
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

    private val viewModel: AceWindowsDebugStateDetailViewModel = mockk()

    private val onBack: () -> Unit = mockk()

    @Test
    fun `PaneはKoinの状態を表示しカード移動と戻る操作を通知する`() {
        val cardOrder = listOf(AceWindowsDebugStateCardKey.SIMULATOR, AceWindowsDebugStateCardKey.VEHICLE_LOCATION)
        val state =
            MutableStateFlow(
                AceWindowsDebugStateDetailUiState(
                    cardOrder = cardOrder,
                    enabledCardKeys = cardOrder.toSet(),
                    aceWindows =
                        AceWindowsDebugState(
                            status = AceWindowsStatusData(AceWindowsStatusType.LIVE, AceWindowsCarLocation.TRACK),
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
                    AceWindowsDebugStateDetailPane(
                        true,
                        onBack,
                        Modifier.requiredSize(360.dp, 640.dp).testTag("ace-debug-pane"),
                    )
                }
            }
            rule.onNodeWithTag("ace-debug-pane").assertExists()
            rule.onNodeWithText("Assetto Corsa EVO（Windows版）").assertIsDisplayed()
            rule.onNodeWithText("Track（コース上）").assertIsDisplayed()

            val grid = rule.onNodeWithTag(DEBUG_STATE_GRID_TEST_TAG)
            val gridTopLeft = grid.fetchSemanticsNode().boundsInRoot.topLeft
            val from =
                rule
                    .onNodeWithText("選択中のシミュレーター")
                    .fetchSemanticsNode()
                    .boundsInRoot.center - gridTopLeft
            val to =
                rule
                    .onNodeWithText("車両位置")
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
    fun `Paneは状態の更新を表示し戻れない場合は戻る操作を隠す`() {
        val state =
            MutableStateFlow(
                AceWindowsDebugStateDetailUiState(cardOrder = listOf(AceWindowsDebugStateCardKey.VEHICLE_LOCATION)),
            )
        excludeRecords { viewModel.uiState }
        every { viewModel.uiState } returns state
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                KoDriverTheme {
                    AceWindowsDebugStateDetailPane(false, onBack)
                }
            }
            rule.onNodeWithText("未取得").assertIsDisplayed()
            rule.onNodeWithContentDescription("戻る").assertDoesNotExist()
            state.update {
                it.copy(
                    aceWindows =
                        AceWindowsDebugState(
                            status = AceWindowsStatusData(AceWindowsStatusType.LIVE, AceWindowsCarLocation.PITLANE),
                        ),
                    enabledCardKeys =
                        setOf(
                            AceWindowsDebugStateCardKey.SIMULATOR,
                            AceWindowsDebugStateCardKey.VEHICLE_LOCATION,
                        ),
                )
            }
            rule.onNodeWithText("PitLane（ピットレーン）").assertIsDisplayed()

            verify(exactly = 0) { onBack() }
            confirmVerified(viewModel, onBack)
        } finally {
            stopKoin()
        }
    }

    @Test
    fun `固定カードはグリッドの上にあり項目選択と入力と送信をViewModelへ通知する`() {
        val keys = ReadoutItemKey.topLevelEntriesOf(Simulator.AceWindows)
        val state = MutableStateFlow(AceWindowsDebugStateDetailUiState())
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
                    AceWindowsDebugStateDetailPane(
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
