package kurou.kodriver.feature.telemetrylogdetail

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
import kurou.kodriver.domain.usecase.ObserveTelemetryLogDetailUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TelemetryLogDetailViewModelTest {

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
    fun `uiStateの初期値は空の項目を持つ`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        assertEquals(TelemetryLogDetailUiState(), viewModel.uiState.value)
    }

    @Test
    fun `setLogIdでログIDを保持する`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.setLogId(10)

        assertEquals(
            TelemetryLogDetailUiState(logId = 10),
            viewModel.uiState.first { it.logId == 10L },
        )
    }

    @Test
    fun `選択したログと一つ前のログのJSONを表示項目に変換する`() = runTest(dispatcher) {
        val repository = FakeTelemetryLogRepository()
        val viewModel = createViewModel(repository)

        repository.emit(
            listOf(
                telemetryLog(id = 1L, createdAt = 100L, telemetryJson = """{"speed":118}"""),
                telemetryLog(id = 2L, createdAt = 200L, telemetryJson = """{"speed":120}"""),
            ),
        )
        viewModel.setLogId(2L)

        assertEquals(
            TelemetryLogDetailUiState(
                logId = 2L,
                items = listOf(
                    TelemetryLogDetailItemUiState(
                        title = "選択したログ",
                        telemetryJson = """{"speed":120}""",
                    ),
                    TelemetryLogDetailItemUiState(
                        title = "一つ前のログ",
                        telemetryJson = """{"speed":118}""",
                    ),
                ),
            ),
            viewModel.uiState.first { it.items.size == 2 },
        )
    }

    @Test
    fun `ログの更新を観測する`() = runTest(dispatcher) {
        val repository = FakeTelemetryLogRepository(
            initialLogs = listOf(
                telemetryLog(id = 1L, createdAt = 100L, telemetryJson = """{"speed":118}"""),
            ),
        )
        val viewModel = createViewModel(repository)

        viewModel.setLogId(1L)
        assertEquals(
            """{"speed":118}""",
            viewModel.uiState.first { it.items.isNotEmpty() }.items.single().telemetryJson,
        )

        repository.emit(
            listOf(
                telemetryLog(id = 1L, createdAt = 100L, telemetryJson = """{"speed":119}"""),
            ),
        )

        assertEquals(
            """{"speed":119}""",
            viewModel.uiState
                .first { it.items.single().telemetryJson == """{"speed":119}""" }
                .items
                .single()
                .telemetryJson,
        )
    }

    @Test
    fun `選択したログが存在しない場合は項目を空にする`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.setLogId(999L)

        assertEquals(
            TelemetryLogDetailUiState(logId = 999L),
            viewModel.uiState.first { it.logId == 999L },
        )
    }
}

private fun createViewModel(
    repository: FakeTelemetryLogRepository = FakeTelemetryLogRepository(),
) = TelemetryLogDetailViewModel(
    observeTelemetryLogDetail = ObserveTelemetryLogDetailUseCase(repository),
)

private fun telemetryLog(
    id: Long,
    createdAt: Long,
    telemetryJson: String,
) = TelemetryLog(
    id = id,
    createdAt = createdAt,
    simulatorId = "lmu_windows",
    readoutItemKey = "flag",
    telemetryJson = telemetryJson,
)

private class FakeTelemetryLogRepository(
    initialLogs: List<TelemetryLog> = emptyList(),
) : TelemetryLogRepository {
    private val logs = MutableStateFlow(initialLogs)

    override fun observeTelemetryLogs() = logs

    override fun observeTelemetryLogDetail(id: Long) = logs.map { logs ->
        val sortedLogs = logs.sortedWith(
            compareByDescending<TelemetryLog> { it.createdAt }.thenByDescending { it.id },
        )
        val index = sortedLogs.indexOfFirst { it.id == id }
        if (index == -1) {
            null
        } else {
            TelemetryLogDetail(
                current = sortedLogs[index],
                previous = sortedLogs.getOrNull(index + 1),
            )
        }
    }

    override suspend fun saveTelemetryLog(log: TelemetryLog) {
        emit(logs.value + log)
    }

    fun emit(value: List<TelemetryLog>) {
        logs.update { value }
    }
}
