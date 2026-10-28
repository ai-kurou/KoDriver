package kurou.kodriver.feature.telemetrylogdetail

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.NarrationSource
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.model.TelemetryLogDetail
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.ObserveTelemetryLogDetailUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TelemetryLogDetailFlowTest {
    private val repository: TelemetryLogRepository = mockk()

    @Test
    fun `購読開始まで詳細を取得せずID変更で旧購読をキャンセルする`() =
        runTest {
            val selectedLogId = MutableStateFlow<Long?>(1L)
            val firstDetail = MutableStateFlow<TelemetryLogDetail?>(null)
            val secondDetail = MutableStateFlow<TelemetryLogDetail?>(null)
            val completedIds = mutableListOf<Long>()
            every { repository.observeTelemetryLogDetail(1L) } returns
                firstDetail.onCompletion { completedIds.add(1L) }
            every { repository.observeTelemetryLogDetail(2L) } returns
                secondDetail.onCompletion { completedIds.add(2L) }
            val flow = TelemetryLogDetailFlow(selectedLogId, ObserveTelemetryLogDetailUseCase(repository))
            verify(exactly = 0) { repository.observeTelemetryLogDetail(1L) }
            verify(exactly = 0) { repository.observeTelemetryLogDetail(2L) }
            val states = mutableListOf<TelemetryLogDetailUiState>()
            val job = backgroundScope.launch { flow.uiState.collect { states.add(it) } }
            runCurrent()
            assertEquals(listOf(TelemetryLogDetailUiState(logId = 1L)), states)

            selectedLogId.update { 2L }
            runCurrent()
            assertEquals(listOf(1L), completedIds)
            assertEquals(TelemetryLogDetailUiState(logId = 2L), states.last())

            firstDetail.update {
                TelemetryLogDetail(
                    current = telemetryLog(id = 1L, createdAt = 100L, telemetryJson = "旧ログの更新"),
                    previous = null,
                )
            }
            runCurrent()
            assertEquals(2, states.size)

            secondDetail.update {
                TelemetryLogDetail(
                    current = telemetryLog(id = 2L, createdAt = 200L, telemetryJson = "新ログの更新"),
                    previous = null,
                )
            }
            runCurrent()
            assertEquals(
                TelemetryLogDetailUiState(
                    logId = 2L,
                    items = listOf(TelemetryLogDetailItemUiState(TelemetryLogDetailItemKind.SELECTED, "新ログの更新")),
                ),
                states.last(),
            )

            selectedLogId.update { null }
            runCurrent()
            assertEquals(listOf(1L, 2L), completedIds)
            assertEquals(TelemetryLogDetailUiState(), states.last())
            job.cancel()
            verify(exactly = 1) { repository.observeTelemetryLogDetail(1L) }
            verify(exactly = 1) { repository.observeTelemetryLogDetail(2L) }
            confirmVerified(repository)
        }

    @Test
    fun `uiStateの初期値は空の項目を持つ`() =
        runTest {
            val selectedLogId = MutableStateFlow<Long?>(null)
            val flow = TelemetryLogDetailFlow(selectedLogId, ObserveTelemetryLogDetailUseCase(repository))
            assertEquals(TelemetryLogDetailUiState(), flow.uiState.first())
            confirmVerified(repository)
        }

    @Test
    fun `選択したログと一つ前のログのJSONを表示項目に変換する`() =
        runTest {
            val selectedLogId = MutableStateFlow<Long?>(null)
            val flow = TelemetryLogDetailFlow(selectedLogId, ObserveTelemetryLogDetailUseCase(repository))
            val current = telemetryLog(id = 2L, createdAt = 200L, telemetryJson = """{"speed":120}""")
            val previous = telemetryLog(id = 1L, createdAt = 100L, telemetryJson = """{"speed":118}""")
            every { repository.observeTelemetryLogDetail(2L) } returns
                flowOf(TelemetryLogDetail(current = current, previous = previous))

            selectedLogId.update { 2L }

            assertEquals(
                TelemetryLogDetailUiState(
                    logId = 2L,
                    items =
                        listOf(
                            TelemetryLogDetailItemUiState(
                                kind = TelemetryLogDetailItemKind.SELECTED,
                                telemetryJson = """{"speed":120}""",
                            ),
                            TelemetryLogDetailItemUiState(
                                kind = TelemetryLogDetailItemKind.PREVIOUS,
                                telemetryJson = """{"speed":118}""",
                            ),
                        ),
                ),
                flow.uiState.first { it.items.size == 2 },
            )
            verify(exactly = 1) { repository.observeTelemetryLogDetail(2L) }
            confirmVerified(repository)
        }

    @Test
    fun `選択したログが存在しない場合は項目を空にする`() =
        runTest {
            val selectedLogId = MutableStateFlow<Long?>(null)
            val flow = TelemetryLogDetailFlow(selectedLogId, ObserveTelemetryLogDetailUseCase(repository))
            every { repository.observeTelemetryLogDetail(999L) } returns flowOf(null)

            selectedLogId.update { 999L }

            assertEquals(
                TelemetryLogDetailUiState(logId = 999L),
                flow.uiState.first { it.logId == 999L },
            )
            verify(exactly = 1) { repository.observeTelemetryLogDetail(999L) }
            confirmVerified(repository)
        }

    @Test
    fun `ace_windowsの選択したログと一つ前のログのJSONを表示項目に変換する`() =
        runTest {
            val selectedLogId = MutableStateFlow<Long?>(null)
            val flow = TelemetryLogDetailFlow(selectedLogId, ObserveTelemetryLogDetailUseCase(repository))
            val current =
                telemetryLog(
                    id = 2L,
                    createdAt = 200L,
                    telemetryJson = """{"remainingFuelLiters":8.2}""",
                    simulator = Simulator.AceWindows,
                )
            val previous =
                telemetryLog(
                    id = 1L,
                    createdAt = 100L,
                    telemetryJson = """{"remainingFuelLiters":8.5}""",
                    simulator = Simulator.AceWindows,
                )
            every { repository.observeTelemetryLogDetail(2L) } returns
                flowOf(TelemetryLogDetail(current = current, previous = previous))

            selectedLogId.update { 2L }

            assertEquals(
                TelemetryLogDetailUiState(
                    logId = 2L,
                    items =
                        listOf(
                            TelemetryLogDetailItemUiState(
                                kind = TelemetryLogDetailItemKind.SELECTED,
                                telemetryJson = """{"remainingFuelLiters":8.2}""",
                            ),
                            TelemetryLogDetailItemUiState(
                                kind = TelemetryLogDetailItemKind.PREVIOUS,
                                telemetryJson = """{"remainingFuelLiters":8.5}""",
                            ),
                        ),
                ),
                flow.uiState.first { it.items.size == 2 },
            )
            verify(exactly = 1) { repository.observeTelemetryLogDetail(2L) }
            confirmVerified(repository)
        }
}

private fun telemetryLog(
    id: Long,
    createdAt: Long,
    telemetryJson: String,
    simulator: Simulator = Simulator.LmuWindows,
) = TelemetryLog(
    id = id,
    createdAt = createdAt,
    simulator = simulator,
    readoutItemKey =
        if (simulator == Simulator.AceWindows) {
            AceWindowsReadoutItemKey.RemainingFuel.Root
        } else {
            LmuWindowsReadoutItemKey.Flag.Root
        },
    narratedText = "イエローフラッグ",
    narrationOutcome = NarrationOutcome.INTERRUPTED,
    narrationSource = NarrationSource.AUTO,
    telemetryJson = telemetryJson,
)
