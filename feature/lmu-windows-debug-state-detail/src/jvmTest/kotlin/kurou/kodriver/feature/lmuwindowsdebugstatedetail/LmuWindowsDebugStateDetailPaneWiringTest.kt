package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
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
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsDebugStateDetailPaneWiringTest {
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
                        Modifier.requiredSize(360.dp, 1080.dp).testTag("lmu-debug-pane"),
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
            rule.onNodeWithContentDescription("戻る").performSemanticsAction(SemanticsActions.OnClick)

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
    fun `Paneは文字列入力とキー選択と送信をViewModelへ渡す`() {
        val keys = ReadoutItemKey.entriesOf(Simulator.LmuWindows)
        val state = MutableStateFlow(LmuWindowsDebugStateDetailUiState(cardOrder = emptyList()))
        excludeRecords { viewModel.uiState }
        every { viewModel.uiState } returns state
        every { viewModel.onDebugOverlayTextChange("表示テスト") } answers {
            state.update { it.copy(debugOverlayText = "表示テスト") }
        }
        every { viewModel.onDebugOverlayKeySelect(keys[1]) } answers {
            state.update { it.copy(debugOverlaySelectedKey = keys[1]) }
        }
        every { viewModel.sendDebugOverlayText() } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                KoDriverTheme { LmuWindowsDebugStateDetailPane(false, onBack) }
            }
            rule.onNodeWithText("オーバーレイ表示テスト").assertIsDisplayed()
            rule.onNodeWithText("送信").assertIsNotEnabled()
            rule.onNodeWithText("表示する文字列").performTextInput("表示テスト")
            val initial = keys.first().value
            val selected = keys[1].value
            rule.onNodeWithText(" ($initial)", substring = true).performClick()
            rule.onNodeWithText(" ($selected)", substring = true).performClick()
            rule.onNodeWithText("送信").assertIsEnabled().performClick()
            verify(exactly = 1) { viewModel.onDebugOverlayTextChange("表示テスト") }
            verify(exactly = 1) { viewModel.onDebugOverlayKeySelect(keys[1]) }
            verify(exactly = 1) { viewModel.sendDebugOverlayText() }
            confirmVerified(viewModel)
        } finally {
            stopKoin()
        }
    }
}
