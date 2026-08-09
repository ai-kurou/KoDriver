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

/**
 * [TelemetryLogListViewModel] の削除フロー（onDeleteClick/onDeleteConfirm/onDeleteDismiss/consumeDeleteResult）のテスト。
 * detekt の TooManyFunctions（20/20）に抵触するため [TelemetryLogListViewModelTest] から分割している。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TelemetryLogListViewModelDeleteTest {
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
    fun `onDeleteClickで削除対象のログIDを保持する`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            val viewModel = createViewModel()

            viewModel.onDeleteClick(1L)

            assertEquals(1L, viewModel.uiState.first { it.pendingDeleteLogId == 1L }.pendingDeleteLogId)
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            confirmVerified(repository)
        }

    @Test
    fun `onDeleteDismissで削除対象のログIDを解除する`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            val viewModel = createViewModel()

            viewModel.onDeleteClick(1L)
            viewModel.uiState.first { it.pendingDeleteLogId == 1L }
            viewModel.onDeleteDismiss()

            assertNull(viewModel.uiState.first { it.pendingDeleteLogId == null }.pendingDeleteLogId)
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            confirmVerified(repository)
        }

    @Test
    fun `onDeleteConfirmで削除対象のログIDを解除してdeleteTelemetryLogを実行する`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            coEvery { repository.deleteTelemetryLog(1L) } answers { logsFlow.update { emptyList() } }
            val viewModel = createViewModel()

            logsFlow.update { listOf(telemetryLog(id = 1, createdAt = 100)) }
            viewModel.uiState.first { it.logs.isNotEmpty() }
            viewModel.onDeleteClick(1L)
            viewModel.uiState.first { it.pendingDeleteLogId == 1L }

            viewModel.onDeleteConfirm()

            val state = viewModel.uiState.first { it.deleteSucceeded != null }
            assertNull(state.pendingDeleteLogId)
            assertEquals(true, state.deleteSucceeded)
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            coVerify(exactly = 1) { repository.deleteTelemetryLog(1L) }
            confirmVerified(repository)
        }

    @Test
    fun `deleteTelemetryLogが失敗するとdeleteSucceededがfalseになる`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            coEvery { repository.deleteTelemetryLog(1L) } throws IllegalStateException("削除に失敗しました")
            val viewModel = createViewModel()

            viewModel.onDeleteClick(1L)
            viewModel.uiState.first { it.pendingDeleteLogId == 1L }
            viewModel.onDeleteConfirm()

            val state = viewModel.uiState.first { it.deleteSucceeded != null }
            assertEquals(false, state.deleteSucceeded)
            assertFalse(state.isDeleting)
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            coVerify(exactly = 1) { repository.deleteTelemetryLog(1L) }
            confirmVerified(repository)
        }

    @Test
    fun `pendingDeleteLogIdがnullの状態でonDeleteConfirmを呼んでもdeleteTelemetryLogは実行されない`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            val viewModel = createViewModel()

            viewModel.onDeleteConfirm()

            assertNull(viewModel.uiState.first().pendingDeleteLogId)
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            confirmVerified(repository)
        }

    @Test
    fun `consumeDeleteResultでdeleteSucceededをnullに戻す`() =
        runTest(dispatcher) {
            every { repository.observeTelemetryLogs() } returns logsFlow
            coEvery { repository.deleteTelemetryLog(1L) } answers { logsFlow.update { emptyList() } }
            val viewModel = createViewModel()

            viewModel.onDeleteClick(1L)
            viewModel.onDeleteConfirm()
            viewModel.uiState.first { it.deleteSucceeded != null }

            viewModel.consumeDeleteResult()

            assertNull(viewModel.uiState.first { it.deleteSucceeded == null && !it.isDeleting }.deleteSucceeded)
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            coVerify(exactly = 1) { repository.deleteTelemetryLog(1L) }
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
