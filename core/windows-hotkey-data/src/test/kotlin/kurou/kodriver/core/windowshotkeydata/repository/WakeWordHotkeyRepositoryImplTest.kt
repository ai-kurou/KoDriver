package kurou.kodriver.core.windowshotkeydata.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.windowshotkeydata.GlobalHotkey
import kurou.kodriver.core.windowshotkeydata.GlobalHotkeyEvent
import kurou.kodriver.core.windowshotkeydata.GlobalHotkeySource
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.speechrecognition.model.WakeWordHotkey
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class WakeWordHotkeyRepositoryImplTest {
    private val preferences = FakePreferencesRepository()
    private val source = RecordingSource()

    @Test
    fun `割り当ての変更で登録し直し解除で登録を外して押下は再配信しない`() =
        runTest {
            val repository = WakeWordHotkeyRepositoryImpl(source, preferences, backgroundScope)
            val states = mutableListOf<HotkeyRegistrationState>()
            val presses = mutableListOf<Unit>()
            val stateCollector =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    repository.registrationStateStream().collect { states.add(it) }
                }
            val pressCollector =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    repository.pressStream().collect { presses.add(it) }
                }
            testScheduler.runCurrent()
            assertEquals(emptyList(), source.requested)
            assertEquals(HotkeyRegistrationState.Unsupported, states.last())

            val first = WakeWordHotkey(0x41)
            preferences.hotkey.value = first
            testScheduler.runCurrent()
            assertEquals(HotkeyRegistrationState.Registered, states.last())
            source.events.emit(GlobalHotkeyEvent.Pressed)
            testScheduler.runCurrent()
            assertEquals(listOf(Unit), presses)
            val late = mutableListOf<Unit>()
            val lateCollector =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    repository.pressStream().collect { late.add(it) }
                }
            testScheduler.runCurrent()
            assertEquals(emptyList(), late)

            val changed = first.copy(modifiers = ManualReadoutHotkeyModifiers.ALT_SHIFT)
            preferences.hotkey.value = changed
            testScheduler.runCurrent()
            assertEquals(
                listOf(
                    GlobalHotkey(first.modifiers.value, 0x41),
                    GlobalHotkey(changed.modifiers.value, 0x41),
                ),
                source.requested,
            )
            assertEquals(listOf(GlobalHotkey(first.modifiers.value, 0x41)), source.released)

            preferences.hotkey.value = changed.copy(virtualKeyCode = null)
            testScheduler.runCurrent()
            assertEquals(source.requested, source.released)
            assertEquals(HotkeyRegistrationState.Unsupported, states.last())
            stateCollector.cancel()
            pressCollector.cancel()
            lateCollector.cancel()
        }

    @Test
    fun `競合と失敗だけ再試行し最後の購読終了で解除する`() =
        runTest {
            preferences.hotkey.value = WakeWordHotkey(0x41)
            for (failure in listOf(HotkeyRegistrationState.Conflict, HotkeyRegistrationState.Failed(5))) {
                val source = RecordingSource(failure)
                val repository = WakeWordHotkeyRepositoryImpl(source, preferences, backgroundScope)
                val states = mutableListOf<HotkeyRegistrationState>()
                val collector =
                    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                        repository.registrationStateStream().collect { states.add(it) }
                    }
                testScheduler.runCurrent()
                assertEquals(failure, states.last())

                source.state = HotkeyRegistrationState.Registered
                repository.retryRegistration()
                testScheduler.runCurrent()
                assertEquals(HotkeyRegistrationState.Registered, states.last())
                assertEquals(2, source.requested.size)
                assertEquals(1, source.released.size)

                repository.retryRegistration()
                testScheduler.runCurrent()
                assertEquals(2, source.requested.size)

                collector.cancel()
                testScheduler.runCurrent()
                assertEquals(2, source.released.size)
                assertEquals(HotkeyRegistrationState.Unsupported, repository.registrationStateStream().first())
                testScheduler.runCurrent()
            }
        }
}

private class FakePreferencesRepository : WakeWordHotkeyPreferencesRepository {
    val hotkey = MutableStateFlow(WakeWordHotkey())

    override fun observeWakeWordHotkey(): Flow<WakeWordHotkey> = hotkey

    override suspend fun saveWakeWordHotkey(hotkey: WakeWordHotkey) {
        this.hotkey.value = hotkey
    }
}

private class RecordingSource(
    var state: HotkeyRegistrationState = HotkeyRegistrationState.Registered,
) : GlobalHotkeySource {
    val requested = mutableListOf<GlobalHotkey>()
    val released = mutableListOf<GlobalHotkey>()
    val events = MutableSharedFlow<GlobalHotkeyEvent>()

    override fun observe(hotkey: GlobalHotkey): Flow<GlobalHotkeyEvent> =
        flow {
            requested.add(hotkey)
            try {
                emit(GlobalHotkeyEvent.Registration(state))
                events.collect { emit(it) }
                awaitCancellation()
            } finally {
                released.add(hotkey)
            }
        }
}
