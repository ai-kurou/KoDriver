package kurou.kodriver.feature.telemetryloglist

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.core.model.Simulator
import kurou.kodriver.core.model.TelemetryLog
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.DeleteTelemetryLogUseCase
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

    @MockK
    private lateinit var repository: TelemetryLogRepository

    private val logsFlow = MutableStateFlow(emptyList<TelemetryLog>())

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
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
                ),
            resetTelemetryLogDatabase = ResetTelemetryLogDatabaseUseCase(repository),
            deleteTelemetryLog = DeleteTelemetryLogUseCase(repository),
        )

    @Test
    fun `ログの更新を観測する`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
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
    fun `selectLogで未選択のログIDを選択する`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
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
    fun `resetDatabaseに成功するとisResettingがfalseに戻りresetSucceededがtrueになる`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            coEvery { repository.deleteAllTelemetryLogs() } answers { logsFlow.update { emptyList() } }
            val viewModel = createViewModel()

            logsFlow.update { listOf(telemetryLog(id = 1, createdAt = 100)) }
            viewModel.uiState.first { it.logs.isNotEmpty() }

            viewModel.resetDatabase()

            val state = viewModel.uiState.first { it.resetSucceeded != null && it.logs.isEmpty() }
            assertEquals(true, state.resetSucceeded)
            assertFalse(state.isResetting)
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            coVerify(exactly = 1) { repository.deleteAllTelemetryLogs() }
            confirmVerified(repository)
        }

    @Test
    fun `resetDatabaseが失敗するとresetSucceededがfalseになる`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            coEvery { repository.deleteAllTelemetryLogs() } throws IllegalStateException("削除に失敗しました")
            val viewModel = createViewModel()

            viewModel.resetDatabase()

            val state = viewModel.uiState.first { it.resetSucceeded != null }
            assertEquals(false, state.resetSucceeded)
            assertFalse(state.isResetting)
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            coVerify(exactly = 1) { repository.deleteAllTelemetryLogs() }
            confirmVerified(repository)
        }

    @Test
    fun `onResetClickで確認ダイアログを表示する`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
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
            coEvery { repository.deleteAllTelemetryLogs() } answers { logsFlow.update { emptyList() } }
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
            coVerify(exactly = 1) { repository.deleteAllTelemetryLogs() }
            confirmVerified(repository)
        }

    @Test
    fun `consumeResetResultでresetSucceededをnullに戻す`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            coEvery { repository.deleteAllTelemetryLogs() } answers { logsFlow.update { emptyList() } }
            val viewModel = createViewModel()

            viewModel.resetDatabase()
            viewModel.uiState.first { it.resetSucceeded != null }

            viewModel.consumeResetResult()

            assertNull(viewModel.uiState.first { it.resetSucceeded == null && !it.isResetting }.resetSucceeded)
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            coVerify(exactly = 1) { repository.deleteAllTelemetryLogs() }
            confirmVerified(repository)
        }
}

private fun telemetryLog(
    id: Long,
    createdAt: Long,
) = TelemetryLog(
    id = id,
    createdAt = createdAt,
    simulator = Simulator.LmuWindows,
    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
    telemetryJson = "{}",
)
