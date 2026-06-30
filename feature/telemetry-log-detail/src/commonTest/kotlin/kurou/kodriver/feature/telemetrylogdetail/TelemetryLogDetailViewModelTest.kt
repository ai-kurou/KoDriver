package kurou.kodriver.feature.telemetrylogdetail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.usecase.ObserveTelemetryLogDetailUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TelemetryLogDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeTelemetryLogRepository
    private lateinit var viewModel: TelemetryLogDetailViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTelemetryLogRepository()
        viewModel = createViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiStateの初期値は空の項目を持つ`() = runTest {
        assertEquals(TelemetryLogDetailUiState(), viewModel.uiState.value)
    }

    @Test
    fun `setLogIdでログIDを保持する`() = runTest {
        viewModel.setLogId(10)

        assertEquals(
            TelemetryLogDetailUiState(logId = 10),
            viewModel.uiState.first { it.logId == 10L },
        )
    }

    @Test
    fun `選択したログと一つ前のログのJSONを表示項目に変換する`() = runTest {
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
    fun `ログの更新を観測する`() = runTest {
        repository.emit(
            listOf(
                telemetryLog(id = 1L, createdAt = 100L, telemetryJson = """{"speed":118}"""),
            ),
        )

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
    fun `選択したログが存在しない場合は項目を空にする`() = runTest {
        viewModel.setLogId(999L)

        assertEquals(
            TelemetryLogDetailUiState(logId = 999L),
            viewModel.uiState.first { it.logId == 999L },
        )
    }
}

private fun createViewModel(
    repository: FakeTelemetryLogRepository,
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
