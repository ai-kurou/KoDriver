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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
    fun `selectLogで選択したログIDを保持する`() = runTest(dispatcher) {
        val repository = FakeTelemetryLogRepository()
        val viewModel = TelemetryLogListViewModel(
            observeTelemetryLogs = ObserveTelemetryLogsUseCase(repository),
        )

        repository.emit(listOf(telemetryLog(id = 1, createdAt = 100)))
        viewModel.selectLog(1)

        assertEquals(1L, viewModel.uiState.first { it.selectedLogId == 1L }.selectedLogId)
    }

    @Test
    fun `clearSelectedLogで選択状態を解除する`() = runTest(dispatcher) {
        val repository = FakeTelemetryLogRepository()
        val viewModel = TelemetryLogListViewModel(
            observeTelemetryLogs = ObserveTelemetryLogsUseCase(repository),
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
        )

        repository.emit(listOf(telemetryLog(id = 1, createdAt = 100)))
        viewModel.selectLog(1)
        viewModel.uiState.first { it.selectedLogId == 1L }
        repository.emit(emptyList())

        assertNull(viewModel.uiState.first { it.logs.isEmpty() }.selectedLogId)
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

private class FakeTelemetryLogRepository : TelemetryLogRepository {
    private val logs = MutableStateFlow(emptyList<TelemetryLog>())

    override fun observeTelemetryLogs() = logs

    override fun observeTelemetryLogDetail(id: Long) = logs.map { logs ->
        val current = logs.firstOrNull { it.id == id } ?: return@map null
        TelemetryLogDetail(current = current, previous = null)
    }

    override suspend fun saveTelemetryLog(log: TelemetryLog) {
        emit(logs.value + log)
    }

    fun emit(value: List<TelemetryLog>) {
        logs.update { value }
    }
}
