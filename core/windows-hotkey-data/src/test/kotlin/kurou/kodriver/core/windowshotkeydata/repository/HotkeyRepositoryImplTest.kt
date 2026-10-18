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
import kurou.kodriver.domain.repository.ReadoutHotkeyPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HotkeyRepositoryImplTest {
    @Test
    fun `状態と押下を同時購読しても登録は共有されキー変更で解除後に再登録する`() =
        runTest {
            val source = RecordingSource()
            val preferences = RecordingPreferences()
            val repository = HotkeyRepositoryImpl(source, preferences, backgroundScope)
            val states = mutableListOf<HotkeyRegistrationState>()
            val presses = mutableListOf<Unit>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                repository.registrationStateStream().collect { states.add(it) }
            }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                repository.hotkeyPressStream().collect { presses.add(it) }
            }
            testScheduler.runCurrent()
            assertEquals(listOf(GlobalHotkey(7, 0x70)), source.requested)
            assertEquals(HotkeyRegistrationState.Registered, states.last())
            source.presses.emit(GlobalHotkeyEvent.Pressed)
            testScheduler.runCurrent()
            assertEquals(listOf(Unit), presses)
            val latePresses = mutableListOf<Unit>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                repository.hotkeyPressStream().collect { latePresses.add(it) }
            }
            testScheduler.runCurrent()
            assertEquals(emptyList(), latePresses)
            assertEquals(HotkeyRegistrationState.Registered, repository.registrationStateStream().first())
            preferences.saveVirtualKeyCode(0x41)
            testScheduler.runCurrent()
            assertEquals(listOf(GlobalHotkey(7, 0x70), GlobalHotkey(7, 0x41)), source.requested)
            assertEquals(listOf(GlobalHotkey(7, 0x70)), source.released)
            assertEquals(1, source.activeRegistrations)
            source.presses.emit(GlobalHotkeyEvent.Pressed)
            testScheduler.runCurrent()
            assertEquals(listOf(Unit, Unit), presses)
            assertEquals(listOf(Unit), latePresses)
        }

    @Test
    fun `競合または失敗後は同じキーでも再試行で再登録され登録済みでは再登録しない`() =
        runTest {
            val retryableStates =
                listOf(
                    HotkeyRegistrationState.Conflict,
                    HotkeyRegistrationState.Failed(5),
                )
            for (state in retryableStates) {
                val source = RecordingSource(state)
                val repository = HotkeyRepositoryImpl(source, RecordingPreferences(), backgroundScope)
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    repository.registrationStateStream().collect { }
                }
                testScheduler.runCurrent()
                repository.retryRegistration()
                testScheduler.runCurrent()
                assertEquals(listOf(GlobalHotkey(7, 0x70), GlobalHotkey(7, 0x70)), source.requested)
            }

            val registered = RecordingSource()
            val other = HotkeyRepositoryImpl(registered, RecordingPreferences(), backgroundScope)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                other.registrationStateStream().collect { }
            }
            testScheduler.runCurrent()
            other.retryRegistration()
            testScheduler.runCurrent()
            assertEquals(listOf(GlobalHotkey(7, 0x70)), registered.requested)
        }

    @Test
    fun `最後の購読者が抜けると状態を登録中へ戻す`() =
        runTest {
            val repository = HotkeyRepositoryImpl(RecordingSource(), RecordingPreferences(), backgroundScope)
            val collector =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    repository.registrationStateStream().collect { }
                }
            testScheduler.runCurrent()
            assertEquals(HotkeyRegistrationState.Registered, repository.registrationStateStream().first())
            collector.cancel()
            testScheduler.runCurrent()
            val states = mutableListOf<HotkeyRegistrationState>()
            val second =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    repository.registrationStateStream().collect { states.add(it) }
                }
            assertEquals(HotkeyRegistrationState.Registering, states.first())
            second.cancel()
        }

    @Test
    fun `競合と失敗と非対応を状態に反映し購読終了時に解除する`() =
        runTest {
            val results =
                listOf(
                    HotkeyRegistrationState.Conflict,
                    HotkeyRegistrationState.Failed(5),
                    HotkeyRegistrationState.Unsupported,
                )
            for (state in results) {
                val source = RecordingSource(state)
                val repository = HotkeyRepositoryImpl(source, RecordingPreferences(), backgroundScope)
                val collector =
                    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                        repository.registrationStateStream().collect { }
                    }
                testScheduler.runCurrent()
                assertEquals(state, repository.registrationStateStream().first())
                collector.cancel()
                testScheduler.runCurrent()
                assertEquals(0, source.activeRegistrations)
            }
        }
}

private class RecordingPreferences : ReadoutHotkeyPreferencesRepository {
    private val key = MutableStateFlow(0x70)

    override fun observeVirtualKeyCode(): Flow<Int> = key

    override suspend fun saveVirtualKeyCode(virtualKeyCode: Int) {
        key.update { virtualKeyCode }
    }
}

private class RecordingSource(
    private val state: HotkeyRegistrationState = HotkeyRegistrationState.Registered,
) : GlobalHotkeySource {
    val presses = MutableSharedFlow<GlobalHotkeyEvent>()
    val requested = mutableListOf<GlobalHotkey>()
    val released = mutableListOf<GlobalHotkey>()
    var activeRegistrations = 0

    override fun observe(hotkey: GlobalHotkey): Flow<GlobalHotkeyEvent> =
        flow {
            check(activeRegistrations == 0)
            activeRegistrations++
            requested.add(hotkey)
            try {
                emit(GlobalHotkeyEvent.Registration(state))
                presses.collect { emit(it) }
            } finally {
                activeRegistrations--
                released.add(hotkey)
            }
        }
}
