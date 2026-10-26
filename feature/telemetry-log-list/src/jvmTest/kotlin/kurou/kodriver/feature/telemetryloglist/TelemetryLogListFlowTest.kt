package kurou.kodriver.feature.telemetryloglist

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.NarrationSource
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class TelemetryLogListFlowTest {
    @Test
    fun `初期状態は入力された一覧と画面状態を合成する`() =
        runTest {
            val logs = MutableStateFlow(listOf(createTelemetryLog(id = 1)))
            val selectedLogId = MutableStateFlow<Long?>(1)
            val deleteState = MutableStateFlow(TelemetryLogListDeleteState())
            val pendingDeleteLogId = MutableStateFlow<Long?>(null)
            val selectedSimulator = MutableStateFlow<Simulator>(Simulator.LmuWindows)
            val flow = createFlow(logs, selectedLogId, deleteState, pendingDeleteLogId, selectedSimulator)

            assertEquals(
                TelemetryLogListUiState(
                    logs = logs.value,
                    selectedLogId = 1,
                    selectedSimulator = Simulator.LmuWindows,
                ),
                flow.uiState.first(),
            )
        }

    @Test
    fun `各入力の更新を画面状態へ反映し一覧にない選択IDを除外する`() =
        runTest {
            val logs = MutableStateFlow(listOf(createTelemetryLog(id = 1)))
            val selectedLogId = MutableStateFlow<Long?>(1)
            val deleteState = MutableStateFlow(TelemetryLogListDeleteState())
            val pendingDeleteLogId = MutableStateFlow<Long?>(null)
            val selectedSimulator = MutableStateFlow<Simulator>(Simulator.LmuWindows)
            val flow = createFlow(logs, selectedLogId, deleteState, pendingDeleteLogId, selectedSimulator)

            deleteState.update { TelemetryLogListDeleteState(isDeleting = true, deleteSucceeded = true) }
            pendingDeleteLogId.update { 1L }
            selectedSimulator.update { Simulator.AceWindows }
            val updated = flow.uiState.first { it.isDeleting && it.deleteSucceeded == true }
            assertEquals(1L, updated.pendingDeleteLogId)
            assertEquals(Simulator.AceWindows, updated.selectedSimulator)

            selectedLogId.update { 99L }
            assertNull(flow.uiState.first { it.selectedLogId == null }.selectedLogId)

            logs.update { listOf(createTelemetryLog(id = 2)) }
            selectedLogId.update { 2L }
            assertEquals(2L, flow.uiState.first { it.logs.singleOrNull()?.id == 2L }.selectedLogId)
        }

    private fun createFlow(
        logs: MutableStateFlow<List<TelemetryLog>>,
        selectedLogId: MutableStateFlow<Long?>,
        deleteState: MutableStateFlow<TelemetryLogListDeleteState>,
        pendingDeleteLogId: MutableStateFlow<Long?>,
        selectedSimulator: MutableStateFlow<Simulator>,
    ) = TelemetryLogListFlow(logs, selectedLogId, deleteState, pendingDeleteLogId, selectedSimulator)
}

private fun createTelemetryLog(id: Long) =
    TelemetryLog(
        id = id,
        createdAt = id,
        simulator = Simulator.LmuWindows,
        readoutItemKey = LmuWindowsReadoutItemKey.Flag.Root,
        narratedText = "イエローフラッグ",
        narrationOutcome = NarrationOutcome.INTERRUPTED,
        narrationSource = NarrationSource.AUTO,
        telemetryJson = "{}",
    )
