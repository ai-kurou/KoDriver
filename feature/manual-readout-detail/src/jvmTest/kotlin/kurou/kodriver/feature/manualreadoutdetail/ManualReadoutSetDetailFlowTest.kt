package kurou.kodriver.feature.manualreadoutdetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ManualReadoutSetDetailFlowTest {
    @Test
    fun `初期状態と対象セットの状態を変換する`() =
        runTest {
            val sets = MutableStateFlow(listOf(ManualReadoutSet(id = "target", number = 0)))
            val deleted = MutableStateFlow(false)
            val conflict = MutableStateFlow<ManualReadoutHotkeyConflict?>(null)
            val debugHotkey = MutableStateFlow<Int?>(null)
            val registration = MutableStateFlow<HotkeyRegistrationState>(HotkeyRegistrationState.Registered)
            val ttsAvailable = MutableStateFlow(true)
            val previewing = MutableStateFlow(false)
            val flow = createFlow(sets, deleted, conflict, debugHotkey, registration, ttsAvailable, previewing)

            assertEquals(
                ManualReadoutSetDetailUiState(simulatorId = Simulator.LmuWindows.id, availableItemKeys = listOf("key")),
                flow.initialUiState,
            )
            assertEquals(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 1,
                    availableItemKeys = listOf("key"),
                    hotkeyRegistrationState = HotkeyRegistrationState.Registered,
                    isTextToSpeechAvailable = true,
                    isLoading = false,
                ),
                flow.uiState.first(),
            )

            sets.update {
                listOf(
                    ManualReadoutSet(
                        id = "target",
                        number = 7,
                        name = "読み上げ",
                        itemKeys = listOf("key"),
                        itemTexts = mapOf("key" to "文言"),
                        hotkeyVirtualKeyCode = 65,
                        hotkeyModifiers = ManualReadoutHotkeyModifiers.CTRL_ALT,
                    ),
                )
            }
            deleted.update { true }
            previewing.update { true }
            assertEquals(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 7,
                    name = "読み上げ",
                    availableItemKeys = listOf("key"),
                    selectedItemKeys = listOf("key"),
                    itemTexts = mapOf("key" to "文言"),
                    hotkeyModifiers = ManualReadoutHotkeyModifiers.CTRL_ALT,
                    hotkeyVirtualKeyCode = 65,
                    hotkeyRegistrationState = HotkeyRegistrationState.Registered,
                    isTextToSpeechAvailable = true,
                    isPreviewing = true,
                    isLoading = false,
                    deleted = true,
                ),
                flow.uiState.first(),
            )
        }

    @Test
    fun `セット不在時はセット情報を空にする`() =
        runTest {
            val flow =
                createFlow(
                    MutableStateFlow(listOf(ManualReadoutSet(id = "other"))),
                    MutableStateFlow(false),
                    MutableStateFlow<ManualReadoutHotkeyConflict?>(null),
                    MutableStateFlow<Int?>(null),
                    MutableStateFlow(HotkeyRegistrationState.Unsupported),
                    MutableStateFlow(false),
                    MutableStateFlow(false),
                )
            val state = flow.uiState.first()
            assertNull(state.setNumber)
            assertEquals("", state.name)
            assertEquals(emptyList(), state.selectedItemKeys)
            assertEquals(emptyMap(), state.itemTexts)
            assertNull(state.hotkeyVirtualKeyCode)
        }

    @Test
    fun `保存済みデバッグ競合は同じキーかつ既定修飾キーの場合だけ表示する`() =
        runTest {
            val sets = MutableStateFlow(listOf(ManualReadoutSet(id = "target", hotkeyVirtualKeyCode = 65)))
            val debugHotkey = MutableStateFlow<Int?>(65)
            val flow = createFlow(sets, debugHotkey = debugHotkey)

            assertEquals(ManualReadoutHotkeyConflict.DebugHotkey, flow.uiState.first().hotkeyConflict)
            debugHotkey.update { 66 }
            assertNull(flow.uiState.first().hotkeyConflict)
            debugHotkey.update { 65 }
            sets.update { listOf(it.single().copy(hotkeyModifiers = ManualReadoutHotkeyModifiers.CTRL_ALT)) }
            assertNull(flow.uiState.first().hotkeyConflict)
        }

    @Test
    fun `ローカル競合は保存済みデバッグ競合より優先する`() =
        runTest {
            val flow =
                createFlow(
                    MutableStateFlow(listOf(ManualReadoutSet(id = "target", hotkeyVirtualKeyCode = 65))),
                    conflict = MutableStateFlow(ManualReadoutHotkeyConflict.OtherSet(Simulator.Gt7Ps5.id, 2)),
                    debugHotkey = MutableStateFlow(65),
                )
            assertEquals(
                ManualReadoutHotkeyConflict.OtherSet(Simulator.Gt7Ps5.id, 2),
                flow.uiState.first().hotkeyConflict,
            )
        }

    @Test
    fun `TTSと試聴中の状態変更を反映する`() =
        runTest {
            val ttsAvailable = MutableStateFlow(false)
            val previewing = MutableStateFlow(false)
            val flow = createFlow(ttsAvailable = ttsAvailable, previewing = previewing)

            assertEquals(false to false, flow.uiState.first().let { it.isTextToSpeechAvailable to it.isPreviewing })
            ttsAvailable.update { true }
            previewing.update { true }
            assertEquals(true to true, flow.uiState.first().let { it.isTextToSpeechAvailable to it.isPreviewing })
        }

    private fun createFlow(
        sets: MutableStateFlow<List<ManualReadoutSet>> = MutableStateFlow(emptyList()),
        deleted: MutableStateFlow<Boolean> = MutableStateFlow(false),
        conflict: MutableStateFlow<ManualReadoutHotkeyConflict?> = MutableStateFlow(null),
        debugHotkey: MutableStateFlow<Int?> = MutableStateFlow(null),
        registration: MutableStateFlow<HotkeyRegistrationState> = MutableStateFlow(HotkeyRegistrationState.Unsupported),
        ttsAvailable: MutableStateFlow<Boolean> = MutableStateFlow(false),
        previewing: MutableStateFlow<Boolean> = MutableStateFlow(false),
    ) = ManualReadoutSetDetailFlow(
        sets = sets,
        deleted = deleted,
        hotkeyConflict = conflict,
        debugHotkeyVirtualKeyCode = debugHotkey,
        hotkeyRegistrationState = registration,
        textToSpeechAvailable = ttsAvailable,
        isPreviewing = previewing,
        simulatorId = Simulator.LmuWindows.id,
        setId = "target",
        availableItemKeys = listOf("key"),
    )
}
