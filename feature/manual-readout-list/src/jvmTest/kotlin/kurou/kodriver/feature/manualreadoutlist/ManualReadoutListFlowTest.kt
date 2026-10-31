package kurou.kodriver.feature.manualreadoutlist

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ManualReadoutListFlowTest {
    init {
        registerAllManualReadoutSimulators()
    }

    @Test
    fun `入力状態を反映し存在しない選択セットIDを除外する`() =
        runTest {
            val sets = MutableStateFlow(listOf(ManualReadoutSet(id = "set-1")))
            val selectedSetId = MutableStateFlow<String?>("set-1")
            val playButtonLayoutEnabled = MutableStateFlow(false)
            val flow = ManualReadoutListFlow(sets, selectedSetId, playButtonLayoutEnabled, Simulator.LmuWindows.id)

            assertEquals(
                ManualReadoutListUiState(
                    selectedSetId = "set-1",
                    canReadSets = true,
                    sets = sets.value,
                    playButtonLayoutEnabled = false,
                ),
                flow.uiState.first(),
            )

            sets.update { emptyList() }
            assertNull(flow.uiState.first().selectedSetId)

            selectedSetId.update { "missing" }
            sets.update { listOf(ManualReadoutSet(id = "set-2")) }
            assertNull(flow.uiState.first().selectedSetId)

            selectedSetId.update { "set-2" }
            playButtonLayoutEnabled.update { true }
            assertEquals("set-2", flow.uiState.first().selectedSetId)
            assertEquals(true, flow.uiState.first().playButtonLayoutEnabled)
        }

    @Test
    fun `読み上げ可能なシミュレーターだけcanReadSetsを有効にする`() =
        runTest {
            Simulator.entries.forEach { simulator ->
                val flow =
                    ManualReadoutListFlow(
                        sets = MutableStateFlow(emptyList()),
                        selectedSetId = MutableStateFlow(null),
                        playButtonLayoutEnabled = MutableStateFlow(false),
                        simulatorId = simulator.id,
                    )

                assertEquals(
                    simulator in setOf(Simulator.LmuWindows, Simulator.Gt7Ps5, Simulator.AceWindows),
                    flow.uiState.first().canReadSets,
                )
            }

            val unknownSimulatorFlow =
                ManualReadoutListFlow(
                    sets = MutableStateFlow(emptyList()),
                    selectedSetId = MutableStateFlow(null),
                    playButtonLayoutEnabled = MutableStateFlow(false),
                    simulatorId = "unknown",
                )
            assertEquals(false, unknownSimulatorFlow.uiState.first().canReadSets)
        }
}
