package kurou.kodriver.core.windowshotkeydata.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.windowshotkeydata.GlobalHotkey
import kurou.kodriver.core.windowshotkeydata.GlobalHotkeyEvent
import kurou.kodriver.core.windowshotkeydata.GlobalHotkeySource
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.MANUAL_READOUT_HOTKEY_MODIFIERS_DEFAULT
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ManualReadoutHotkeyRepositoryImplTest {
    private val lmu = FakeManualReadoutSetPreferencesRepository()
    private val gt7 = FakeManualReadoutSetPreferencesRepository()

    @Test
    fun `全シミュレーターの登録を共有し押下を再配信せず変更対象だけ登録し直す`() =
        runTest {
            val first = ManualReadoutSet("same-id", hotkeyVirtualKeyCode = 0x41)
            val second = ManualReadoutSet("same-id", hotkeyVirtualKeyCode = 0x42)
            val lmuSets = MutableStateFlow(listOf(first, ManualReadoutSet("unassigned")))
            val gt7Sets = MutableStateFlow(listOf(second))
            lmu.sets = lmuSets
            gt7.sets = gt7Sets
            val source = ManualRecordingSource()
            val repository =
                ManualReadoutHotkeyRepositoryImpl(source, mapOf("lmu" to lmu, "gt7" to gt7), backgroundScope)
            val presses = mutableListOf<ManualReadoutRequest>()
            val states = mutableListOf<HotkeyRegistrationState>()
            val stateCollector =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    repository.registrationStateStream("lmu", "same-id").collect { states.add(it) }
                }
            val pressCollector =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    repository.pressStream().collect { presses.add(it) }
                }
            testScheduler.runCurrent()
            assertEquals(listOf(0x41, 0x42), source.requested)
            assertEquals(HotkeyRegistrationState.Registered, states.last())
            assertEquals(
                HotkeyRegistrationState.Unsupported,
                repository.registrationStateStream("lmu", "unassigned").first(),
            )
            source.events.emit(0x41 to GlobalHotkeyEvent.Pressed)
            source.events.emit(0x42 to GlobalHotkeyEvent.Pressed)
            testScheduler.runCurrent()
            assertEquals(
                listOf(ManualReadoutRequest("lmu", "same-id"), ManualReadoutRequest("gt7", "same-id")),
                presses,
            )
            val late = mutableListOf<ManualReadoutRequest>()
            val lateCollector =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    repository.pressStream().collect { late.add(it) }
                }
            testScheduler.runCurrent()
            assertEquals(emptyList(), late)
            lmuSets.update { listOf(first.copy(itemTexts = mapOf("unknown" to "文言"))) }
            testScheduler.runCurrent()
            assertEquals(listOf(0x41, 0x42), source.requested)
            lmuSets.update { listOf(first.copy(hotkeyVirtualKeyCode = 0x43)) }
            testScheduler.runCurrent()
            assertEquals(listOf(0x41, 0x42, 0x43), source.requested)
            assertEquals(listOf(0x41), source.released)
            val defaultModifiers = MANUAL_READOUT_HOTKEY_MODIFIERS_DEFAULT.value
            val changedModifiers = ManualReadoutHotkeyModifiers.entries.first { it.value != defaultModifiers }
            lmuSets.update {
                listOf(first.copy(hotkeyVirtualKeyCode = 0x43, hotkeyModifiers = changedModifiers))
            }
            testScheduler.runCurrent()
            assertEquals(
                listOf(
                    GlobalHotkey(defaultModifiers, 0x41),
                    GlobalHotkey(defaultModifiers, 0x42),
                    GlobalHotkey(defaultModifiers, 0x43),
                    GlobalHotkey(changedModifiers.value, 0x43),
                ),
                source.hotkeys,
            )
            assertEquals(listOf(0x41, 0x43), source.released)
            lmuSets.update { listOf(first.copy(hotkeyVirtualKeyCode = null)) }
            testScheduler.runCurrent()
            assertEquals(HotkeyRegistrationState.Unsupported, states.last())
            gt7Sets.update { emptyList() }
            testScheduler.runCurrent()
            assertEquals(listOf(0x41, 0x43, 0x43, 0x42), source.released)
            stateCollector.cancel()
            pressCollector.cancel()
            lateCollector.cancel()
        }

    @Test
    fun `競合と失敗だけ対象セットを再試行し最後の購読終了で解除する`() =
        runTest {
            lmu.sets =
                MutableStateFlow(
                    listOf(
                        ManualReadoutSet("a", hotkeyVirtualKeyCode = 0x41),
                        ManualReadoutSet("b", hotkeyVirtualKeyCode = 0x42),
                    ),
                )
            for (failure in listOf(HotkeyRegistrationState.Conflict, HotkeyRegistrationState.Failed(5))) {
                val source = ManualRecordingSource(failure)
                val repository = ManualReadoutHotkeyRepositoryImpl(source, mapOf("lmu" to lmu), backgroundScope)
                val collector =
                    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                        repository.registrationStateStream("lmu", "a").collect { }
                    }
                testScheduler.runCurrent()
                assertEquals(failure, repository.registrationStateStream("lmu", "a").first())
                source.state = HotkeyRegistrationState.Registered
                repository.retryRegistration("lmu", "a")
                testScheduler.runCurrent()
                assertEquals(listOf(0x41, 0x42, 0x41), source.requested)
                assertEquals(listOf(0x41), source.released)
                assertEquals(HotkeyRegistrationState.Registered, repository.registrationStateStream("lmu", "a").first())
                repository.retryRegistration("lmu", "a")
                repository.retryRegistration("lmu", "missing")
                testScheduler.runCurrent()
                assertEquals(3, source.requested.size)
                collector.cancel()
                testScheduler.runCurrent()
                assertEquals(3, source.released.size)
                assertEquals(
                    HotkeyRegistrationState.Unsupported,
                    repository.registrationStateStream("lmu", "a").first(),
                )
                testScheduler.runCurrent()
            }
        }

    @Test
    fun `最後の購読終了で解除し再購読時は改めて登録する`() =
        runTest {
            lmu.sets =
                MutableStateFlow(listOf(ManualReadoutSet("a", hotkeyVirtualKeyCode = 0x41)))
            val source = ManualRecordingSource()
            val repository = ManualReadoutHotkeyRepositoryImpl(source, mapOf("lmu" to lmu), backgroundScope)
            val collector =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    repository.pressStream().collect { }
                }
            testScheduler.runCurrent()
            collector.cancel()
            testScheduler.runCurrent()
            assertEquals(listOf(0x41), source.released)
            val states = mutableListOf<HotkeyRegistrationState>()
            val second =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    repository.registrationStateStream("lmu", "a").collect { states.add(it) }
                }
            assertEquals(HotkeyRegistrationState.Unsupported, states.first())
            testScheduler.runCurrent()
            assertEquals(listOf(0x41, 0x41), source.requested)
            assertEquals(HotkeyRegistrationState.Registered, states.last())
            second.cancel()
            testScheduler.runCurrent()
            assertEquals(listOf(0x41, 0x41), source.released)
        }

    @Test
    fun `同一キーも個別登録し非対応と登録中は再試行せず空の構成も扱う`() =
        runTest {
            lmu.sets =
                MutableStateFlow(
                    listOf(
                        ManualReadoutSet("a", hotkeyVirtualKeyCode = 0x41),
                        ManualReadoutSet("b", hotkeyVirtualKeyCode = 0x41),
                    ),
                )
            for (state in listOf(HotkeyRegistrationState.Unsupported, HotkeyRegistrationState.Registering)) {
                val source = ManualRecordingSource(state)
                val repository = ManualReadoutHotkeyRepositoryImpl(source, mapOf("lmu" to lmu), backgroundScope)
                val collector =
                    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                        repository.pressStream().collect { }
                    }
                testScheduler.runCurrent()
                repository.retryRegistration("lmu", "a")
                testScheduler.runCurrent()
                assertEquals(listOf(0x41, 0x41), source.requested)
                collector.cancel()
                testScheduler.runCurrent()
            }
            val source = ManualRecordingSource()
            val empty = ManualReadoutHotkeyRepositoryImpl(source, emptyMap(), backgroundScope)
            assertEquals(HotkeyRegistrationState.Unsupported, empty.registrationStateStream("lmu", "missing").first())
            testScheduler.runCurrent()
            assertTrue(source.requested.isEmpty())
        }
}

private class ManualRecordingSource(
    var state: HotkeyRegistrationState = HotkeyRegistrationState.Registered,
) : GlobalHotkeySource {
    val events = MutableSharedFlow<Pair<Int, GlobalHotkeyEvent>>()
    val hotkeys = mutableListOf<GlobalHotkey>()
    val requested = mutableListOf<Int>()
    val released = mutableListOf<Int>()

    override fun observe(hotkey: GlobalHotkey): Flow<GlobalHotkeyEvent> =
        flow {
            hotkeys.add(hotkey)
            requested.add(hotkey.virtualKeyCode)
            try {
                emit(GlobalHotkeyEvent.Registration(state))
                events.collect { (key, event) -> if (key == hotkey.virtualKeyCode) emit(event) }
            } finally {
                released.add(hotkey.virtualKeyCode)
            }
        }
}

private class FakeManualReadoutSetPreferencesRepository : ManualReadoutSetPreferencesRepository {
    var sets: Flow<List<ManualReadoutSet>> = MutableStateFlow(emptyList())

    override fun observeSets(): Flow<List<ManualReadoutSet>> = sets

    override suspend fun addSet(set: ManualReadoutSet) = Unit

    override suspend fun updateSet(set: ManualReadoutSet) = Unit

    override suspend fun deleteSet(id: String) = Unit

    override suspend fun saveSets(sets: List<ManualReadoutSet>) = Unit

    override suspend fun resetToDefault() = Unit
}
