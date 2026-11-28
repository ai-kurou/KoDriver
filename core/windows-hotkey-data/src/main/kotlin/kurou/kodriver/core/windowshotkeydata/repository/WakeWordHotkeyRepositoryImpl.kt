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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.core.windowshotkeydata.GlobalHotkey
import kurou.kodriver.core.windowshotkeydata.GlobalHotkeyEvent
import kurou.kodriver.core.windowshotkeydata.GlobalHotkeySource
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyPreferencesRepository
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyRepository

@OptIn(ExperimentalCoroutinesApi::class)
internal class WakeWordHotkeyRepositoryImpl(
    private val source: GlobalHotkeySource,
    preferencesRepository: WakeWordHotkeyPreferencesRepository,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : WakeWordHotkeyRepository {
    private val state = MutableStateFlow<HotkeyRegistrationState>(HotkeyRegistrationState.Unsupported)
    private val retries = MutableStateFlow(0)

    // 状態購読と押下購読で登録を共有し、押下は再配信しない。
    // flatMapLatest は旧登録の解除完了を待ってから新登録を始めるため、自分の登録とは競合しない。
    private val presses: Flow<Unit> =
        combine(preferencesRepository.observeWakeWordHotkey(), retries, ::Pair)
            .distinctUntilChanged()
            .flatMapLatest { (hotkey, _) ->
                val key = hotkey.virtualKeyCode
                if (key == null) {
                    flowOf(GlobalHotkeyEvent.Registration(HotkeyRegistrationState.Unsupported))
                } else {
                    source
                        .observe(GlobalHotkey(hotkey.modifiers.value, key))
                        .onStart { emit(GlobalHotkeyEvent.Registration(HotkeyRegistrationState.Registering)) }
                }
            }.transform { event ->
                when (event) {
                    is GlobalHotkeyEvent.Registration -> state.value = event.state
                    GlobalHotkeyEvent.Pressed -> emit(Unit)
                }
            }.onCompletion { state.value = HotkeyRegistrationState.Unsupported }
            .shareIn(scope, SharingStarted.WhileSubscribed(), replay = 0)

    override fun pressStream(): Flow<Unit> = presses

    override fun registrationStateStream(): Flow<HotkeyRegistrationState> =
        flow {
            coroutineScope {
                // 登録状態だけを見る画面も WhileSubscribed の購読者とし、OS への登録処理を動かす。
                launch { presses.collect { } }
                emitAll(state)
            }
        }

    override suspend fun retryRegistration() {
        val current = state.value
        if (current is HotkeyRegistrationState.Conflict || current is HotkeyRegistrationState.Failed) {
            retries.update { it + 1 }
        }
    }
}
