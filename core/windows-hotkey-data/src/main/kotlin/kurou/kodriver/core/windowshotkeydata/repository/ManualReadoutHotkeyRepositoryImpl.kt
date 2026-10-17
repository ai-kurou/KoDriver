package kurou.kodriver.core.windowshotkeydata.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.core.windowshotkeydata.GlobalHotkey
import kurou.kodriver.core.windowshotkeydata.GlobalHotkeyEvent
import kurou.kodriver.core.windowshotkeydata.GlobalHotkeySource
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.repository.ManualReadoutHotkeyRepository
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository

class ManualReadoutHotkeyRepositoryImpl(
    private val source: GlobalHotkeySource,
    setRepositories: Map<String, ManualReadoutSetPreferencesRepository>,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : ManualReadoutHotkeyRepository {
    private data class Registration(
        val key: Int,
        val modifiers: ManualReadoutHotkeyModifiers,
        val retry: Int,
    )

    private val states = MutableStateFlow<Map<ManualReadoutRequest, HotkeyRegistrationState>>(emptyMap())
    private val retries = MutableStateFlow<Map<ManualReadoutRequest, Int>>(emptyMap())
    private val assignments =
        setRepositories
            .map { (simulatorId, repository) ->
                repository.observeSets().map { sets ->
                    sets
                        .mapNotNull { set ->
                            set.hotkeyVirtualKeyCode?.let {
                                ManualReadoutRequest(simulatorId, set.id) to (it to set.hotkeyModifiers)
                            }
                        }.toMap()
                }
            }.let { streams ->
                if (streams.isEmpty()) {
                    flowOf(emptyMap())
                } else {
                    combine(streams) {
                        it
                            .flatMap { keys ->
                                keys.entries
                            }.associate { it.toPair() }
                    }
                }
            }

    // 状態購読と押下購読で全セットの登録を共有し、押下は再配信しない。
    private val presses =
        channelFlow {
            val active = mutableMapOf<ManualReadoutRequest, Pair<Registration, Job>>()
            combine(assignments, retries) { keys, counts ->
                keys.mapValues { (request, key) -> Registration(key.first, key.second, counts[request] ?: 0) }
            }.distinctUntilChanged().collect { desired ->
                val removed = active.keys.filter { active.getValue(it).first != desired[it] }
                // 旧登録の UnregisterHotKey 完了を待ってから新登録を始め、自分の登録との競合を避ける。
                for (request in removed) {
                    active.remove(request)?.second?.cancelAndJoin()
                }
                states.update { current -> current.filterKeys { it in desired } }
                for ((request, registration) in desired) {
                    if (request !in active) {
                        val job =
                            launch {
                                source
                                    .observe(GlobalHotkey(registration.modifiers.value, registration.key))
                                    .onStart {
                                        emit(
                                            GlobalHotkeyEvent.Registration(HotkeyRegistrationState.Registering),
                                        )
                                    }.collect { event ->
                                        when (event) {
                                            is GlobalHotkeyEvent.Registration -> {
                                                states.update { it + (request to event.state) }
                                            }

                                            GlobalHotkeyEvent.Pressed -> {
                                                send(request)
                                            }
                                        }
                                    }
                            }
                        active[request] = registration to job
                    }
                }
            }
        }.onCompletion { states.update { emptyMap() } }
            .shareIn(scope, SharingStarted.WhileSubscribed(), replay = 0)

    override fun pressStream(): Flow<ManualReadoutRequest> = presses

    override fun registrationStateStream(
        simulatorId: String,
        setId: String,
    ): Flow<HotkeyRegistrationState> =
        flow {
            coroutineScope {
                // 登録状態だけを見る画面も WhileSubscribed の購読者とし、OS への登録処理を動かす。
                launch { presses.collect { } }
                emitAll(
                    states
                        .map { it[ManualReadoutRequest(simulatorId, setId)] ?: HotkeyRegistrationState.Unsupported }
                        .distinctUntilChanged(),
                )
            }
        }

    override suspend fun retryRegistration(
        simulatorId: String,
        setId: String,
    ) {
        val request = ManualReadoutRequest(simulatorId, setId)
        val state = states.value[request]
        if (state is HotkeyRegistrationState.Conflict || state is HotkeyRegistrationState.Failed) {
            retries.update { it + (request to ((it[request] ?: 0) + 1)) }
        }
    }
}
