package kurou.kodriver.feature.telemetrylogdetail

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: TelemetryLogRepository

    private lateinit var viewModel: TelemetryLogDetailViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = TelemetryLogDetailViewModel(
            observeTelemetryLogDetail = ObserveTelemetryLogDetailUseCase(repository),
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiStateの初期値は空の項目を持つ`() = runTest {
        assertEquals(TelemetryLogDetailUiState(), viewModel.uiState.first())
        confirmVerified(repository)
    }

    @Test
    fun `setLogIdでログIDを保持する`() = runTest {
        every { repository.observeTelemetryLogDetail(10L) } returns flowOf(null)

        viewModel.setLogId(10)

        assertEquals(
            TelemetryLogDetailUiState(logId = 10),
            viewModel.uiState.first { it.logId == 10L },
        )
        verify(exactly = 1) { repository.observeTelemetryLogDetail(10L) }
        confirmVerified(repository)
    }

    @Test
    fun `選択したログと一つ前のログのJSONを表示項目に変換する`() = runTest {
        val current = telemetryLog(id = 2L, createdAt = 200L, telemetryJson = """{"speed":120}""")
        val previous = telemetryLog(id = 1L, createdAt = 100L, telemetryJson = """{"speed":118}""")
        every { repository.observeTelemetryLogDetail(2L) } returns
            flowOf(TelemetryLogDetail(current = current, previous = previous))

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
        verify(exactly = 1) { repository.observeTelemetryLogDetail(2L) }
        confirmVerified(repository)
    }

    @Test
    fun `ログの更新を観測する`() = runTest {
        val detailFlow = MutableStateFlow(
            TelemetryLogDetail(
                current = telemetryLog(id = 1L, createdAt = 100L, telemetryJson = """{"speed":118}"""),
                previous = null,
            ),
        )
        every { repository.observeTelemetryLogDetail(1L) } returns detailFlow

        viewModel.setLogId(1L)
        assertEquals(
            """{"speed":118}""",
            viewModel.uiState.first { it.items.isNotEmpty() }.items.single().telemetryJson,
        )

        detailFlow.update {
            it.copy(current = telemetryLog(id = 1L, createdAt = 100L, telemetryJson = """{"speed":119}"""))
        }

        assertEquals(
            """{"speed":119}""",
            viewModel.uiState
                .first { it.items.single().telemetryJson == """{"speed":119}""" }
                .items
                .single()
                .telemetryJson,
        )
        verify(exactly = 1) { repository.observeTelemetryLogDetail(1L) }
        confirmVerified(repository)
    }

    @Test
    fun `選択したログが存在しない場合は項目を空にする`() = runTest {
        every { repository.observeTelemetryLogDetail(999L) } returns flowOf(null)

        viewModel.setLogId(999L)

        assertEquals(
            TelemetryLogDetailUiState(logId = 999L),
            viewModel.uiState.first { it.logId == 999L },
        )
        verify(exactly = 1) { repository.observeTelemetryLogDetail(999L) }
        confirmVerified(repository)
    }
}

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
