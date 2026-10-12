package kurou.kodriver.core.windowshotkeydata.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.core.windowshotkeydata.GlobalHotkey
import kurou.kodriver.core.windowshotkeydata.GlobalHotkeyEvent
import kurou.kodriver.core.windowshotkeydata.GlobalHotkeySource
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.LMU_WINDOWS_READOUT_HOTKEY_MODIFIERS
import kurou.kodriver.domain.repository.HotkeyRepository
import kurou.kodriver.domain.repository.ReadoutHotkeyPreferencesRepository

@OptIn(ExperimentalCoroutinesApi::class)
class HotkeyRepositoryImpl(
    private val source: GlobalHotkeySource,
    private val preferences: ReadoutHotkeyPreferencesRepository,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : HotkeyRepository {
    private val registrationState = MutableStateFlow<HotkeyRegistrationState>(HotkeyRegistrationState.Registering)
    private val retryCount = MutableStateFlow(0)

    // 押下は replay しない。状態の購読も同じ共有登録を維持する。
    private val events =
        combine(preferences.observeVirtualKeyCode(), retryCount) { key, retry -> key to retry }
            .distinctUntilChanged()
            .flatMapLatest { (key, _) ->
                source
                    .observe(GlobalHotkey(LMU_WINDOWS_READOUT_HOTKEY_MODIFIERS, key))
                    .onStart { emit(GlobalHotkeyEvent.Registration(HotkeyRegistrationState.Registering)) }
            }.onEach { event ->
                if (event is GlobalHotkeyEvent.Registration) registrationState.update { event.state }
            }.onCompletion { registrationState.update { HotkeyRegistrationState.Registering } }
            .shareIn(scope, SharingStarted.WhileSubscribed(), replay = 0)

    override suspend fun retryRegistration() {
        val state = registrationState.value
        if (state is HotkeyRegistrationState.Conflict || state is HotkeyRegistrationState.Failed) {
            retryCount.update { it + 1 }
        }
    }

    override fun hotkeyPressStream(): Flow<Unit> = events.filterIsInstance<GlobalHotkeyEvent.Pressed>().map { Unit }

    override fun registrationStateStream(): Flow<HotkeyRegistrationState> =
        flow {
            coroutineScope {
                launch { events.collect { } }
                emitAll(registrationState)
            }
        }
}
