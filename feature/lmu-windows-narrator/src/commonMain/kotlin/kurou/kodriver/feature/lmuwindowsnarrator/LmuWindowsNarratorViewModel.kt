package kurou.kodriver.feature.lmuwindowsnarrator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.usecase.DetermineLmuWindowsNarratorReadoutUseCase
import kurou.kodriver.domain.usecase.LmuWindowsNarratorReadoutSettings
import kurou.kodriver.domain.usecase.LmuWindowsNarratorState
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartReadoutTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase

data class VehicleApproachUseCases(
    val observeVehicleApproach: ObserveLmuWindowsVehicleApproachUseCase,
    val observeLmuWindows: ObserveLmuWindowsUseCase,
    val observeSkipFirstLap: ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase,
    val observeEnabledStates: ObserveLmuWindowsVehicleApproachEnabledStatesUseCase,
    val observeStartReadoutType: ObserveLmuWindowsVehicleApproachStartReadoutTypeUseCase,
)

data class VehicleDamageUseCases(
    val observeVehicleDamage: ObserveLmuWindowsVehicleDamageUseCase,
    val observeVehicleDamageEnabledStates: ObserveLmuWindowsVehicleDamageEnabledStatesUseCase,
)

data class ReadoutListUseCases(
    val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    val observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    val observeReadoutOrder: ObserveReadoutOrderUseCase,
)

data class FlagUseCases(
    val observeRaceFlags: ObserveLmuWindowsRaceFlagsUseCase,
    val observeFlagEnabledStates: ObserveLmuWindowsFlagEnabledStatesUseCase,
)

data class TyreTemperatureUseCases(
    val observeTyreCarcassTemperature: ObserveLmuWindowsTyreCarcassTemperatureUseCase,
    val observeHighThreshold: ObserveLmuWindowsTyreTemperatureHighThresholdUseCase,
    val observeTyreTemperatureEnabledStates: ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase,
    val observeLowWarningPhases: ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase,
)

data class NarratorUseCases(
    val determineReadout: DetermineLmuWindowsNarratorReadoutUseCase,
    val observeMyBestLapVoiceType: ObserveLmuWindowsMyBestLapVoiceTypeUseCase,
    val saveTelemetryLog: SaveTelemetryLogUseCase,
)

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("LongParameterList")
class LmuWindowsNarratorViewModel(
    vehicleApproachUseCases: VehicleApproachUseCases,
    vehicleDamageUseCases: VehicleDamageUseCases,
    readoutListUseCases: ReadoutListUseCases,
    flagUseCases: FlagUseCases,
    tyreTemperatureUseCases: TyreTemperatureUseCases,
    private val ttsEngine: TextToSpeechEngine,
    private val narratorUseCases: NarratorUseCases,
    private val currentTimeMs: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {

    private var narratorState = LmuWindowsNarratorState()
    private var previousVehicleApproach: LmuWindowsVehicleApproachData? = null
    private var previousLmuWindowsTelemetry: LmuWindowsTelemetryData? = null
    private var previousVehicleDamage: LmuWindowsVehicleDamageData? = null
    private var previousRaceFlags: LmuWindowsRaceFlagsData? = null

    private val selectedSimulator = readoutListUseCases.observeSelectedSimulator()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // listPane（readoutStates）とdetailPane（flagStates・vehicleDamageStates）を統合した、
    // Narratorの読み上げ判定に実際に使う唯一のenabledStates。
    private val mergedEnabledStates = combine(
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator == null) emptyFlow<Map<ReadoutItemKey, Boolean>>()
                else readoutListUseCases.observeReadoutEnabledStates(simulator.id)
            },
        flagUseCases.observeFlagEnabledStates(),
        vehicleDamageUseCases.observeVehicleDamageEnabledStates(),
        tyreTemperatureUseCases.observeTyreTemperatureEnabledStates(),
        vehicleApproachUseCases.observeEnabledStates(),
    ) {
            readoutStates: Map<ReadoutItemKey, Boolean>,
            flagStates,
            vehicleDamageStates,
            tyreTemperatureStates,
            vehicleApproachStates,
        ->
        readoutStates + flagStates + vehicleDamageStates + tyreTemperatureStates + vehicleApproachStates
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap<ReadoutItemKey, Boolean>())

    // index が小さいほど優先度が高い（リスト上位 = 高優先）
    private val readoutOrder = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator == null) emptyFlow() else readoutListUseCases.observeReadoutOrder(simulator.id)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val lmuTelemetryFlow = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator !is Simulator.LmuWindows) emptyFlow()
            else vehicleApproachUseCases.observeLmuWindows()
        }
        .shareIn(viewModelScope, SharingStarted.Eagerly)

    private val raceFlagsFlow = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
            flagUseCases.observeRaceFlags()
        }
        .shareIn(viewModelScope, SharingStarted.Eagerly)

    private val currentLap = lmuTelemetryFlow
        .map { it.timing.currentLap }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val voiceType = narratorUseCases.observeMyBestLapVoiceType()
        .stateIn(viewModelScope, SharingStarted.Eagerly, MyBestLapVoiceType.FORMAL)

    private val tyreHighThreshold = tyreTemperatureUseCases.observeHighThreshold()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 90)

    private val tyreLowWarningPhases = tyreTemperatureUseCases.observeLowWarningPhases()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    private val skipFirstLap = vehicleApproachUseCases.observeSkipFirstLap()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val startReadoutType = vehicleApproachUseCases.observeStartReadoutType()
        .stateIn(viewModelScope, SharingStarted.Eagerly, VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)

    @Suppress("UnusedPrivateProperty")
    private val myBestLapJob = lmuTelemetryFlow
        .onEach { telemetry ->
            val previous = previousLmuWindowsTelemetry
            val observedAtMs = currentTimeMs()
            val decision = narratorUseCases.determineReadout.determineMyBestLap(
                state = narratorState,
                telemetry = telemetry,
                settings = currentSettings,
            )
            narratorState = decision.state
            decision.events.forEach { event ->
                if (speakWithPriority(event)) {
                    saveTelemetryLogSafely(
                        createdAt = observedAtMs,
                        simulatorId = Simulator.LmuWindows.id,
                        readoutItemKey = event.readoutItemKey.value,
                        telemetryJson = buildTelemetryLogJson(previous = previous, current = telemetry),
                    )
                }
            }
            previousLmuWindowsTelemetry = telemetry
        }
        .launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val vehicleApproachJob = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
            vehicleApproachUseCases.observeVehicleApproach()
        }
        .onEach { vehicleApproach ->
            val previous = previousVehicleApproach
            val observedAtMs = currentTimeMs()
            val decision = narratorUseCases.determineReadout.determineVehicleApproach(
                state = narratorState,
                vehicleApproach = vehicleApproach,
                settings = currentSettings,
                observedAtMs = observedAtMs,
            )
            narratorState = decision.state
            decision.events.forEach { event ->
                if (speakWithPriority(event)) {
                    saveTelemetryLogSafely(
                        createdAt = observedAtMs,
                        simulatorId = Simulator.LmuWindows.id,
                        readoutItemKey = event.readoutItemKey.value,
                        telemetryJson = buildTelemetryLogJson(previous = previous, current = vehicleApproach),
                    )
                }
            }
            previousVehicleApproach = vehicleApproach
        }
        .launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val vehicleDamageJob = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
            vehicleDamageUseCases.observeVehicleDamage()
        }
        .onEach { vehicleDamage ->
            val previous = previousVehicleDamage
            val observedAtMs = currentTimeMs()
            val decision = narratorUseCases.determineReadout.determineVehicleDamage(
                state = narratorState,
                vehicleDamage = vehicleDamage,
                settings = currentSettings,
            )
            narratorState = decision.state
            decision.events.forEach { event ->
                if (speakWithPriority(event)) {
                    saveTelemetryLogSafely(
                        createdAt = observedAtMs,
                        simulatorId = Simulator.LmuWindows.id,
                        readoutItemKey = event.readoutItemKey.value,
                        telemetryJson = buildTelemetryLogJson(previous = previous, current = vehicleDamage),
                    )
                }
            }
            previousVehicleDamage = vehicleDamage
        }
        .launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val flagJob = raceFlagsFlow
        .onEach { raceFlags ->
            val previous = previousRaceFlags
            val observedAtMs = currentTimeMs()
            val decision = narratorUseCases.determineReadout.determineRaceFlags(
                state = narratorState,
                raceFlags = raceFlags,
                settings = currentSettings,
            )
            narratorState = decision.state
            decision.events.forEach { event ->
                if (speakWithPriority(event)) {
                    saveTelemetryLogSafely(
                        createdAt = observedAtMs,
                        simulatorId = Simulator.LmuWindows.id,
                        readoutItemKey = event.readoutItemKey.value,
                        telemetryJson = buildTelemetryLogJson(previous = previous, current = raceFlags),
                    )
                }
            }
            previousRaceFlags = raceFlags
        }
        .launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val tyreTemperatureJob = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
            combine(
                tyreTemperatureUseCases.observeTyreCarcassTemperature(),
                raceFlagsFlow,
            ) { tyreCarcassTemperature, raceFlags -> tyreCarcassTemperature to raceFlags }
        }
        .onEach { (tyreCarcassTemperature, raceFlags) ->
            val observedAtMs = currentTimeMs()
            val overheatDecision = narratorUseCases.determineReadout.determineTyreTemperatureOverheat(
                state = narratorState,
                data = tyreCarcassTemperature,
                settings = currentSettings,
            )
            narratorState = overheatDecision.state
            val lowDecision = narratorUseCases.determineReadout.determineTyreTemperatureLow(
                state = narratorState,
                data = tyreCarcassTemperature,
                raceFlags = raceFlags,
                settings = currentSettings,
            )
            narratorState = lowDecision.state
            (overheatDecision.events + lowDecision.events).forEach { event ->
                if (speakWithPriority(event)) {
                    saveTelemetryLogSafely(
                        createdAt = observedAtMs,
                        simulatorId = Simulator.LmuWindows.id,
                        readoutItemKey = event.readoutItemKey.value,
                        telemetryJson = buildTelemetryLogJson(tyreCarcassTemperature),
                    )
                }
            }
        }
        .launchIn(viewModelScope)

    private val currentSettings: LmuWindowsNarratorReadoutSettings
        get() = LmuWindowsNarratorReadoutSettings(
            enabledStates = mergedEnabledStates.value,
            myBestLapVoiceType = voiceType.value,
            currentLap = currentLap.value,
            skipFirstLap = skipFirstLap.value,
            vehicleApproachStartReadoutEnabled =
                mergedEnabledStates.value[ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout] ?: true,
            vehicleApproachStartReadoutType = startReadoutType.value,
            tyreTemperatureHighThresholdCelsius = tyreHighThreshold.value,
            tyreTemperatureLowWarningPhases = tyreLowWarningPhases.value,
        )

    /**
     * 優先度を考慮して読み上げる。
     * - 再生中のアイテムより優先度が高い（order の index が小さい）場合: 現在の再生を停止して割り込む
     * - 再生中のアイテムと同じか優先度が低い場合: 無視する
     */
    private fun speakWithPriority(event: SpeechEvent): Boolean {
        val order = readoutOrder.value
        val currentKey = ttsEngine.currentReadoutItemKey
        if (currentKey != null) {
            val currentIndex = order.indexOf(currentKey).takeIf { it != -1 } ?: Int.MAX_VALUE
            val newIndex = order.indexOf(event.readoutItemKey).takeIf { it != -1 } ?: Int.MAX_VALUE
            if (newIndex >= currentIndex) return false
            ttsEngine.stop()
        }
        ttsEngine.speak(event)
        return true
    }

    private suspend fun saveTelemetryLogSafely(
        createdAt: Long,
        simulatorId: String,
        readoutItemKey: String,
        telemetryJson: String,
    ) {
        try {
            narratorUseCases.saveTelemetryLog(
                createdAt = createdAt,
                simulatorId = simulatorId,
                readoutItemKey = readoutItemKey,
                telemetryJson = telemetryJson,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // ログ保存は読み上げの補助機能のため、保存失敗で以後の読み上げを止めない。
        }
    }
}

private fun buildTelemetryLogJson(
    previous: LmuWindowsVehicleApproachData?,
    current: LmuWindowsVehicleApproachData,
): String =
    """{"previous":${previous?.toJson() ?: "null"},"current":${current.toJson()}}"""

private fun buildTelemetryLogJson(previous: LmuWindowsTelemetryData?, current: LmuWindowsTelemetryData): String =
    """{"previous":${previous?.toJson() ?: "null"},"current":${current.toJson()}}"""

private fun LmuWindowsTelemetryData.toJson(): String =
    "{" +
        """"currentLapTimeMs":${timing.currentLapTimeMs},""" +
        """"lastLapTimeMs":${timing.lastLapTimeMs},""" +
        """"bestLapTimeMs":${timing.bestLapTimeMs},""" +
        """"currentLap":${timing.currentLap},""" +
        """"maxLaps":${timing.maxLaps}""" +
        "}"

private fun LmuWindowsVehicleApproachData.toJson(): String =
    "{" +
        """"sideBySideLeftVehicleIds":${sideBySideLeftVehicleIds.sorted()},""" +
        """"sideBySideRightVehicleIds":${sideBySideRightVehicleIds.sorted()},""" +
        """"lateralDistanceLeftMeters":$lateralDistanceLeftMeters,""" +
        """"lateralDistanceRightMeters":$lateralDistanceRightMeters""" +
        "}"

private fun buildTelemetryLogJson(
    previous: LmuWindowsVehicleDamageData?,
    current: LmuWindowsVehicleDamageData,
): String =
    """{"previous":${previous?.toJson() ?: "null"},"current":${current.toJson()}}"""

private fun LmuWindowsVehicleDamageData.toJson(): String =
    "{" +
        """"overheating":$overheating,""" +
        """"partDetached":$partDetached,""" +
        """"lastImpactMagnitude":$lastImpactMagnitude""" +
        "}"

private fun buildTelemetryLogJson(previous: LmuWindowsRaceFlagsData?, current: LmuWindowsRaceFlagsData): String =
    """{"previous":${previous?.toJson() ?: "null"},"current":${current.toJson()}}"""

private fun LmuWindowsRaceFlagsData.toJson(): String =
    "{" +
        """"gamePhase":"$gamePhase",""" +
        """"yellowFlagState":"$yellowFlagState",""" +
        """"sectorFlags":[${sectorFlags.joinToString(",") { """"$it"""" }}],""" +
        """"startLight":$startLight,""" +
        """"numRedLights":$numRedLights,""" +
        """"playerFlag":"$playerFlag",""" +
        """"playerUnderYellow":$playerUnderYellow,""" +
        """"playerCountLapFlag":"$playerCountLapFlag"""" +
        "}"

private fun buildTelemetryLogJson(data: LmuWindowsTyreCarcassTemperatureData): String =
    """{"wheels":{${data.wheels.entries.joinToString(",") { (k, v) -> """"$k":$v""" }}}}"""
