package kurou.kodriver.feature.acewindowsdebugstatedetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagData
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagType
import kurou.kodriver.domain.model.ACE_WINDOWS_READOUT_ITEM_KEYS
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.registerAceWindowsReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsDebugStateDetailFlowTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerAceWindowsReadoutItemKeys()
    }

    @Test
    fun `既定入力からUiStateを合成しデータ更新を反映する`() =
        runTest {
            val cardOrder = MutableStateFlow(defaultDebugStateCardOrder)
            val received = MutableStateFlow(setOf(AceWindowsDebugStateCardKey.SIMULATOR))
            val aceWindows = MutableStateFlow(AceWindowsDebugState())
            val selected = MutableStateFlow(AceWindowsDebugStateDetailUiState().selectedReadoutItemKey)
            val text = MutableStateFlow("")
            val composition =
                AceWindowsDebugStateDetailFlow(
                    cardOrder = cardOrder,
                    receivedCardKeys = received,
                    aceWindows = aceWindows,
                    selectedReadoutItemKey = selected,
                    debugReadoutText = text,
                )
            val uiState = composition.uiState(composition.header)
            val states = mutableListOf<AceWindowsDebugStateDetailUiState>()
            backgroundScope.launch { uiState.collect { states.add(it) } }
            testScheduler.runCurrent()

            assertEquals(AceWindowsDebugStateDetailUiState(), states.last())

            val order = defaultDebugStateCardOrder.reversed()
            val aceState = AceWindowsDebugState(flag = AceWindowsFlagData(AceWindowsFlagType.GREEN_FLAG))
            cardOrder.update { order }
            received.update { setOf(AceWindowsDebugStateCardKey.SIMULATOR, AceWindowsDebugStateCardKey.FLAG_INFO) }
            aceWindows.update { aceState }
            selected.update { ACE_WINDOWS_READOUT_ITEM_KEYS.filterIsInstance<ReadoutItemKey.TopLevel>().last() }
            text.update { "テスト読み上げ" }
            testScheduler.runCurrent()

            assertEquals(
                AceWindowsDebugStateDetailUiState(
                    selectedReadoutItemKey = selected.value,
                    debugReadoutText = "テスト読み上げ",
                    aceWindows = aceState,
                    enabledCardKeys = received.value,
                    cardOrder = order,
                ),
                states.last(),
            )
        }

    @Test
    fun `受信済みカードがない場合は有効カードを空にする`() =
        runTest {
            val flow =
                AceWindowsDebugStateDetailFlow(
                    cardOrder = MutableStateFlow(defaultDebugStateCardOrder),
                    receivedCardKeys = MutableStateFlow(emptySet()),
                    aceWindows = MutableStateFlow(AceWindowsDebugState()),
                    selectedReadoutItemKey = MutableStateFlow(AceWindowsDebugStateDetailUiState().selectedReadoutItemKey),
                    debugReadoutText = MutableStateFlow(""),
                )

            assertEquals(emptySet(), flow.uiState(flow.header).first().enabledCardKeys)
        }
}
