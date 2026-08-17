package kurou.kodriver.feature.acewindowsnarrator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.AceWindowsCarLocation
import kurou.kodriver.domain.model.AceWindowsStatusType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.AceWindowsNarratorReadoutSettings
import kurou.kodriver.domain.usecase.AceWindowsNarratorState
import kurou.kodriver.domain.usecase.DetermineAceWindowsNarratorReadoutUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsStatusUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal data class RemainingFuelUseCases(
    val observeAceWindowsFuel: ObserveAceWindowsFuelUseCase,
    val observeThresholdPercentage: ObserveAceWindowsRemainingFuelThresholdPercentageUseCase,
)

internal data class FlagUseCases(
    val observeAceWindowsFlag: ObserveAceWindowsFlagUseCase,
    val observeFlagEnabledStates: ObserveAceWindowsFlagEnabledStatesUseCase,
)

internal data class TyreTemperatureUseCases(
    val observeAceWindowsTyreCarcassTemperature: ObserveAceWindowsTyreCarcassTemperatureUseCase,
    val observeHighThreshold: ObserveAceWindowsTyreTemperatureHighThresholdUseCase,
    val observeTyreTemperatureEnabledStates: ObserveAceWindowsTyreTemperatureEnabledStatesUseCase,
)

internal data class ReadoutListUseCases(
    val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    val observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    val observeReadoutOrder: ObserveReadoutOrderUseCase,
    val observeQueueEnabledStates: ObserveQueueEnabledStatesUseCase,
)

@Suppress("LongParameterList")
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
internal class AceWindowsNarratorViewModel(
    remainingFuelUseCases: RemainingFuelUseCases,
    readoutListUseCases: ReadoutListUseCases,
    flagUseCases: FlagUseCases,
    tyreTemperatureUseCases: TyreTemperatureUseCases,
    observeAceWindowsStatus: ObserveAceWindowsStatusUseCase,
    private val eventProcessor: AceWindowsNarratorEventProcessor,
    private val determineAceWindowsNarratorReadout: DetermineAceWindowsNarratorReadoutUseCase =
        DetermineAceWindowsNarratorReadoutUseCase(),
    private val currentTimeMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : ViewModel() {
    private val selectedSimulator =
        readoutListUseCases
            .observeSelectedSimulator()
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val listEnabledStates =
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator == null) emptyFlow() else readoutListUseCases.observeReadoutEnabledStates(simulator.id)
            }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    // listPane（listEnabledStates）とdetailPane（flagEnabledStates・tyreTemperatureEnabledStates）を統合した、
    // Narratorの読み上げ判定に実際に使う唯一のenabledStates。
    private val mergedEnabledStates =
        combine(
            listEnabledStates,
            flagUseCases.observeFlagEnabledStates(),
            tyreTemperatureUseCases.observeTyreTemperatureEnabledStates(),
        ) { readoutStates, flagStates, tyreTemperatureStates ->
            readoutStates + flagStates + tyreTemperatureStates
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap<ReadoutItemKey, Boolean>())

    private val readoutOrder =
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator == null) emptyFlow() else readoutListUseCases.observeReadoutOrder(simulator.id)
            }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // キューに追加して読み上げるかどうか（ReadoutItemKey.TopLevel 単位）。
    private val queueEnabledStates =
        readoutListUseCases
            .observeQueueEnabledStates()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap<ReadoutItemKey, Boolean>())

    private val remainingFuelThreshold =
        remainingFuelUseCases
            .observeThresholdPercentage()
            .stateIn(viewModelScope, SharingStarted.Eagerly, ACE_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT)

    private val tyreTemperatureHighThreshold =
        tyreTemperatureUseCases
            .observeHighThreshold()
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT,
            )

    private var narratorState = AceWindowsNarratorState()

    private val currentSettings: AceWindowsNarratorReadoutSettings
        get() =
            AceWindowsNarratorReadoutSettings(
                enabledStates = mergedEnabledStates.value,
                remainingFuelThresholdPercentage = remainingFuelThreshold.value,
                tyreTemperatureHighThresholdCelsius = tyreTemperatureHighThreshold.value,
            )

    private val fuelFlow =
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator !is Simulator.AceWindows) emptyFlow() else remainingFuelUseCases.observeAceWindowsFuel()
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    private val flagFlow =
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator !is Simulator.AceWindows) emptyFlow() else flagUseCases.observeAceWindowsFlag()
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    private val tyreTemperatureFlow =
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator !is Simulator.AceWindows) {
                    emptyFlow()
                } else {
                    tyreTemperatureUseCases.observeAceWindowsTyreCarcassTemperature()
                }
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    // レース中（LIVE）以外はメニュー・リプレイ・ポーズ中とみなし、読み上げそのものを行わない。
    // ACE以外に切り替わった際はflowOf(null)で明示的にリセットする。emptyFlow()ではstateInが
    // 直前の値を保持し続けてしまい、ACEへ戻した際に新しいstatusStream()の値が届くまで
    // 古いLIVE状態が残ってしまう。
    private val currentStatus =
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator !is Simulator.AceWindows) flowOf(null) else observeAceWindowsStatus()
            }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // レース走行中（LIVE）かつコース上（TRACK）走行中に限定する。
    // ピットレーン・ピット進入・退出中、およびメニュー・リプレイ・ポーズ中は読み上げない。
    private val isOnTrack: Boolean
        get() =
            currentStatus.value?.status == AceWindowsStatusType.LIVE &&
                currentStatus.value?.carLocation == AceWindowsCarLocation.TRACK

    @Suppress("UnusedPrivateProperty")
    private val fuelJob =
        fuelFlow
            .onEach { fuel ->
                val observedAtMs = currentTimeMs()
                val state = narratorState
                val settings = currentSettings
                val decision =
                    determineAceWindowsNarratorReadout.determineRemainingFuel(
                        state = state,
                        data = fuel,
                        settings = settings,
                    )
                narratorState = decision.state
                eventProcessor.processRemainingFuel(
                    fuel = fuel,
                    events = decision.events,
                    readoutOrder = readoutOrder.value,
                    queueEnabledStates = queueEnabledStates.value,
                    observedAtMs = observedAtMs,
                    logContext =
                        AceWindowsTelemetryLogContext(
                            state = state,
                            settings = settings,
                            finalState = decision.state,
                        ),
                    isOnTrack = isOnTrack,
                )
            }.launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val flagJob =
        flagFlow
            .onEach { flag ->
                val observedAtMs = currentTimeMs()
                val state = narratorState
                val settings = currentSettings
                val decision =
                    determineAceWindowsNarratorReadout.determineFlag(
                        state = state,
                        data = flag,
                        settings = settings,
                    )
                narratorState = decision.state
                eventProcessor.processFlag(
                    flag = flag,
                    events = decision.events,
                    readoutOrder = readoutOrder.value,
                    queueEnabledStates = queueEnabledStates.value,
                    observedAtMs = observedAtMs,
                    logContext =
                        AceWindowsTelemetryLogContext(
                            state = state,
                            settings = settings,
                            finalState = decision.state,
                        ),
                    isOnTrack = isOnTrack,
                )
            }.launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val tyreTemperatureJob =
        tyreTemperatureFlow
            .onEach { tyreCarcassTemperature ->
                val observedAtMs = currentTimeMs()
                val state = narratorState
                val settings = currentSettings
                val decision =
                    determineAceWindowsNarratorReadout.determineTyreTemperatureOverheat(
                        state = state,
                        data = tyreCarcassTemperature,
                        settings = settings,
                    )
                narratorState = decision.state
                eventProcessor.processTyreTemperature(
                    tyreCarcassTemperature = tyreCarcassTemperature,
                    events = decision.events,
                    readoutOrder = readoutOrder.value,
                    queueEnabledStates = queueEnabledStates.value,
                    observedAtMs = observedAtMs,
                    logContext =
                        AceWindowsTelemetryLogContext(
                            state = state,
                            settings = settings,
                            finalState = decision.state,
                        ),
                    isOnTrack = isOnTrack,
                )
            }.launchIn(viewModelScope)
}
