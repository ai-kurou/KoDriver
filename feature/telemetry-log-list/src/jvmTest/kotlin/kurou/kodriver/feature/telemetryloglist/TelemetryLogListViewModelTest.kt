package kurou.kodriver.feature.telemetryloglist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.DeleteTelemetryLogUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveTelemetryLogsUseCase
import kurou.kodriver.domain.usecase.ResetTelemetryLogDatabaseUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class TelemetryLogListViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    private val repository: TelemetryLogRepository = mockk()
    private val simulatorRepository: SimulatorPreferencesRepository = mockk()
    private val selectedSimulator = MutableStateFlow<Simulator>(Simulator.LmuWindows)

    private val logsFlow = MutableStateFlow(emptyList<TelemetryLog>())

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        TelemetryLogListViewModel(
            observeSortedTelemetryLogs =
                ObserveSortedTelemetryLogsUseCase(
                    ObserveTelemetryLogsUseCase(repository),
                    ObserveSelectedSimulatorUseCase(simulatorRepository),
                ),
            observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
            resetTelemetryLogDatabase = ResetTelemetryLogDatabaseUseCase(repository),
            deleteTelemetryLog = DeleteTelemetryLogUseCase(repository),
        )

    @Test
    fun `ログの更新を観測する`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            every { simulatorRepository.selectedSimulator() } returns selectedSimulator
            val viewModel = createViewModel()

            logsFlow.update { listOf(telemetryLog(id = 1, createdAt = 100)) }
            assertEquals(
                listOf(1L),
                viewModel.uiState
                    .first { it.logs.isNotEmpty() }
                    .logs
                    .map { it.id },
            )

            logsFlow.update {
                listOf(
                    telemetryLog(id = 1, createdAt = 100),
                    telemetryLog(id = 2, createdAt = 300),
                )
            }
            assertEquals(
                listOf(2L, 1L),
                viewModel.uiState
                    .first { it.logs.firstOrNull()?.id == 2L }
                    .logs
                    .map { it.id },
            )
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            confirmVerified(repository)
        }

    @Test
    fun `選択中のace_windowsとそのログを観測する`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            every { simulatorRepository.selectedSimulator() } returns selectedSimulator
            selectedSimulator.update { Simulator.AceWindows }
            val viewModel = createViewModel()

            logsFlow.update {
                listOf(telemetryLog(id = 1, createdAt = 100, simulator = Simulator.AceWindows))
            }

            assertEquals(
                listOf(Simulator.AceWindows),
                viewModel.uiState
                    .first { it.logs.isNotEmpty() }
                    .logs
                    .map { it.simulator },
            )
            assertEquals(Simulator.AceWindows, viewModel.uiState.first().selectedSimulator)
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            confirmVerified(repository)
        }

    @Test
    fun `selectLogで未選択のログIDを選択する`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            every { simulatorRepository.selectedSimulator() } returns selectedSimulator
            val viewModel = createViewModel()

            logsFlow.update { listOf(telemetryLog(id = 1, createdAt = 100)) }
            viewModel.selectLog(1)

            assertEquals(1L, viewModel.uiState.first { it.selectedLogId == 1L }.selectedLogId)
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            confirmVerified(repository)
        }

    @Test
    fun `selectLogで選択済みのログIDを再選択すると選択状態を解除する`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            every { simulatorRepository.selectedSimulator() } returns selectedSimulator
            val viewModel = createViewModel()

            logsFlow.update { listOf(telemetryLog(id = 1, createdAt = 100)) }
            viewModel.selectLog(1)
            viewModel.uiState.first { it.selectedLogId == 1L }
            viewModel.selectLog(1)

            assertNull(viewModel.uiState.first { it.selectedLogId == null }.selectedLogId)
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            confirmVerified(repository)
        }

    @Test
    fun `clearSelectedLogで選択状態を解除する`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            every { simulatorRepository.selectedSimulator() } returns selectedSimulator
            val viewModel = createViewModel()

            logsFlow.update { listOf(telemetryLog(id = 1, createdAt = 100)) }
            viewModel.selectLog(1)
            viewModel.uiState.first { it.selectedLogId == 1L }
            viewModel.clearSelectedLog()

            assertNull(viewModel.uiState.first { it.selectedLogId == null }.selectedLogId)
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            confirmVerified(repository)
        }

    @Test
    fun `選択中のログが一覧から消えた場合は選択状態を解除する`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            every { simulatorRepository.selectedSimulator() } returns selectedSimulator
            val viewModel = createViewModel()

            logsFlow.update { listOf(telemetryLog(id = 1, createdAt = 100)) }
            viewModel.selectLog(1)
            viewModel.uiState.first { it.selectedLogId == 1L }
            logsFlow.update { emptyList() }

            assertNull(viewModel.uiState.first { it.logs.isEmpty() }.selectedLogId)
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            confirmVerified(repository)
        }

    @Test
    fun `シミュレーターを切り替えると一覧と選択中シミュレーターが更新されログ選択を解除する`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            every { simulatorRepository.selectedSimulator() } returns selectedSimulator
            logsFlow.update {
                listOf(
                    telemetryLog(id = 1, createdAt = 100),
                    telemetryLog(id = 2, createdAt = 300, simulator = Simulator.AceWindows),
                )
            }
            val viewModel = createViewModel()
            viewModel.selectLog(1)
            val initial = viewModel.uiState.first { it.selectedLogId == 1L }
            assertEquals(Simulator.LmuWindows, initial.selectedSimulator)
            assertEquals(listOf(1L), initial.logs.map { it.id })

            selectedSimulator.update { Simulator.AceWindows }

            val state =
                viewModel.uiState.first {
                    it.selectedSimulator == Simulator.AceWindows && it.logs.firstOrNull()?.id == 2L
                }
            assertEquals(listOf(2L), state.logs.map { it.id })
            assertNull(state.selectedLogId)

            selectedSimulator.update { Simulator.LmuWindows }
            runCurrent()

            val restored =
                viewModel.uiState.first {
                    it.selectedSimulator == Simulator.LmuWindows && it.logs.firstOrNull()?.id == 1L
                }
            assertNull(restored.selectedLogId)
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            verify(exactly = 2) { simulatorRepository.selectedSimulator() }
            confirmVerified(repository, simulatorRepository)
        }

    @Test
    fun `初回購読と同じシミュレーターでの再購読ではログ選択を維持する`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            every { simulatorRepository.selectedSimulator() } returns selectedSimulator
            logsFlow.update { listOf(telemetryLog(id = 1, createdAt = 100)) }
            val viewModel = createViewModel()
            viewModel.selectLog(1)

            assertEquals(1L, viewModel.uiState.first { it.logs.isNotEmpty() }.selectedLogId)
            advanceTimeBy(5_001)
            runCurrent()

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
            runCurrent()

            assertEquals(1L, viewModel.uiState.first().selectedLogId)
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            verify(exactly = 2) { simulatorRepository.selectedSimulator() }
            confirmVerified(repository, simulatorRepository)
        }

    @Test
    fun `resetDatabaseは選択中のAceWindowsを渡して削除し成功状態を返す`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            every { simulatorRepository.selectedSimulator() } returns selectedSimulator
            coEvery { repository.deleteTelemetryLogsBySimulator(Simulator.AceWindows) } answers {
                logsFlow.update { logs -> logs.filterNot { it.simulator == Simulator.AceWindows } }
            }
            val viewModel = createViewModel()
            selectedSimulator.update { Simulator.AceWindows }

            logsFlow.update {
                listOf(
                    telemetryLog(id = 1, createdAt = 100, simulator = Simulator.AceWindows),
                    telemetryLog(id = 2, createdAt = 200),
                )
            }
            viewModel.uiState.first { it.logs.isNotEmpty() }

            viewModel.resetDatabase()

            val state = viewModel.uiState.first { it.resetSucceeded != null && it.logs.isEmpty() }
            assertEquals(true, state.resetSucceeded)
            assertFalse(state.isResetting)
            assertEquals(listOf(telemetryLog(id = 2, createdAt = 200)), logsFlow.first())
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            coVerify(exactly = 1) { repository.deleteTelemetryLogsBySimulator(Simulator.AceWindows) }
            confirmVerified(repository)
        }

    @Test
    fun `resetDatabaseが失敗するとresetSucceededがfalseになる`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            every { simulatorRepository.selectedSimulator() } returns selectedSimulator
            coEvery { repository.deleteTelemetryLogsBySimulator(Simulator.LmuWindows) } throws
                IllegalStateException("削除に失敗しました")
            val viewModel = createViewModel()

            viewModel.resetDatabase()

            val state = viewModel.uiState.first { it.resetSucceeded != null }
            assertEquals(false, state.resetSucceeded)
            assertFalse(state.isResetting)
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            coVerify(exactly = 1) { repository.deleteTelemetryLogsBySimulator(Simulator.LmuWindows) }
            confirmVerified(repository)
        }

    @Test
    fun `onResetClickで確認ダイアログを表示する`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            every { simulatorRepository.selectedSimulator() } returns selectedSimulator
            val viewModel = createViewModel()

            viewModel.onResetClick()

            assertEquals(true, viewModel.uiState.first { it.showResetConfirmDialog }.showResetConfirmDialog)
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            confirmVerified(repository)
        }

    @Test
    fun `onResetDismissで確認ダイアログを閉じる`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            every { simulatorRepository.selectedSimulator() } returns selectedSimulator
            val viewModel = createViewModel()

            viewModel.onResetClick()
            viewModel.uiState.first { it.showResetConfirmDialog }
            viewModel.onResetDismiss()

            assertFalse(viewModel.uiState.first { !it.showResetConfirmDialog }.showResetConfirmDialog)
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            confirmVerified(repository)
        }

    @Test
    fun `onResetConfirmでダイアログを閉じてresetDatabaseを実行する`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            every { simulatorRepository.selectedSimulator() } returns selectedSimulator
            coEvery { repository.deleteTelemetryLogsBySimulator(Simulator.LmuWindows) } answers {
                logsFlow.update { emptyList() }
            }
            val viewModel = createViewModel()

            logsFlow.update { listOf(telemetryLog(id = 1, createdAt = 100)) }
            viewModel.uiState.first { it.logs.isNotEmpty() }
            viewModel.onResetClick()
            viewModel.uiState.first { it.showResetConfirmDialog }

            viewModel.onResetConfirm()

            val state = viewModel.uiState.first { it.resetSucceeded != null }
            assertFalse(state.showResetConfirmDialog)
            assertEquals(true, state.resetSucceeded)
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            coVerify(exactly = 1) { repository.deleteTelemetryLogsBySimulator(Simulator.LmuWindows) }
            confirmVerified(repository)
        }

    @Test
    fun `consumeResetResultでresetSucceededをnullに戻す`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            every { simulatorRepository.selectedSimulator() } returns selectedSimulator
            coEvery { repository.deleteTelemetryLogsBySimulator(Simulator.LmuWindows) } answers {
                logsFlow.update { emptyList() }
            }
            val viewModel = createViewModel()

            viewModel.resetDatabase()
            viewModel.uiState.first { it.resetSucceeded != null }

            viewModel.consumeResetResult()

            assertNull(viewModel.uiState.first { it.resetSucceeded == null && !it.isResetting }.resetSucceeded)
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            coVerify(exactly = 1) { repository.deleteTelemetryLogsBySimulator(Simulator.LmuWindows) }
            confirmVerified(repository)
        }
}

private fun telemetryLog(
    id: Long,
    createdAt: Long,
    simulator: Simulator = Simulator.LmuWindows,
) = TelemetryLog(
    id = id,
    createdAt = createdAt,
    simulator = simulator,
    readoutItemKey =
        if (simulator == Simulator.AceWindows) {
            AceWindowsReadoutItemKey.Flag.Root
        } else {
            LmuWindowsReadoutItemKey.Flag.Root
        },
    narratedText = "イエローフラッグ",
    narrationOutcome = NarrationOutcome.INTERRUPTED,
    telemetryJson = "{}",
)
