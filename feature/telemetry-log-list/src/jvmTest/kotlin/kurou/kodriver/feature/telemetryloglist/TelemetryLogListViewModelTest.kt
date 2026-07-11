package kurou.kodriver.feature.telemetryloglist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.model.TelemetryLogDetail
import kurou.kodriver.domain.repository.TelemetryLogRepository
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

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ログを新しい順で表示する`() = runTest(dispatcher) {
        val repository = FakeTelemetryLogRepository()
        val viewModel = TelemetryLogListViewModel(
            observeTelemetryLogs = ObserveTelemetryLogsUseCase(repository),
            resetTelemetryLogDatabase = ResetTelemetryLogDatabaseUseCase(repository),
        )

        repository.emit(
            listOf(
                telemetryLog(id = 1, createdAt = 100),
                telemetryLog(id = 3, createdAt = 200),
                telemetryLog(id = 2, createdAt = 200),
            ),
        )

        assertEquals(
            listOf(3L, 2L, 1L),
            viewModel.uiState.first { it.logs.isNotEmpty() }.logs.map { it.id },
        )
    }

    @Test
    fun `ログの更新を観測する`() = runTest(dispatcher) {
        val repository = FakeTelemetryLogRepository()
        val viewModel = TelemetryLogListViewModel(
            observeTelemetryLogs = ObserveTelemetryLogsUseCase(repository),
            resetTelemetryLogDatabase = ResetTelemetryLogDatabaseUseCase(repository),
        )

        repository.emit(listOf(telemetryLog(id = 1, createdAt = 100)))
        assertEquals(listOf(1L), viewModel.uiState.first { it.logs.isNotEmpty() }.logs.map { it.id })

        repository.emit(
            listOf(
                telemetryLog(id = 1, createdAt = 100),
                telemetryLog(id = 2, createdAt = 300),
            ),
        )
        assertEquals(listOf(2L, 1L), viewModel.uiState.first { it.logs.firstOrNull()?.id == 2L }.logs.map { it.id })
    }

    @Test
    fun `selectLogで未選択のログIDを選択する`() = runTest(dispatcher) {
        val repository = FakeTelemetryLogRepository()
        val viewModel = TelemetryLogListViewModel(
            observeTelemetryLogs = ObserveTelemetryLogsUseCase(repository),
            resetTelemetryLogDatabase = ResetTelemetryLogDatabaseUseCase(repository),
        )

        repository.emit(listOf(telemetryLog(id = 1, createdAt = 100)))
        viewModel.selectLog(1)

        assertEquals(1L, viewModel.uiState.first { it.selectedLogId == 1L }.selectedLogId)
    }

    @Test
    fun `selectLogで選択済みのログIDを再選択すると選択状態を解除する`() = runTest(dispatcher) {
        val repository = FakeTelemetryLogRepository()
        val viewModel = TelemetryLogListViewModel(
            observeTelemetryLogs = ObserveTelemetryLogsUseCase(repository),
            resetTelemetryLogDatabase = ResetTelemetryLogDatabaseUseCase(repository),
        )

        repository.emit(listOf(telemetryLog(id = 1, createdAt = 100)))
        viewModel.selectLog(1)
        viewModel.uiState.first { it.selectedLogId == 1L }
        viewModel.selectLog(1)

        assertNull(viewModel.uiState.first { it.selectedLogId == null }.selectedLogId)
    }

    @Test
    fun `clearSelectedLogで選択状態を解除する`() = runTest(dispatcher) {
        val repository = FakeTelemetryLogRepository()
        val viewModel = TelemetryLogListViewModel(
            observeTelemetryLogs = ObserveTelemetryLogsUseCase(repository),
            resetTelemetryLogDatabase = ResetTelemetryLogDatabaseUseCase(repository),
        )

        repository.emit(listOf(telemetryLog(id = 1, createdAt = 100)))
        viewModel.selectLog(1)
        viewModel.uiState.first { it.selectedLogId == 1L }
        viewModel.clearSelectedLog()

        assertNull(viewModel.uiState.first { it.selectedLogId == null }.selectedLogId)
    }

    @Test
    fun `選択中のログが一覧から消えた場合は選択状態を解除する`() = runTest(dispatcher) {
        val repository = FakeTelemetryLogRepository()
        val viewModel = TelemetryLogListViewModel(
            observeTelemetryLogs = ObserveTelemetryLogsUseCase(repository),
            resetTelemetryLogDatabase = ResetTelemetryLogDatabaseUseCase(repository),
        )

        repository.emit(listOf(telemetryLog(id = 1, createdAt = 100)))
        viewModel.selectLog(1)
        viewModel.uiState.first { it.selectedLogId == 1L }
        repository.emit(emptyList())

        assertNull(viewModel.uiState.first { it.logs.isEmpty() }.selectedLogId)
    }

    @Test
    fun `resetDatabaseに成功するとisResettingがfalseに戻りresetSucceededがtrueになる`() = runTest(dispatcher) {
        val repository = FakeTelemetryLogRepository()
        val viewModel = TelemetryLogListViewModel(
            observeTelemetryLogs = ObserveTelemetryLogsUseCase(repository),
            resetTelemetryLogDatabase = ResetTelemetryLogDatabaseUseCase(repository),
        )

        repository.emit(listOf(telemetryLog(id = 1, createdAt = 100)))
        viewModel.uiState.first { it.logs.isNotEmpty() }

        viewModel.resetDatabase()

        val state = viewModel.uiState.first { it.resetSucceeded != null && it.logs.isEmpty() }
        assertEquals(true, state.resetSucceeded)
        assertFalse(state.isResetting)
    }

    @Test
    fun `resetDatabaseが失敗するとresetSucceededがfalseになる`() = runTest(dispatcher) {
        val repository = FakeTelemetryLogRepository(shouldFailOnDelete = true)
        val viewModel = TelemetryLogListViewModel(
            observeTelemetryLogs = ObserveTelemetryLogsUseCase(repository),
            resetTelemetryLogDatabase = ResetTelemetryLogDatabaseUseCase(repository),
        )

        viewModel.resetDatabase()

        val state = viewModel.uiState.first { it.resetSucceeded != null }
        assertEquals(false, state.resetSucceeded)
        assertFalse(state.isResetting)
    }

    @Test
    fun `onResetClickで確認ダイアログを表示する`() = runTest(dispatcher) {
        val repository = FakeTelemetryLogRepository()
        val viewModel = TelemetryLogListViewModel(
            observeTelemetryLogs = ObserveTelemetryLogsUseCase(repository),
            resetTelemetryLogDatabase = ResetTelemetryLogDatabaseUseCase(repository),
        )

        viewModel.onResetClick()

        assertEquals(true, viewModel.uiState.first { it.showResetConfirmDialog }.showResetConfirmDialog)
    }

    @Test
    fun `onResetDismissで確認ダイアログを閉じる`() = runTest(dispatcher) {
        val repository = FakeTelemetryLogRepository()
        val viewModel = TelemetryLogListViewModel(
            observeTelemetryLogs = ObserveTelemetryLogsUseCase(repository),
            resetTelemetryLogDatabase = ResetTelemetryLogDatabaseUseCase(repository),
        )

        viewModel.onResetClick()
        viewModel.uiState.first { it.showResetConfirmDialog }
        viewModel.onResetDismiss()

        assertFalse(viewModel.uiState.first { !it.showResetConfirmDialog }.showResetConfirmDialog)
    }

    @Test
    fun `onResetConfirmでダイアログを閉じてresetDatabaseを実行する`() = runTest(dispatcher) {
        val repository = FakeTelemetryLogRepository()
        val viewModel = TelemetryLogListViewModel(
            observeTelemetryLogs = ObserveTelemetryLogsUseCase(repository),
            resetTelemetryLogDatabase = ResetTelemetryLogDatabaseUseCase(repository),
        )

        repository.emit(listOf(telemetryLog(id = 1, createdAt = 100)))
        viewModel.uiState.first { it.logs.isNotEmpty() }
        viewModel.onResetClick()
        viewModel.uiState.first { it.showResetConfirmDialog }

        viewModel.onResetConfirm()

        val state = viewModel.uiState.first { it.resetSucceeded != null }
        assertFalse(state.showResetConfirmDialog)
        assertEquals(true, state.resetSucceeded)
    }

    @Test
    fun `consumeResetResultでresetSucceededをnullに戻す`() = runTest(dispatcher) {
        val repository = FakeTelemetryLogRepository()
        val viewModel = TelemetryLogListViewModel(
            observeTelemetryLogs = ObserveTelemetryLogsUseCase(repository),
            resetTelemetryLogDatabase = ResetTelemetryLogDatabaseUseCase(repository),
        )

        viewModel.resetDatabase()
        viewModel.uiState.first { it.resetSucceeded != null }

        viewModel.consumeResetResult()

        assertNull(viewModel.uiState.first { it.resetSucceeded == null && !it.isResetting }.resetSucceeded)
    }
}

private fun telemetryLog(
    id: Long,
    createdAt: Long,
) = TelemetryLog(
    id = id,
    createdAt = createdAt,
    simulatorId = "lmu_windows",
    readoutItemKey = "flag",
    telemetryJson = "{}",
)

private class FakeTelemetryLogRepository(
    private val shouldFailOnDelete: Boolean = false,
) : TelemetryLogRepository {
    private val logs = MutableStateFlow(emptyList<TelemetryLog>())

    override fun observeTelemetryLogs() = logs

    override fun observeTelemetryLogDetail(id: Long) = logs.map { logs ->
        val current = logs.firstOrNull { it.id == id } ?: return@map null
        TelemetryLogDetail(current = current, previous = null)
    }

    override suspend fun saveTelemetryLog(log: TelemetryLog) {
        emit(logs.value + log)
    }

    override suspend fun deleteAllTelemetryLogs() {
        if (shouldFailOnDelete) throw IllegalStateException("削除に失敗しました")
        emit(emptyList())
    }

    fun emit(value: List<TelemetryLog>) {
        logs.update { value }
    }
}
