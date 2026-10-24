@file:Suppress("TooManyFunctions", "LargeClass")

package kurou.kodriver.feature.lmuwindowsnarrator

import androidx.lifecycle.viewModelScope
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kurou.kodriver.domain.engine.DebugReadoutText
import kurou.kodriver.domain.engine.LmuWindowsBlueFlag
import kurou.kodriver.domain.engine.LmuWindowsBrakeOverheat
import kurou.kodriver.domain.engine.LmuWindowsBrakeWearLow
import kurou.kodriver.domain.engine.LmuWindowsCarLeft
import kurou.kodriver.domain.engine.LmuWindowsCarLeftSustained
import kurou.kodriver.domain.engine.LmuWindowsManualReadoutText
import kurou.kodriver.domain.engine.LmuWindowsMyBestLap
import kurou.kodriver.domain.engine.LmuWindowsOverheating
import kurou.kodriver.domain.engine.LmuWindowsPartDetached
import kurou.kodriver.domain.engine.LmuWindowsPitTimingWarning
import kurou.kodriver.domain.engine.LmuWindowsRedFlag
import kurou.kodriver.domain.engine.LmuWindowsRemainingFuelWarning
import kurou.kodriver.domain.engine.LmuWindowsRemainingVirtualEnergyCurrentValue
import kurou.kodriver.domain.engine.LmuWindowsRemainingVirtualEnergyWarning
import kurou.kodriver.domain.engine.LmuWindowsTyreCold
import kurou.kodriver.domain.engine.LmuWindowsTyreDetached
import kurou.kodriver.domain.engine.LmuWindowsTyreOverheat
import kurou.kodriver.domain.engine.LmuWindowsTyreWearWarning
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeWearData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsEngineData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsFuelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsFuelUnit
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsInputsData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTimingData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearRatio
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyRatio
import kurou.kodriver.domain.lmuwindows.model.PrimaryFlag
import kurou.kodriver.domain.lmuwindows.model.SectorFlagState
import kurou.kodriver.domain.lmuwindows.model.SessionPhase
import kurou.kodriver.domain.lmuwindows.model.SessionYellowFlagState
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsMyBestLapReadoutText
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsAllVehicleClasses
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsBrakeTemperatureRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsBrakeWearRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreDetachedRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreWearPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreWearRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVirtualEnergyRepository
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsBrakeReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsFuelReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsManualReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsMyBestLapReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsPitTimingReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsRaceFlagReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsTyreReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsVehicleReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsVirtualEnergyReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.LmuWindowsPitTimingTrackingState
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeTemperatureUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeWearRemainingUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsFuelUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreDetachedUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreWearThresholdPercentageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreWearUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachSustainedDurationUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import kurou.kodriver.domain.model.BrakeThicknessMeters
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.DebugReadoutRequest
import kurou.kodriver.domain.model.LateralDistanceMeters
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.PitTimingSource
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import kurou.kodriver.domain.repository.AutoReadoutPreferencesRepository
import kurou.kodriver.domain.repository.DebugReadoutRequestRepository
import kurou.kodriver.domain.repository.HotkeyRepository
import kurou.kodriver.domain.repository.ManualReadoutHotkeyRepository
import kurou.kodriver.domain.repository.ManualReadoutRequestRepository
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.repository.QueuePreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.ObserveAutoReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAutoReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveDebugReadoutRequestUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutHotkeyPressesUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutRequestsUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutHotkeyUseCase
import kurou.kodriver.domain.usecase.ObserveResolvedAutoReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ResolveAutoReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private const val TELEMETRY_BUFFER_CAPACITY = 64

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsNarratorViewModelTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerLmuWindowsReadoutItemKeys()
    }

    private val debugRequests = MutableSharedFlow<DebugReadoutRequest>()
    private val debugRequestRepository: DebugReadoutRequestRepository = mockk()

    private val testDispatcher = UnconfinedTestDispatcher()

    // 本番の telemetryStream は共有メモリ由来で複数購読できるため、テストでもチャンネルの値を全購読者へ配る。
    private val telemetryForwarderScope = CoroutineScope(SupervisorJob() + testDispatcher)

    /** 判定時に解決済みの文言を持つ状態で TTS エンジンへ渡される残量警告。 */
    private val resolvedFuelWarning =
        LmuWindowsRemainingFuelWarning(
            percentage = 20,
            resolvedText = LmuWindowsRemainingFuelWarning(20).narratedText,
        )

    private val resolvedWarning =
        LmuWindowsRemainingVirtualEnergyWarning(
            percentage = 50,
            resolvedText = LmuWindowsRemainingVirtualEnergyWarning(50).narratedText,
        )

    private val vehicleApproachRepository: LmuWindowsVehicleApproachRepository = mockk(relaxUnitFun = true)

    private val lmuWindowsRepository: LmuWindowsRepository = mockk(relaxUnitFun = true)

    private val vehicleApproachPreferencesRepository: LmuWindowsVehicleApproachPreferencesRepository =
        mockk(relaxUnitFun = true)

    private val vehicleApproachThresholdsPreferencesRepository:
        LmuWindowsVehicleApproachThresholdsPreferencesRepository = mockk(relaxUnitFun = true)

    private val vehicleDamageRepository: LmuWindowsVehicleDamageRepository = mockk(relaxUnitFun = true)

    private val vehicleDamagePreferencesRepository: LmuWindowsVehicleDamagePreferencesRepository =
        mockk(relaxUnitFun = true)

    private val tyreDetachedRepository: LmuWindowsTyreDetachedRepository = mockk(relaxUnitFun = true)

    private val simulatorPreferencesRepository: SimulatorPreferencesRepository = mockk(relaxUnitFun = true)

    private val autoReadoutPreferencesRepository: AutoReadoutPreferencesRepository = mockk(relaxUnitFun = true)

    private val flagRepository: LmuWindowsFlagRepository = mockk(relaxUnitFun = true)

    private val flagPreferencesRepository: LmuWindowsFlagPreferencesRepository = mockk(relaxUnitFun = true)

    private val tyreCarcassTemperatureRepository: LmuWindowsTyreCarcassTemperatureRepository =
        mockk(relaxUnitFun = true)

    private val tyreTemperaturePreferencesRepository: LmuWindowsTyreTemperaturePreferencesRepository =
        mockk(relaxUnitFun = true)

    private val vehicleClassTyreTemperaturePreferencesRepository:
        LmuWindowsVehicleClassTyreTemperaturePreferencesRepository = mockk(relaxUnitFun = true)

    private val vehicleClassRepository: LmuWindowsVehicleClassRepository = mockk(relaxUnitFun = true)

    private val tyreWearRepository: LmuWindowsTyreWearRepository = mockk(relaxUnitFun = true)

    private val tyreWearPreferencesRepository: LmuWindowsTyreWearPreferencesRepository = mockk(relaxUnitFun = true)

    private val brakeTemperatureRepository: LmuWindowsBrakeTemperatureRepository = mockk(relaxUnitFun = true)

    private val vehicleClassBrakeTemperaturePreferencesRepository:
        LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository = mockk(relaxUnitFun = true)

    private val brakeWearRepository: LmuWindowsBrakeWearRepository = mockk(relaxUnitFun = true)

    private val vehicleClassBrakeWearPreferencesRepository:
        LmuWindowsVehicleClassBrakeWearPreferencesRepository = mockk(relaxUnitFun = true)

    private val manualHotkeyRepository: ManualReadoutHotkeyRepository = mockk()
    private val manualRequestRepository: ManualReadoutRequestRepository = mockk()
    private val manualSetRepository: ManualReadoutSetPreferencesRepository = mockk()

    private val hotkeyRepository: HotkeyRepository = mockk()

    private val virtualEnergyRepository: LmuWindowsVirtualEnergyRepository = mockk(relaxUnitFun = true)

    private val remainingVirtualEnergyPreferencesRepository: LmuWindowsRemainingVirtualEnergyPreferencesRepository =
        mockk(relaxUnitFun = true)

    private val remainingFuelPreferencesRepository: LmuWindowsRemainingFuelPreferencesRepository =
        mockk(relaxUnitFun = true)

    private val pitTimingPreferencesRepository: LmuWindowsPitTimingPreferencesRepository = mockk(relaxUnitFun = true)

    private val telemetryLogRepository: TelemetryLogRepository = mockk(relaxUnitFun = true)

    private val queuePreferencesRepository: QueuePreferencesRepository = mockk(relaxUnitFun = true)

    private val ttsEngine: TextToSpeechEngine = mockk(relaxUnitFun = true)

    private val priorityAwareTts: PriorityAwareTts = mockk(relaxUnitFun = true)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        telemetryForwarderScope.cancel()
        Dispatchers.resetMain()
    }

    @Suppress("LongParameterList")
    private fun stubRepositories(
        vehicleApproachChannel: Channel<LmuWindowsVehicleApproachData>,
        flagChannel: Channel<LmuWindowsRaceFlagsData>,
        damageChannel: Channel<LmuWindowsVehicleDamageData>,
        tyreDetachedChannel: Channel<LmuWindowsTyreDetachedData>,
        telemetryChannel: Channel<LmuWindowsTelemetryData>,
        tyreTemperatureChannel: Channel<LmuWindowsTyreCarcassTemperatureData>,
        tyreWearChannel: Channel<LmuWindowsTyreWearData>,
        brakeTemperatureChannel: Channel<LmuWindowsBrakeTemperatureData>,
        brakeWearChannel: Channel<LmuWindowsBrakeWearData?>,
        remainingVirtualEnergyChannel: Channel<LmuWindowsVirtualEnergyData>,
        enabledOverrides: Map<ReadoutItemKey, Boolean>,
        flagEnabledOverrides: Map<ReadoutItemKey, Boolean>,
        vehicleDamageEnabledOverrides: Map<ReadoutItemKey, Boolean>,
        orderOverride: List<ReadoutItemKey>,
        skipFirstLap: Boolean,
        startReadoutEnabled: Boolean,
        sustainedReadoutEnabled: Boolean,
        sustainedApproachDurationSeconds: Int,
        tyreTemperatureHighThreshold: Int,
        vehicleClass: LmuWindowsVehicleClassData,
        tyreTemperatureHighThresholdByVehicleClass: Map<LmuWindowsVehicleClassData, Int>?,
        tyreTemperatureOverheatWarningEnabled: Boolean,
        tyreTemperatureLowWarningEnabled: Boolean,
        tyreTemperatureLowWarningPhasesOverride: Map<SessionPhase, Boolean>,
        tyreWearThresholdPercentage: Int,
        remainingVirtualEnergyThresholdPercentage: Int,
        remainingFuelThresholdPercentage: Int,
        pitTimingVirtualEnergyLapsThreshold: Int,
        pitTimingTyreWearLapsThreshold: Int,
        simulator: Simulator,
        queueEnabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
        pitTimingEnabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
    ) {
        every { vehicleApproachRepository.vehicleApproachStream() } returns vehicleApproachChannel.receiveAsFlow()
        val telemetryShared =
            MutableSharedFlow<LmuWindowsTelemetryData>(extraBufferCapacity = TELEMETRY_BUFFER_CAPACITY)
        telemetryForwarderScope.launch { telemetryChannel.consumeEach { telemetryShared.emit(it) } }
        every { lmuWindowsRepository.telemetryStream() } returns telemetryShared
        every { vehicleApproachPreferencesRepository.observeSkipFirstLap() } returns MutableStateFlow(skipFirstLap)
        every { vehicleApproachPreferencesRepository.observeEnabledStates() } returns
            MutableStateFlow(
                mapOf(
                    LmuWindowsReadoutItemKey.VehicleApproach.StartReadout to startReadoutEnabled,
                    LmuWindowsReadoutItemKey.VehicleApproach.Sustained to sustainedReadoutEnabled,
                ),
            )
        every { vehicleApproachThresholdsPreferencesRepository.observeSustainedApproachDurationSeconds() } returns
            MutableStateFlow(sustainedApproachDurationSeconds)
        every { vehicleDamageRepository.vehicleDamageStream() } returns damageChannel.receiveAsFlow()
        every { vehicleDamagePreferencesRepository.observeEnabledStates() } returns
            MutableStateFlow(vehicleDamageEnabledOverrides)
        every { tyreDetachedRepository.tyreDetachedStream() } returns tyreDetachedChannel.receiveAsFlow()
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(simulator)
        every { autoReadoutPreferencesRepository.observeAutoReadoutEnabledStates(Simulator.LmuWindows.id) } returns
            MutableStateFlow(enabledOverrides)
        every { autoReadoutPreferencesRepository.observeAutoReadoutOrder(Simulator.LmuWindows.id) } returns
            MutableStateFlow(orderOverride)
        // シミュレーター切り替えのテスト用に、LMU 以外の購読も解決できるようにする
        every { autoReadoutPreferencesRepository.observeAutoReadoutEnabledStates(Simulator.Gt7Ps5.id) } returns
            MutableStateFlow(emptyMap())
        every { autoReadoutPreferencesRepository.observeAutoReadoutOrder(Simulator.Gt7Ps5.id) } returns
            MutableStateFlow(orderOverride)
        every { flagRepository.flagStream() } returns flagChannel.receiveAsFlow()
        every { flagPreferencesRepository.observeFlagEnabledStates() } returns MutableStateFlow(flagEnabledOverrides)
        every { tyreCarcassTemperatureRepository.tyreCarcassTemperatureStream() } returns
            tyreTemperatureChannel.receiveAsFlow()
        every { vehicleClassTyreTemperaturePreferencesRepository.observeHighThresholdCelsius() } returns
            MutableStateFlow(
                tyreTemperatureHighThresholdByVehicleClass?.mapValues { Celsius(it.value) }
                    ?: lmuWindowsAllVehicleClasses.associateWith { Celsius(tyreTemperatureHighThreshold) },
            )
        every { vehicleClassRepository.vehicleClassStream() } returns MutableStateFlow(vehicleClass)
        every { tyreTemperaturePreferencesRepository.observeEnabledStates() } returns
            MutableStateFlow(
                mapOf(
                    LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning to tyreTemperatureOverheatWarningEnabled,
                    LmuWindowsReadoutItemKey.TyreTemperature.LowWarning to tyreTemperatureLowWarningEnabled,
                ),
            )
        every { tyreTemperaturePreferencesRepository.observeLowWarningPhases() } returns
            MutableStateFlow(tyreTemperatureLowWarningPhasesOverride)
        every { tyreWearRepository.tyreWearStream() } returns tyreWearChannel.receiveAsFlow()
        every { tyreWearPreferencesRepository.observeThresholdPercentage() } returns
            MutableStateFlow(tyreWearThresholdPercentage)
        every { brakeTemperatureRepository.brakeTemperatureStream() } returns brakeTemperatureChannel.receiveAsFlow()
        every { vehicleClassBrakeTemperaturePreferencesRepository.observeHighThresholdCelsius() } returns
            MutableStateFlow(
                lmuWindowsAllVehicleClasses.associateWith {
                    lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(it)
                },
            )
        every { brakeWearRepository.brakeWearStream() } returns brakeWearChannel.receiveAsFlow()
        every { vehicleClassBrakeWearPreferencesRepository.observeLowThresholdPercent() } returns
            MutableStateFlow(
                lmuWindowsAllVehicleClasses.associateWith {
                    lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault(it)
                },
            )
        every { virtualEnergyRepository.virtualEnergyStream() } returns
            remainingVirtualEnergyChannel.receiveAsFlow()
        every { remainingVirtualEnergyPreferencesRepository.observeThresholdPercentage() } returns
            MutableStateFlow(remainingVirtualEnergyThresholdPercentage)
        every { remainingFuelPreferencesRepository.observeThresholdPercentage() } returns
            MutableStateFlow(remainingFuelThresholdPercentage)
        every { pitTimingPreferencesRepository.observeVirtualEnergyLaps() } returns
            MutableStateFlow(pitTimingVirtualEnergyLapsThreshold)
        every { pitTimingPreferencesRepository.observeTyreWearLaps() } returns
            MutableStateFlow(pitTimingTyreWearLapsThreshold)
        every { pitTimingPreferencesRepository.observeEnabledStates() } returns
            MutableStateFlow(pitTimingEnabledOverrides)
        every { queuePreferencesRepository.observeQueueEnabledStates() } returns
            MutableStateFlow(queueEnabledOverrides)
    }

    @Suppress("LongParameterList", "LongMethod")
    private fun createViewModel(
        vehicleApproachChannel: Channel<LmuWindowsVehicleApproachData> = Channel(Channel.UNLIMITED),
        flagChannel: Channel<LmuWindowsRaceFlagsData> = Channel(Channel.UNLIMITED),
        damageChannel: Channel<LmuWindowsVehicleDamageData> = Channel(Channel.UNLIMITED),
        tyreDetachedChannel: Channel<LmuWindowsTyreDetachedData> = Channel(Channel.UNLIMITED),
        telemetryChannel: Channel<LmuWindowsTelemetryData> = Channel(Channel.UNLIMITED),
        tyreTemperatureChannel: Channel<LmuWindowsTyreCarcassTemperatureData> = Channel(Channel.UNLIMITED),
        tyreWearChannel: Channel<LmuWindowsTyreWearData> = Channel(Channel.UNLIMITED),
        brakeTemperatureChannel: Channel<LmuWindowsBrakeTemperatureData> = Channel(Channel.UNLIMITED),
        brakeWearChannel: Channel<LmuWindowsBrakeWearData?> = Channel(Channel.UNLIMITED),
        remainingVirtualEnergyChannel: Channel<LmuWindowsVirtualEnergyData> = Channel(Channel.UNLIMITED),
        hotkeyChannel: Channel<Unit> = Channel(Channel.UNLIMITED),
        manualRequestChannel: Channel<ManualReadoutRequest> = Channel(Channel.UNLIMITED),
        manualHotkeyChannel: Channel<ManualReadoutRequest> = Channel(Channel.UNLIMITED),
        manualSets: List<ManualReadoutSet> =
            listOf(ManualReadoutSet("set-1", listOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value))),
        ttsEngine: TextToSpeechEngine,
        enabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
        flagEnabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
        vehicleDamageEnabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
        orderOverride: List<ReadoutItemKey> =
            listOf(
                LmuWindowsReadoutItemKey.Flag.Root,
                LmuWindowsReadoutItemKey.VehicleApproach.Root,
            ),
        skipFirstLap: Boolean = false,
        startReadoutEnabled: Boolean = true,
        sustainedReadoutEnabled: Boolean = true,
        sustainedApproachDurationSeconds: Int = LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT,
        tyreOverheatReadoutText: String? = "タイヤを冷やして",
        tyreColdReadoutText: String? = "タイヤを温めて",
        myBestLapReadoutText: String? = LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT,
        overheatReadoutText: String? = "オーバーヒート",
        partDetachedReadoutText: String? = "部品脱落",
        tyreDetachedReadoutText: String? = "タイヤ脱落",
        tyreTemperatureHighThreshold: Int = 90,
        vehicleClass: LmuWindowsVehicleClassData = LmuWindowsVehicleClassData.Hypercar,
        tyreTemperatureHighThresholdByVehicleClass: Map<LmuWindowsVehicleClassData, Int>? = null,
        tyreTemperatureOverheatWarningEnabled: Boolean = true,
        tyreTemperatureLowWarningEnabled: Boolean = true,
        tyreTemperatureLowWarningPhasesOverride: Map<SessionPhase, Boolean> = emptyMap(),
        tyreWearThresholdPercentage: Int = LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT,
        remainingVirtualEnergyThresholdPercentage: Int = 30,
        remainingFuelThresholdPercentage: Int = 20,
        pitTimingVirtualEnergyLapsThreshold: Int = 3,
        pitTimingTyreWearLapsThreshold: Int = 3,
        simulator: Simulator = Simulator.LmuWindows,
        selectedSimulatorFlow: MutableStateFlow<Simulator>? = null,
        currentTimeMs: () -> Long = { 0L },
        queueEnabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
        pitTimingEnabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
        readoutTextOverride: (suspend (SpeechEvent) -> String?)? = null,
    ): LmuWindowsNarratorViewModel {
        stubRepositories(
            vehicleApproachChannel = vehicleApproachChannel,
            flagChannel = flagChannel,
            damageChannel = damageChannel,
            tyreDetachedChannel = tyreDetachedChannel,
            telemetryChannel = telemetryChannel,
            tyreTemperatureChannel = tyreTemperatureChannel,
            tyreWearChannel = tyreWearChannel,
            brakeTemperatureChannel = brakeTemperatureChannel,
            brakeWearChannel = brakeWearChannel,
            remainingVirtualEnergyChannel = remainingVirtualEnergyChannel,
            enabledOverrides = enabledOverrides,
            flagEnabledOverrides = flagEnabledOverrides,
            vehicleDamageEnabledOverrides = vehicleDamageEnabledOverrides,
            orderOverride = orderOverride,
            skipFirstLap = skipFirstLap,
            startReadoutEnabled = startReadoutEnabled,
            sustainedReadoutEnabled = sustainedReadoutEnabled,
            sustainedApproachDurationSeconds = sustainedApproachDurationSeconds,
            tyreTemperatureHighThreshold = tyreTemperatureHighThreshold,
            vehicleClass = vehicleClass,
            tyreTemperatureHighThresholdByVehicleClass = tyreTemperatureHighThresholdByVehicleClass,
            tyreTemperatureOverheatWarningEnabled = tyreTemperatureOverheatWarningEnabled,
            tyreTemperatureLowWarningEnabled = tyreTemperatureLowWarningEnabled,
            tyreTemperatureLowWarningPhasesOverride = tyreTemperatureLowWarningPhasesOverride,
            tyreWearThresholdPercentage = tyreWearThresholdPercentage,
            remainingVirtualEnergyThresholdPercentage = remainingVirtualEnergyThresholdPercentage,
            remainingFuelThresholdPercentage = remainingFuelThresholdPercentage,
            pitTimingVirtualEnergyLapsThreshold = pitTimingVirtualEnergyLapsThreshold,
            pitTimingTyreWearLapsThreshold = pitTimingTyreWearLapsThreshold,
            simulator = simulator,
            queueEnabledOverrides = queueEnabledOverrides,
            pitTimingEnabledOverrides = pitTimingEnabledOverrides,
        )

        selectedSimulatorFlow?.let {
            every { simulatorPreferencesRepository.selectedSimulator() } returns it
        }
        every { hotkeyRepository.hotkeyPressStream() } returns hotkeyChannel.receiveAsFlow()
        every { manualHotkeyRepository.pressStream() } returns manualHotkeyChannel.receiveAsFlow()
        every { manualRequestRepository.observeRequests() } returns manualRequestChannel.receiveAsFlow()
        every { manualSetRepository.observeSets() } returns flowOf(manualSets)

        every { debugRequestRepository.observeRequests() } returns debugRequests
        return LmuWindowsNarratorViewModel(
            observeDebugReadoutRequest = ObserveDebugReadoutRequestUseCase(debugRequestRepository),
            vehicleApproachUseCases =
                VehicleApproachUseCases(
                    observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(vehicleApproachRepository),
                    observeLmuWindows = ObserveLmuWindowsUseCase(lmuWindowsRepository),
                    observeSkipFirstLap =
                        ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase(
                            vehicleApproachPreferencesRepository,
                        ),
                    observeEnabledStates =
                        ObserveLmuWindowsVehicleApproachEnabledStatesUseCase(
                            vehicleApproachPreferencesRepository,
                        ),
                    observeSustainedApproachDuration =
                        ObserveLmuWindowsVehicleApproachSustainedDurationUseCase(
                            vehicleApproachThresholdsPreferencesRepository,
                        ),
                ),
            vehicleDamageUseCases =
                VehicleDamageUseCases(
                    observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(vehicleDamageRepository),
                    observeVehicleDamageEnabledStates =
                        ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(
                            vehicleDamagePreferencesRepository,
                        ),
                ),
            tyreDetachedUseCases =
                TyreDetachedUseCases(ObserveLmuWindowsTyreDetachedUseCase(tyreDetachedRepository)),
            simulatorUseCases =
                SimulatorUseCases(ObserveSelectedSimulatorUseCase(simulatorPreferencesRepository)),
            readoutListUseCases =
                ReadoutListUseCases(
                    observeAutoReadoutEnabledStates =
                        ObserveAutoReadoutEnabledStatesUseCase(autoReadoutPreferencesRepository),
                    observeAutoReadoutOrder =
                        ObserveResolvedAutoReadoutOrderUseCase(
                            ObserveAutoReadoutOrderUseCase(autoReadoutPreferencesRepository),
                            ResolveAutoReadoutOrderUseCase(),
                        ),
                    observeQueueEnabledStates = ObserveQueueEnabledStatesUseCase(queuePreferencesRepository),
                ),
            flagUseCases =
                FlagUseCases(
                    observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(flagRepository),
                    observeFlagEnabledStates = ObserveLmuWindowsFlagEnabledStatesUseCase(flagPreferencesRepository),
                ),
            tyreTemperatureUseCases =
                TyreTemperatureUseCases(
                    observeTyreCarcassTemperature =
                        ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                            tyreCarcassTemperatureRepository,
                        ),
                    observeVehicleClassHighThreshold =
                        ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase(
                            vehicleClassTyreTemperaturePreferencesRepository,
                        ),
                    observeVehicleClass = ObserveLmuWindowsVehicleClassUseCase(vehicleClassRepository),
                    observeTyreTemperatureEnabledStates =
                        ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase(
                            tyreTemperaturePreferencesRepository,
                        ),
                    observeLowWarningPhases =
                        ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(
                            tyreTemperaturePreferencesRepository,
                        ),
                ),
            tyreWearUseCases =
                TyreWearUseCases(
                    observeTyreWear = ObserveLmuWindowsTyreWearUseCase(tyreWearRepository),
                    observeThresholdPercentage =
                        ObserveLmuWindowsTyreWearThresholdPercentageUseCase(
                            tyreWearPreferencesRepository,
                        ),
                ),
            brakeTemperatureUseCases =
                BrakeTemperatureUseCases(
                    observeBrakeTemperature = ObserveLmuWindowsBrakeTemperatureUseCase(brakeTemperatureRepository),
                    observeVehicleClassHighThreshold =
                        ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase(
                            vehicleClassBrakeTemperaturePreferencesRepository,
                        ),
                ),
            brakeWearUseCases =
                BrakeWearUseCases(
                    observeBrakeWear =
                        ObserveLmuWindowsBrakeWearRemainingUseCase(brakeWearRepository, vehicleClassRepository),
                    observeVehicleClassLowThreshold =
                        ObserveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase(
                            vehicleClassBrakeWearPreferencesRepository,
                        ),
                ),
            remainingVirtualEnergyUseCases =
                RemainingVirtualEnergyUseCases(
                    observeRemainingVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(virtualEnergyRepository),
                    observeReadoutHotkey = ObserveReadoutHotkeyUseCase(hotkeyRepository),
                    observeManualReadoutHotkeyPresses =
                        ObserveManualReadoutHotkeyPressesUseCase(
                            manualHotkeyRepository,
                        ),
                    observeManualReadoutRequests = ObserveManualReadoutRequestsUseCase(manualRequestRepository),
                    observeManualReadoutSets = ObserveManualReadoutSetsUseCase(manualSetRepository),
                    observeThresholdPercentage =
                        ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase(
                            remainingVirtualEnergyPreferencesRepository,
                        ),
                ),
            remainingFuelUseCases =
                RemainingFuelUseCases(
                    ObserveLmuWindowsFuelUseCase(lmuWindowsRepository),
                    ObserveLmuWindowsRemainingFuelThresholdPercentageUseCase(remainingFuelPreferencesRepository),
                ),
            pitTimingUseCases =
                PitTimingUseCases(
                    observeVirtualEnergyLapsThreshold =
                        ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase(
                            pitTimingPreferencesRepository,
                        ),
                    observeTyreWearLapsThreshold =
                        ObserveLmuWindowsPitTimingTyreWearLapsUseCase(
                            pitTimingPreferencesRepository,
                        ),
                    observeEnabledStates =
                        ObserveLmuWindowsPitTimingEnabledStatesUseCase(
                            pitTimingPreferencesRepository,
                        ),
                ),
            eventProcessor =
                LmuWindowsNarratorEventProcessor(
                    ttsEngine = ttsEngine,
                    saveTelemetryLog = SaveTelemetryLogUseCase(telemetryLogRepository),
                    readoutText =
                        readoutTextOverride ?: {
                            if (it is LmuWindowsMyBestLap) {
                                myBestLapReadoutText?.let { template ->
                                    formatLmuWindowsMyBestLapReadoutText(template, it.lapTimeMs)
                                }
                            } else if (it is LmuWindowsOverheating) {
                                overheatReadoutText
                            } else if (it is LmuWindowsPartDetached) {
                                partDetachedReadoutText
                            } else if (it is LmuWindowsTyreDetached) {
                                tyreDetachedReadoutText
                            } else if (it is LmuWindowsTyreOverheat) {
                                tyreOverheatReadoutText
                            } else if (it is LmuWindowsTyreCold) {
                                tyreColdReadoutText
                            } else if (
                                it is LmuWindowsPitTimingWarning &&
                                it.source == PitTimingSource.TyreWear
                            ) {
                                "タイヤ交換へ"
                            } else {
                                it.narratedText
                            }
                        },
                ),
            narratorUseCases =
                NarratorUseCases(
                    myBestLapReadout = DetermineLmuWindowsMyBestLapReadoutUseCase(),
                    vehicleReadout = DetermineLmuWindowsVehicleReadoutUseCase(),
                    tyreReadout = DetermineLmuWindowsTyreReadoutUseCase(),
                    brakeReadout = DetermineLmuWindowsBrakeReadoutUseCase(),
                    virtualEnergyReadout = DetermineLmuWindowsVirtualEnergyReadoutUseCase(),
                    fuelReadout = DetermineLmuWindowsFuelReadoutUseCase(),
                    manualReadout = DetermineLmuWindowsManualReadoutUseCase(),
                    pitTimingReadout = DetermineLmuWindowsPitTimingReadoutUseCase(),
                    raceFlagReadout = DetermineLmuWindowsRaceFlagReadoutUseCase(),
                ),
            currentTimeMs = currentTimeMs,
        )
    }

    private fun mockTts(spokenTexts: MutableList<SpeechEvent>): TextToSpeechEngine {
        every { ttsEngine.currentReadoutItemKey } returns null
        every { ttsEngine.speak(capture(spokenTexts), queue = false) } just Runs
        every { ttsEngine.speak(capture(spokenTexts), queue = true) } just Runs
        return ttsEngine
    }

    private interface PriorityAwareTts : TextToSpeechEngine {
        val stopCalled: Boolean
    }

    private fun mockPriorityAwareTts(
        spokenTexts: MutableList<SpeechEvent>,
        initialKey: ReadoutItemKey?,
    ): PriorityAwareTts {
        var currentKey = initialKey
        var stopCalled = false
        every { priorityAwareTts.currentReadoutItemKey } answers { currentKey }
        every { priorityAwareTts.speak(capture(spokenTexts), queue = false) } just Runs
        every { priorityAwareTts.speak(capture(spokenTexts), queue = true) } just Runs
        every { priorityAwareTts.stop() } answers {
            stopCalled = true
            currentKey = null
        }
        every { priorityAwareTts.stopCalled } answers { stopCalled }
        return priorityAwareTts
    }

    private fun stubTelemetryLogSave(
        logs: MutableList<TelemetryLog>,
        createdAt: Long,
        readoutItemKey: ReadoutItemKey,
    ) {
        val telemetryJsonSlot = slot<String>()
        val narratedTextSlot = slot<String>()
        val narrationOutcomeSlot = slot<NarrationOutcome>()
        coEvery {
            telemetryLogRepository.saveTelemetryLog(
                createdAt = createdAt,
                simulator = Simulator.LmuWindows,
                readoutItemKey = readoutItemKey,
                narratedText = capture(narratedTextSlot),
                narrationOutcome = capture(narrationOutcomeSlot),
                telemetryJson = capture(telemetryJsonSlot),
            )
        } answers {
            logs.add(
                TelemetryLog(
                    id = 0,
                    createdAt = createdAt,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = readoutItemKey,
                    narratedText = narratedTextSlot.captured,
                    narrationOutcome = narrationOutcomeSlot.captured,
                    telemetryJson = telemetryJsonSlot.captured,
                ),
            )
        }
    }

    // --- 自己ベストラップ ---

    @Test
    fun `手動エネルギー周回数は蓄積前と鮮度切れをデータ無しにし自動スイッチに依存しない`() =
        runTest(testDispatcher) {
            val telemetry = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val tyres = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val logs = mutableListOf<TelemetryLog>()
            val key = LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value
            val root = LmuWindowsReadoutItemKey.PitTiming.Root
            var currentTime = 0L
            createViewModel(
                telemetryChannel = telemetry,
                remainingVirtualEnergyChannel = energy,
                tyreWearChannel = tyres,
                manualRequestChannel = requests,
                manualSets = listOf(ManualReadoutSet("laps", listOf(key))),
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(root to false),
                pitTimingEnabledOverrides = mapOf(LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy to false),
                currentTimeMs = { currentTime },
            )
            stubTelemetryLogSave(logs, createdAt = 0L, root)
            val request = ManualReadoutRequest(Simulator.LmuWindows.id, "laps")
            requests.send(request)
            tyres.send(tyreWear())
            energy.send(remainingVirtualEnergy(remainingRatio = 1.0))
            telemetry.send(fakeTelemetryData(currentLap = 0, bestLapTimeMs = 90_000L))
            telemetry.send(fakeTelemetryData(currentLap = 1, bestLapTimeMs = 90_000L))
            requests.send(request)
            energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
            telemetry.send(fakeTelemetryData(currentLap = 2, bestLapTimeMs = 90_000L))
            requests.send(request)
            requests.send(request)
            assertEquals(logs[2].telemetryJson, logs[3].telemetryJson)
            currentTime = 5_000L
            stubTelemetryLogSave(logs, createdAt = currentTime, root)
            requests.send(request)
            currentTime = 5_001L
            stubTelemetryLogSave(logs, createdAt = currentTime, root)
            requests.send(request)
            assertEquals(
                listOf(
                    "データがありません",
                    "データがありません",
                    "エネルギーは残り約4周",
                    "エネルギーは残り約4周",
                    "エネルギーは残り約4周",
                    "データがありません",
                ),
                spoken.map { it.narratedText },
            )
            assertEquals(List(6) { root }, spoken.map { it.readoutItemKey })
        }

    @Test
    fun `手動エネルギー周回数を読んでも同じ周回の自動ピット判定を抑制しない`() =
        runTest(testDispatcher) {
            val telemetry = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val tyres = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            var currentTime = 0L
            createViewModel(
                telemetryChannel = telemetry,
                remainingVirtualEnergyChannel = energy,
                tyreWearChannel = tyres,
                manualRequestChannel = requests,
                manualSets =
                    listOf(ManualReadoutSet("laps", listOf(LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value))),
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.PitTiming.Root to true),
                currentTimeMs = { currentTime },
            )
            tyres.send(tyreWear())
            energy.send(remainingVirtualEnergy(remainingRatio = 1.0))
            telemetry.send(fakeTelemetryData(currentLap = 0, bestLapTimeMs = 90_000L))
            telemetry.send(fakeTelemetryData(currentLap = 1, bestLapTimeMs = 90_000L))
            energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
            telemetry.send(fakeTelemetryData(currentLap = 2, bestLapTimeMs = 90_000L))
            energy.send(remainingVirtualEnergy(remainingRatio = 0.5))
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "laps"))
            currentTime = 60_000L
            energy.send(remainingVirtualEnergy(remainingRatio = 0.5))
            assertEquals(
                listOf("エネルギーは残り約2周", "エネルギー切れのため2周以内にピットイン"),
                spoken.map { it.narratedText },
            )
        }

    @Test
    fun `手動タイヤ摩耗周回数は蓄積前と鮮度切れをデータ無しにし自動スイッチに依存しない`() =
        runTest(testDispatcher) {
            val telemetry = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val tyres = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val logs = mutableListOf<TelemetryLog>()
            val key = LmuWindowsReadoutItemKey.PitTiming.TyreWear.value
            val root = LmuWindowsReadoutItemKey.PitTiming.Root
            var currentTime = 0L
            createViewModel(
                telemetryChannel = telemetry,
                remainingVirtualEnergyChannel = energy,
                tyreWearChannel = tyres,
                manualRequestChannel = requests,
                manualSets = listOf(ManualReadoutSet("laps", listOf(key))),
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(root to false),
                pitTimingEnabledOverrides = mapOf(LmuWindowsReadoutItemKey.PitTiming.TyreWear to false),
                currentTimeMs = { currentTime },
            )
            stubTelemetryLogSave(logs, createdAt = 0L, root)
            val request = ManualReadoutRequest(Simulator.LmuWindows.id, "laps")
            requests.send(request)
            energy.send(remainingVirtualEnergy(remainingRatio = 1.0))
            tyres.send(tyreWear(fl = 1.0))
            telemetry.send(fakeTelemetryData(currentLap = 0, bestLapTimeMs = 90_000L))
            telemetry.send(fakeTelemetryData(currentLap = 1, bestLapTimeMs = 90_000L))
            requests.send(request)
            tyres.send(tyreWear(fl = 0.8))
            telemetry.send(fakeTelemetryData(currentLap = 2, bestLapTimeMs = 90_000L))
            requests.send(request)
            requests.send(request)
            assertEquals(logs[2].telemetryJson, logs[3].telemetryJson)
            currentTime = 5_000L
            stubTelemetryLogSave(logs, createdAt = currentTime, root)
            requests.send(request)
            currentTime = 5_001L
            stubTelemetryLogSave(logs, createdAt = currentTime, root)
            requests.send(request)
            assertEquals(
                listOf(
                    "データがありません",
                    "データがありません",
                    "タイヤは残り約4周",
                    "タイヤは残り約4周",
                    "タイヤは残り約4周",
                    "データがありません",
                ),
                spoken.map { it.narratedText },
            )
            assertEquals(List(6) { root }, spoken.map { it.readoutItemKey })
        }

    @Test
    fun `手動タイヤ摩耗周回数を読んでも同じ周回の自動ピット判定を抑制しない`() =
        runTest(testDispatcher) {
            val telemetry = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val tyres = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            var currentTime = 0L
            createViewModel(
                telemetryChannel = telemetry,
                remainingVirtualEnergyChannel = energy,
                tyreWearChannel = tyres,
                manualRequestChannel = requests,
                manualSets =
                    listOf(ManualReadoutSet("laps", listOf(LmuWindowsReadoutItemKey.PitTiming.TyreWear.value))),
                pitTimingTyreWearLapsThreshold = 3,
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.PitTiming.Root to true),
                currentTimeMs = { currentTime },
            )
            energy.send(remainingVirtualEnergy(remainingRatio = 1.0))
            tyres.send(tyreWear(fl = 1.0))
            telemetry.send(fakeTelemetryData(currentLap = 0, bestLapTimeMs = 90_000L))
            telemetry.send(fakeTelemetryData(currentLap = 1, bestLapTimeMs = 90_000L))
            tyres.send(tyreWear(fl = 0.8))
            telemetry.send(fakeTelemetryData(currentLap = 2, bestLapTimeMs = 90_000L))
            tyres.send(tyreWear(fl = 0.5))
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "laps"))
            currentTime = 60_000L
            tyres.send(tyreWear(fl = 0.5))
            assertEquals(
                listOf("タイヤは残り約2周", "タイヤ摩耗のため2周以内にピットイン"),
                spoken.map { it.narratedText },
            )
        }

    @Test
    fun `手動タイヤ温度は過熱と低温の自動警告状態を維持しフラッグ通知で鮮度を更新しない`() =
        runTest(testDispatcher) {
            val temperature = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val flags = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val key = LmuWindowsReadoutItemKey.TyreTemperature.Root
            var time = 0L
            createViewModel(
                tyreTemperatureChannel = temperature,
                flagChannel = flags,
                manualRequestChannel = requests,
                manualSets = listOf(ManualReadoutSet("temperature", listOf(key.value))),
                enabledOverrides = mapOf(key to true),
                tyreTemperatureHighThreshold = 90,
                tyreTemperatureLowWarningPhasesOverride = mapOf(SessionPhase.GARAGE to true),
                ttsEngine = mockTts(spoken),
                currentTimeMs = { time },
            )
            val request = ManualReadoutRequest(Simulator.LmuWindows.id, "temperature")
            flags.send(clearFlags(gamePhase = SessionPhase.GREEN_FLAG))
            temperature.send(tyreTemperature(fl = 95.0))
            requests.send(request)
            temperature.send(tyreTemperature(fl = 100.0))
            assertEquals(listOf("タイヤ過熱 95度", "タイヤ温度95℃"), spoken.map { it.narratedText })
            temperature.send(tyreTemperature(fl = 55.0))
            flags.send(clearFlags(gamePhase = SessionPhase.GARAGE))
            requests.send(request)
            temperature.send(tyreTemperature(fl = 55.0))
            assertEquals(
                listOf("タイヤ過熱 95度", "タイヤ温度95℃", "タイヤ低温 55度", "タイヤ温度55℃"),
                spoken.map { it.narratedText },
            )
            time = 5_001L
            flags.send(clearFlags(gamePhase = SessionPhase.GREEN_FLAG))
            requests.send(request)
            assertEquals("データがありません", spoken.last().narratedText)
            temperature.send(tyreTemperature(fl = 95.0))
            assertEquals("タイヤ過熱 95度", spoken.last().narratedText)
            assertEquals(6, spoken.size)
        }

    @Test
    fun `手動セットは自己ベストとVEとタイヤを保存順に連結する`() =
        runTest(testDispatcher) {
            val telemetry = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val wear = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val key = LmuWindowsReadoutItemKey.MyBestLap.Root
            createViewModel(
                telemetryChannel = telemetry,
                remainingVirtualEnergyChannel = energy,
                tyreWearChannel = wear,
                manualRequestChannel = requests,
                manualSets =
                    listOf(
                        ManualReadoutSet(
                            "mixed",
                            listOf(
                                key.value,
                                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value,
                                LmuWindowsReadoutItemKey.TyreWear.Root.value,
                            ),
                            mapOf(key.value to "記録{laptime}"),
                        ),
                    ),
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(key to false),
                queueEnabledOverrides = mapOf(key to false),
            )
            telemetry.send(fakeTelemetryData(bestLapTimeMs = 83_456L))
            energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
            wear.send(tyreWear(fl = 0.7))
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "mixed"))
            assertEquals(
                listOf("記録1分23秒456、バーチャルエネルギー残量は80パーセント、タイヤ摩耗残り70%"),
                spoken.map { it.narratedText },
            )
            assertEquals(key, spoken.single().readoutItemKey)
        }

    @Test
    fun `手動セットはタイヤ温度とVEとタイヤを保存順に連結する`() =
        runTest(testDispatcher) {
            val temperature = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val wear = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val key = LmuWindowsReadoutItemKey.TyreTemperature.Root
            createViewModel(
                tyreTemperatureChannel = temperature,
                remainingVirtualEnergyChannel = energy,
                tyreWearChannel = wear,
                manualRequestChannel = requests,
                manualSets =
                    listOf(
                        ManualReadoutSet(
                            "mixed",
                            listOf(
                                key.value,
                                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value,
                                LmuWindowsReadoutItemKey.TyreWear.Root.value,
                            ),
                            mapOf(key.value to "温度{celsius}℃"),
                        ),
                    ),
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(key to false),
                queueEnabledOverrides = mapOf(key to false),
            )
            temperature.send(tyreTemperature(fl = 80.0, fr = 90.5, rl = 85.0, rr = 75.0))
            energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
            wear.send(tyreWear(fl = 0.7))
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "mixed"))
            assertEquals(
                listOf("温度91℃、バーチャルエネルギー残量は80パーセント、タイヤ摩耗残り70%"),
                spoken.map { it.narratedText },
            )
            assertEquals(key, spoken.single().readoutItemKey)
        }

    @Test
    fun `手動セットはブレーキ温度とVEとタイヤを保存順に連結する`() =
        runTest(testDispatcher) {
            val brake = Channel<LmuWindowsBrakeTemperatureData>(Channel.UNLIMITED)
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val wear = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val key = LmuWindowsReadoutItemKey.BrakeTemperature.Root
            createViewModel(
                brakeTemperatureChannel = brake,
                remainingVirtualEnergyChannel = energy,
                tyreWearChannel = wear,
                manualRequestChannel = requests,
                manualSets =
                    listOf(
                        ManualReadoutSet(
                            "mixed",
                            listOf(
                                key.value,
                                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value,
                                LmuWindowsReadoutItemKey.TyreWear.Root.value,
                            ),
                            mapOf(key.value to "温度{celsius}℃"),
                        ),
                    ),
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(key to false),
                queueEnabledOverrides = mapOf(key to false),
            )
            brake.send(brakeTemperature(fl = 400.0, fr = 500.5, rl = 450.0, rr = 350.0))
            energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
            wear.send(tyreWear(fl = 0.7))
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "mixed"))
            assertEquals(
                listOf("温度501℃、バーチャルエネルギー残量は80パーセント、タイヤ摩耗残り70%"),
                spoken.map { it.narratedText },
            )
            assertEquals(key, spoken.single().readoutItemKey)
        }

    @Test
    fun `手動セットはブレーキ残量とVEとタイヤを保存順に連結する`() =
        runTest(testDispatcher) {
            val brake = Channel<LmuWindowsBrakeWearData?>(Channel.UNLIMITED)
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val wear = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val key = LmuWindowsReadoutItemKey.BrakeWear.Root
            createViewModel(
                brakeWearChannel = brake,
                remainingVirtualEnergyChannel = energy,
                tyreWearChannel = wear,
                manualRequestChannel = requests,
                manualSets =
                    listOf(
                        ManualReadoutSet(
                            "mixed",
                            listOf(
                                key.value,
                                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value,
                                LmuWindowsReadoutItemKey.TyreWear.Root.value,
                            ),
                            mapOf(key.value to "ブレーキ{percent}%"),
                        ),
                    ),
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(key to false),
                queueEnabledOverrides = mapOf(key to false),
            )
            brake.send(brakeWearData(thickness = 0.035f))
            brake.send(brakeWearData(thickness = 0.03f))
            energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
            wear.send(tyreWear(fl = 0.7))
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "mixed"))
            assertEquals(
                listOf("ブレーキ50%、バーチャルエネルギー残量は80パーセント、タイヤ摩耗残り70%"),
                spoken.map { it.narratedText },
            )
            assertEquals(key, spoken.single().readoutItemKey)
        }

    @Test
    fun `複数項目の手動依頼は欠損と鮮度を反映し依頼ごとに一発話にする`() =
        runTest(testDispatcher) {
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val wear = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val energyKey = LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root
            val tyreKey = LmuWindowsReadoutItemKey.TyreWear.Root
            var time = 0L
            createViewModel(
                remainingVirtualEnergyChannel = energy,
                tyreWearChannel = wear,
                manualRequestChannel = requests,
                manualSets = listOf(ManualReadoutSet("both", listOf("unknown", energyKey.value, tyreKey.value))),
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(energyKey to false, tyreKey to false),
                queueEnabledOverrides = mapOf(energyKey to false, tyreKey to false),
                currentTimeMs = { time },
            )
            val request = ManualReadoutRequest(Simulator.LmuWindows.id, "both")
            requests.send(request)
            energy.send(remainingVirtualEnergy(remainingRatio = 0.5))
            requests.send(request)
            time = 1L
            wear.send(tyreWear(fl = 0.795))
            requests.send(request)
            time = 5_001L
            requests.send(request)
            time = 5_002L
            requests.send(request)
            assertEquals(
                listOf(
                    "データがありません",
                    "バーチャルエネルギー残量は50パーセント、タイヤ摩耗はデータがありません",
                    "バーチャルエネルギー残量は50パーセント、タイヤ摩耗残り80%",
                    "バーチャルエネルギー残量はデータがありません、タイヤ摩耗残り80%",
                    "データがありません",
                ),
                spoken.map { it.narratedText },
            )
            assertEquals(List(5) { energyKey }, spoken.map { it.readoutItemKey })
        }

    @Test
    fun `自由文言の解決に失敗しても次のテレメトリを収集して読み上げる`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            val key = LmuWindowsReadoutItemKey.Flag.Root
            val skippedJson = slot<String>()
            val spokenJson = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.LmuWindows,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    capture(skippedJson),
                )
            } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.LmuWindows,
                    key,
                    "復旧",
                    NarrationOutcome.SPOKEN,
                    capture(spokenJson),
                )
            } just Runs
            createViewModel(
                flagChannel = channel,
                ttsEngine = tts,
                readoutTextOverride = {
                    if (it is LmuWindowsBlueFlag) error("preference error")
                    "復旧"
                },
            )

            channel.send(clearFlags())
            channel.send(clearFlags(playerFlag = PrimaryFlag.BLUE))
            channel.send(clearFlags(gamePhase = SessionPhase.RED_FLAG))

            assertEquals(listOf<SpeechEvent>(LmuWindowsRedFlag("復旧")), spokenTexts)
            verify(exactly = 1) { tts.currentReadoutItemKey }
            verify(exactly = 1) { tts.speak(LmuWindowsRedFlag("復旧"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.LmuWindows,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    skippedJson.captured,
                )
            }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.LmuWindows,
                    key,
                    "復旧",
                    NarrationOutcome.SPOKEN,
                    spokenJson.captured,
                )
            }
            confirmVerified(tts, telemetryLogRepository)
        }

    @Test
    fun `自己ベストラップの更新タイムを解決して読み上げる`() =
        runTest(testDispatcher) {
            val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                telemetryChannel = telemetryChannel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.MyBestLap.Root to true),
                orderOverride = listOf(LmuWindowsReadoutItemKey.MyBestLap.Root),
            )

            telemetryChannel.send(fakeTelemetryData(bestLapTimeMs = 60_000L))
            telemetryChannel.send(fakeTelemetryData(bestLapTimeMs = 59_000L))

            assertEquals(
                listOf<SpeechEvent>(LmuWindowsMyBestLap(59_000L, "自己ベストラップ更新 59秒000")),
                spokenTexts,
            )
        }

    @Test
    fun `自己ベストラップが無効のときは読み上げない`() =
        runTest(testDispatcher) {
            val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                telemetryChannel = telemetryChannel,
                ttsEngine = tts,
                enabledOverrides = mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.MyBestLap.Root to false),
                orderOverride = listOf(LmuWindowsReadoutItemKey.MyBestLap.Root),
            )

            telemetryChannel.send(fakeTelemetryData(bestLapTimeMs = 60_000L))
            telemetryChannel.send(fakeTelemetryData(bestLapTimeMs = 59_000L))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `自己ベストラップ読み上げが発生したら現在と直前のテレメトリを保存する`() =
        runTest(testDispatcher) {
            val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val logs = mutableListOf<TelemetryLog>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                telemetryChannel = telemetryChannel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.MyBestLap.Root to true),
                orderOverride = listOf(LmuWindowsReadoutItemKey.MyBestLap.Root),
                currentTimeMs = { 456L },
            )
            stubTelemetryLogSave(logs, createdAt = 456L, LmuWindowsReadoutItemKey.MyBestLap.Root)

            telemetryChannel.send(fakeTelemetryData(bestLapTimeMs = 60_000L, currentLap = 1))
            telemetryChannel.send(fakeTelemetryData(bestLapTimeMs = 59_000L, currentLap = 2))

            assertEquals(1, logs.size)
            val log = logs.single()
            assertEquals(456L, log.createdAt)
            assertEquals(Simulator.LmuWindows, log.simulator)
            assertEquals(LmuWindowsReadoutItemKey.MyBestLap.Root, log.readoutItemKey)
            assertContains(log.telemetryJson, """"state":{""")
            assertContains(log.telemetryJson, """"previousTelemetry":{"currentLapTimeMs":0""")
            assertContains(log.telemetryJson, """"bestLapTimeMs":60000""")
            assertContains(log.telemetryJson, """"telemetry":{"currentLapTimeMs":0""")
            assertContains(log.telemetryJson, """"bestLapTimeMs":59000""")
            assertContains(log.telemetryJson, """"settings":{""")
            assertContains(log.telemetryJson, """"observedAtMs":456""")
            assertContains(log.telemetryJson, """"finalState":{""")
        }

    // --- 接近アナウンス ---

    @Test
    fun `接近開始時の読み上げが無効のときは接近アナウンスをしない`() =
        runTest(testDispatcher) {
            var fakeTime = 0L
            val channel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                vehicleApproachChannel = channel,
                ttsEngine = tts,
                startReadoutEnabled = false,
                currentTimeMs = { fakeTime },
            )

            channel.send(noVehicleApproach())
            channel.send(leftVehicleApproach(vehicleId = 1))
            fakeTime = 50L
            channel.send(leftVehicleApproach(vehicleId = 1))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `VEHICLE_APPROACHが無効のときはアナウンスしない`() =
        runTest(testDispatcher) {
            var fakeTime = 0L
            val channel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                vehicleApproachChannel = channel,
                ttsEngine = tts,
                enabledOverrides =
                    mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.VehicleApproach.Root to false),
                currentTimeMs = { fakeTime },
            )

            channel.send(noVehicleApproach())
            channel.send(leftVehicleApproach(vehicleId = 1))
            fakeTime = 50L
            channel.send(leftVehicleApproach(vehicleId = 1))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `1周目スキップONかつ現在ラップが0のときはアナウンスしない`() =
        runTest(testDispatcher) {
            var fakeTime = 0L
            val vehicleApproachChannel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
            val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                vehicleApproachChannel = vehicleApproachChannel,
                telemetryChannel = telemetryChannel,
                ttsEngine = tts,
                skipFirstLap = true,
                currentTimeMs = { fakeTime },
            )

            // mLapNumber は 0 スタートのため、1周目（最初の計測周）は 0
            telemetryChannel.send(fakeTelemetryData(currentLap = 0))
            vehicleApproachChannel.send(noVehicleApproach())
            vehicleApproachChannel.send(leftVehicleApproach(vehicleId = 1))
            fakeTime = 50L
            vehicleApproachChannel.send(leftVehicleApproach(vehicleId = 1))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `1周目スキップONでも2周目以降はアナウンスする`() =
        runTest(testDispatcher) {
            var fakeTime = 0L
            val vehicleApproachChannel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
            val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                vehicleApproachChannel = vehicleApproachChannel,
                telemetryChannel = telemetryChannel,
                ttsEngine = tts,
                skipFirstLap = true,
                currentTimeMs = { fakeTime },
            )

            // mLapNumber は 0 スタートのため、2周目は 1
            telemetryChannel.send(fakeTelemetryData(currentLap = 1))
            vehicleApproachChannel.send(noVehicleApproach())
            vehicleApproachChannel.send(leftVehicleApproach(vehicleId = 1))
            fakeTime = 50L
            vehicleApproachChannel.send(leftVehicleApproach(vehicleId = 1))

            assertEquals(listOf<SpeechEvent>(LmuWindowsCarLeft(resolvedText = "カーレフト")), spokenTexts)
        }

    @Test
    fun `左接近が閾値秒数継続するとCarLeftSustainedを読み上げる`() =
        runTest(testDispatcher) {
            var fakeTime = 0L
            val channel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                vehicleApproachChannel = channel,
                ttsEngine = tts,
                sustainedApproachDurationSeconds = LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT,
                currentTimeMs = { fakeTime },
            )

            channel.send(noVehicleApproach())
            channel.send(leftVehicleApproach(vehicleId = 1))
            fakeTime = 7_000L
            channel.send(leftVehicleApproach(vehicleId = 1))

            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsCarLeft(resolvedText = "カーレフト"),
                    LmuWindowsCarLeftSustained(resolvedText = "キープライト"),
                ),
                spokenTexts,
            )
        }

    @Test
    fun `接近継続時の読み上げが無効のときは継続接近を読み上げない`() =
        runTest(testDispatcher) {
            var fakeTime = 0L
            val channel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                vehicleApproachChannel = channel,
                ttsEngine = tts,
                sustainedReadoutEnabled = false,
                sustainedApproachDurationSeconds = LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT,
                currentTimeMs = { fakeTime },
            )

            channel.send(noVehicleApproach())
            channel.send(leftVehicleApproach(vehicleId = 1))
            fakeTime = 7_000L
            channel.send(leftVehicleApproach(vehicleId = 1))

            assertEquals(listOf<SpeechEvent>(LmuWindowsCarLeft(resolvedText = "カーレフト")), spokenTexts)
        }

    @Test
    fun `接近読み上げが発生したら現在と直前のテレメトリを保存する`() =
        runTest(testDispatcher) {
            var fakeTime = 0L
            val channel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
            val logs = mutableListOf<TelemetryLog>()
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                vehicleApproachChannel = channel,
                ttsEngine = tts,
                sustainedReadoutEnabled = false,
                currentTimeMs = { fakeTime },
            )
            stubTelemetryLogSave(logs, createdAt = 123_456L, LmuWindowsReadoutItemKey.VehicleApproach.Root)

            channel.send(noVehicleApproach())
            channel.send(leftVehicleApproach(vehicleId = 1))
            fakeTime = 123_456L
            channel.send(leftVehicleApproach(vehicleId = 1))

            assertEquals(1, logs.size)
            val log = logs.single()
            assertEquals(123_456L, log.createdAt)
            assertEquals(Simulator.LmuWindows, log.simulator)
            assertEquals(LmuWindowsReadoutItemKey.VehicleApproach.Root, log.readoutItemKey)
            assertContains(log.telemetryJson, """"state":{""")
            assertContains(log.telemetryJson, """"previousVehicleApproach":{"sideBySideLeftVehicleIds":[1]""")
            assertContains(log.telemetryJson, """"vehicleApproach":{"sideBySideLeftVehicleIds":[1]""")
            assertContains(log.telemetryJson, """"lateralDistanceLeftMeters":3.0""")
            assertContains(log.telemetryJson, """"settings":{""")
            assertContains(log.telemetryJson, """"observedAtMs":123456""")
            assertContains(log.telemetryJson, """"finalState":{""")
        }

    // --- 優先度 ---

    @Test
    fun `フラッグ読み上げ中に車両接近イベントが来ても読み上げない`() =
        runTest(testDispatcher) {
            var fakeTime = 0L
            val channel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts =
                mockPriorityAwareTts(
                    spokenTexts = spokenTexts,
                    initialKey = LmuWindowsReadoutItemKey.Flag.Root,
                )
            createViewModel(vehicleApproachChannel = channel, ttsEngine = tts, currentTimeMs = { fakeTime })

            channel.send(noVehicleApproach())
            channel.send(leftVehicleApproach(vehicleId = 1))
            fakeTime = 50L
            channel.send(leftVehicleApproach(vehicleId = 1))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `優先度制御で読み上げなかったイベントはSKIPPEDとして保存する`() =
        runTest(testDispatcher) {
            var fakeTime = 0L
            val channel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
            val logs = mutableListOf<TelemetryLog>()
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts =
                mockPriorityAwareTts(
                    spokenTexts = spokenTexts,
                    initialKey = LmuWindowsReadoutItemKey.Flag.Root,
                )
            createViewModel(
                vehicleApproachChannel = channel,
                ttsEngine = tts,
                currentTimeMs = { fakeTime },
            )
            stubTelemetryLogSave(logs, createdAt = 50L, LmuWindowsReadoutItemKey.VehicleApproach.Root)

            channel.send(noVehicleApproach())
            channel.send(leftVehicleApproach(vehicleId = 1))
            fakeTime = 50L
            channel.send(leftVehicleApproach(vehicleId = 1))

            assertEquals(1, logs.size)
            assertEquals(NarrationOutcome.SKIPPED, logs.single().narrationOutcome)
        }

    @Test
    fun `車両接近読み上げ中にフラッグイベントが来ると読み上げを停止して割り込む`() =
        runTest(testDispatcher) {
            val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts =
                mockPriorityAwareTts(
                    spokenTexts = spokenTexts,
                    initialKey = LmuWindowsReadoutItemKey.VehicleApproach.Root,
                )
            createViewModel(flagChannel = flagChannel, ttsEngine = tts)

            flagChannel.send(clearFlags())
            flagChannel.send(clearFlags(playerFlag = PrimaryFlag.BLUE))

            assertEquals(true, tts.stopCalled)
            assertEquals(listOf<SpeechEvent>(LmuWindowsBlueFlag(resolvedText = "ブルーフラッグ")), spokenTexts)
        }

    @Test
    fun `再生中の項目が優先度リストにないときは新しい読み上げで割り込む`() =
        runTest(testDispatcher) {
            var fakeTime = 0L
            val channel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts =
                mockPriorityAwareTts(
                    spokenTexts = spokenTexts,
                    initialKey = LmuWindowsReadoutItemKey.Flag.Root,
                )
            createViewModel(
                vehicleApproachChannel = channel,
                ttsEngine = tts,
                orderOverride = listOf(LmuWindowsReadoutItemKey.VehicleApproach.Root),
                currentTimeMs = { fakeTime },
            )

            channel.send(noVehicleApproach())
            channel.send(leftVehicleApproach(vehicleId = 1))
            fakeTime = 50L
            channel.send(leftVehicleApproach(vehicleId = 1))

            assertEquals(true, tts.stopCalled)
            assertEquals(listOf<SpeechEvent>(LmuWindowsCarLeft(resolvedText = "カーレフト")), spokenTexts)
        }

    @Test
    fun `新しい項目が優先度リストにないときは再生中の読み上げを優先する`() =
        runTest(testDispatcher) {
            var fakeTime = 0L
            val channel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts =
                mockPriorityAwareTts(
                    spokenTexts = spokenTexts,
                    initialKey = LmuWindowsReadoutItemKey.Flag.Root,
                )
            createViewModel(
                vehicleApproachChannel = channel,
                ttsEngine = tts,
                orderOverride = listOf(LmuWindowsReadoutItemKey.Flag.Root),
                currentTimeMs = { fakeTime },
            )

            channel.send(noVehicleApproach())
            channel.send(leftVehicleApproach(vehicleId = 1))
            fakeTime = 50L
            channel.send(leftVehicleApproach(vehicleId = 1))

            assertEquals(false, tts.stopCalled)
            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `キュー設定が有効なら優先度で本来無視される項目もキュー再生する`() =
        runTest(testDispatcher) {
            var fakeTime = 0L
            val channel = Channel<LmuWindowsVehicleApproachData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts =
                mockPriorityAwareTts(
                    spokenTexts = spokenTexts,
                    initialKey = LmuWindowsReadoutItemKey.Flag.Root,
                )
            createViewModel(
                vehicleApproachChannel = channel,
                ttsEngine = tts,
                orderOverride =
                    listOf(
                        LmuWindowsReadoutItemKey.Flag.Root,
                        LmuWindowsReadoutItemKey.VehicleApproach.Root,
                    ),
                queueEnabledOverrides = mapOf(LmuWindowsReadoutItemKey.VehicleApproach.Root to true),
                currentTimeMs = { fakeTime },
            )

            channel.send(noVehicleApproach())
            channel.send(leftVehicleApproach(vehicleId = 1))
            fakeTime = 50L
            channel.send(leftVehicleApproach(vehicleId = 1))

            assertEquals(false, tts.stopCalled)
            assertEquals(listOf<SpeechEvent>(LmuWindowsCarLeft(resolvedText = "カーレフト")), spokenTexts)
        }

    // --- オーバーヒート / フラッグ ---

    @Test
    fun `OVERHEATが無効のときはオーバーヒートを読み上げない`() =
        runTest(testDispatcher) {
            val damageChannel = Channel<LmuWindowsVehicleDamageData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                damageChannel = damageChannel,
                ttsEngine = tts,
                vehicleDamageEnabledOverrides =
                    mapOf<ReadoutItemKey, Boolean>(
                        LmuWindowsReadoutItemKey.VehicleDamage.Overheat to false,
                    ),
            )

            damageChannel.send(noDamage())
            damageChannel.send(noDamage(overheating = true))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `オーバーヒートが発生すると解決済みの文言を読み上げる`() =
        runTest(testDispatcher) {
            val damageChannel = Channel<LmuWindowsVehicleDamageData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                damageChannel = damageChannel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.VehicleDamage.Root to true),
            )

            damageChannel.send(noDamage())
            damageChannel.send(noDamage(overheating = true))

            assertEquals(listOf<SpeechEvent>(LmuWindowsOverheating(resolvedText = "オーバーヒート")), spokenTexts)
        }

    @Test
    fun `オーバーヒートの自由文言を判定時に解決する`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsVehicleDamageData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                damageChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.VehicleDamage.Root to true),
                overheatReadoutText = "カスタム",
            )
            channel.send(noDamage())
            channel.send(noDamage(overheating = true))
            assertEquals(listOf<SpeechEvent>(LmuWindowsOverheating(resolvedText = "カスタム")), spokenTexts)
        }

    @Test
    fun `オーバーヒートの空白文言を判定時に解決する`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsVehicleDamageData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                damageChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.VehicleDamage.Root to true),
                overheatReadoutText = " ",
            )
            channel.send(noDamage())
            channel.send(noDamage(overheating = true))
            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `オーバーヒートのTTS利用不可を判定時に解決する`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsVehicleDamageData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                damageChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.VehicleDamage.Root to true),
                overheatReadoutText = null,
            )
            channel.send(noDamage())
            channel.send(noDamage(overheating = true))
            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `自己ベストラップの自由文言を判定時に解決する`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                telemetryChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.MyBestLap.Root to true),
                myBestLapReadoutText = "カスタム{laptime}",
            )
            channel.send(fakeTelemetryData(bestLapTimeMs = 60_000L))
            channel.send(fakeTelemetryData(bestLapTimeMs = 59_000L))
            assertEquals(
                listOf<SpeechEvent>(LmuWindowsMyBestLap(59_000L, resolvedText = "カスタム59秒000")),
                spokenTexts,
            )
        }

    @Test
    fun `自己ベストラップの空白文言を判定時に解決する`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                telemetryChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.MyBestLap.Root to true),
                myBestLapReadoutText = " ",
            )
            channel.send(fakeTelemetryData(bestLapTimeMs = 60_000L))
            channel.send(fakeTelemetryData(bestLapTimeMs = 59_000L))
            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `自己ベストラップのTTS利用不可を判定時に解決する`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                telemetryChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.MyBestLap.Root to true),
                myBestLapReadoutText = null,
            )
            channel.send(fakeTelemetryData(bestLapTimeMs = 60_000L))
            channel.send(fakeTelemetryData(bestLapTimeMs = 59_000L))
            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `部品脱落の自由文言を判定時に解決する`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsVehicleDamageData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                damageChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.VehicleDamage.Root to true),
                partDetachedReadoutText = "カスタム",
            )
            channel.send(noDamage())
            channel.send(noDamage(partDetached = true))
            assertEquals(listOf<SpeechEvent>(LmuWindowsPartDetached(resolvedText = "カスタム")), spokenTexts)
        }

    @Test
    fun `部品脱落の空白文言を判定時に解決する`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsVehicleDamageData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                damageChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.VehicleDamage.Root to true),
                partDetachedReadoutText = " ",
            )
            channel.send(noDamage())
            channel.send(noDamage(partDetached = true))
            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `部品脱落のTTS利用不可を判定時に解決する`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsVehicleDamageData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                damageChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.VehicleDamage.Root to true),
                partDetachedReadoutText = null,
            )
            channel.send(noDamage())
            channel.send(noDamage(partDetached = true))
            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `タイヤ脱落の自由文言を判定時に解決する`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreDetachedData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreDetachedChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.VehicleDamage.Root to true),
                tyreDetachedReadoutText = "カスタム",
            )
            channel.send(noTyreDetached())
            channel.send(noTyreDetached(WheelIndex.FRONT_LEFT))
            assertEquals(listOf<SpeechEvent>(LmuWindowsTyreDetached(resolvedText = "カスタム")), spokenTexts)
        }

    @Test
    fun `タイヤ脱落の空白文言を判定時に解決する`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreDetachedData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreDetachedChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.VehicleDamage.Root to true),
                tyreDetachedReadoutText = " ",
            )
            channel.send(noTyreDetached())
            channel.send(noTyreDetached(WheelIndex.FRONT_LEFT))
            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `タイヤ脱落のTTS利用不可を判定時に解決する`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreDetachedData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreDetachedChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.VehicleDamage.Root to true),
                tyreDetachedReadoutText = null,
            )
            channel.send(noTyreDetached())
            channel.send(noTyreDetached(WheelIndex.FRONT_LEFT))
            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `部品脱落が発生するとPartDetachedを読み上げる`() =
        runTest(testDispatcher) {
            val damageChannel = Channel<LmuWindowsVehicleDamageData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                damageChannel = damageChannel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.VehicleDamage.Root to true),
            )

            damageChannel.send(noDamage())
            damageChannel.send(noDamage(partDetached = true))

            assertEquals(listOf<SpeechEvent>(LmuWindowsPartDetached(resolvedText = "部品脱落")), spokenTexts)
        }

    @Test
    fun `PART_DETACHEDが無効のときは部品脱落を読み上げない`() =
        runTest(testDispatcher) {
            val damageChannel = Channel<LmuWindowsVehicleDamageData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                damageChannel = damageChannel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.VehicleDamage.Root to true),
                vehicleDamageEnabledOverrides =
                    mapOf<ReadoutItemKey, Boolean>(
                        LmuWindowsReadoutItemKey.VehicleDamage.PartDetached to false,
                    ),
            )

            damageChannel.send(noDamage())
            damageChannel.send(noDamage(partDetached = true))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `タイヤ脱落が発生するとTyreDetachedを読み上げる`() =
        runTest(testDispatcher) {
            val tyreDetachedChannel = Channel<LmuWindowsTyreDetachedData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreDetachedChannel = tyreDetachedChannel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.VehicleDamage.Root to true),
            )

            tyreDetachedChannel.send(noTyreDetached())
            tyreDetachedChannel.send(noTyreDetached(WheelIndex.FRONT_LEFT))

            assertEquals(listOf<SpeechEvent>(LmuWindowsTyreDetached(resolvedText = "タイヤ脱落")), spokenTexts)
        }

    @Test
    fun `TYRE_DETACHEDが無効のときはタイヤ脱落を読み上げない`() =
        runTest(testDispatcher) {
            val tyreDetachedChannel = Channel<LmuWindowsTyreDetachedData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreDetachedChannel = tyreDetachedChannel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.VehicleDamage.Root to true),
                vehicleDamageEnabledOverrides =
                    mapOf<ReadoutItemKey, Boolean>(
                        LmuWindowsReadoutItemKey.VehicleDamage.TyreDetached to false,
                    ),
            )

            tyreDetachedChannel.send(noTyreDetached())
            tyreDetachedChannel.send(noTyreDetached(WheelIndex.FRONT_LEFT))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `BLUE_FLAGが無効のときはブルーフラッグを読み上げない`() =
        runTest(testDispatcher) {
            val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                flagChannel = flagChannel,
                ttsEngine = tts,
                flagEnabledOverrides = mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.Flag.BlueFlag to false),
            )

            flagChannel.send(clearFlags())
            flagChannel.send(clearFlags(playerFlag = PrimaryFlag.BLUE))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `フラッグが無効のときは詳細フラッグ設定が有効でも読み上げない`() =
        runTest(testDispatcher) {
            val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                flagChannel = flagChannel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.Flag.Root to false),
                flagEnabledOverrides =
                    mapOf(
                        LmuWindowsReadoutItemKey.Flag.BlueFlag to true,
                        LmuWindowsReadoutItemKey.Flag.SectorYellowFlag to true,
                        LmuWindowsReadoutItemKey.Flag.FullCourseYellow to true,
                        LmuWindowsReadoutItemKey.Flag.RedFlag to true,
                    ),
            )

            flagChannel.send(clearFlags())
            flagChannel.send(
                clearFlags(
                    gamePhase = SessionPhase.FULL_COURSE_YELLOW,
                    playerFlag = PrimaryFlag.BLUE,
                    sectorFlags = listOf(SectorFlagState.YELLOW, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
                ),
            )

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `ブルーフラッグ読み上げが発生したら現在と直前のテレメトリを保存する`() =
        runTest(testDispatcher) {
            var fakeTime = 0L
            val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val logs = mutableListOf<TelemetryLog>()
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                flagChannel = flagChannel,
                ttsEngine = tts,
                currentTimeMs = { fakeTime },
            )
            stubTelemetryLogSave(logs, createdAt = 789L, LmuWindowsReadoutItemKey.Flag.Root)

            flagChannel.send(clearFlags())
            fakeTime = 789L
            flagChannel.send(clearFlags(playerFlag = PrimaryFlag.BLUE))

            assertEquals(1, logs.size)
            val log = logs.single()
            assertEquals(789L, log.createdAt)
            assertEquals(Simulator.LmuWindows, log.simulator)
            assertEquals(LmuWindowsReadoutItemKey.Flag.Root, log.readoutItemKey)
            assertContains(log.telemetryJson, """"state":{""")
            assertContains(log.telemetryJson, """"previousRaceFlags":{"gamePhase":"GREEN_FLAG"""")
            assertContains(log.telemetryJson, """"raceFlags":{"gamePhase":"GREEN_FLAG"""")
            assertContains(log.telemetryJson, """"playerFlag":"BLUE"""")
            assertContains(log.telemetryJson, """"settings":{""")
            assertContains(log.telemetryJson, """"observedAtMs":789""")
            assertContains(log.telemetryJson, """"finalState":{""")
        }

    @Test
    fun `レッドフラッグイベントを読み上げる`() =
        runTest(testDispatcher) {
            val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                flagChannel = flagChannel,
                ttsEngine = tts,
            )

            flagChannel.send(clearFlags())
            flagChannel.send(clearFlags(gamePhase = SessionPhase.RED_FLAG))

            assertEquals(listOf<SpeechEvent>(LmuWindowsRedFlag(resolvedText = "レッドフラッグ")), spokenTexts)
        }

    @Test
    fun `ログ保存に失敗しても以後の読み上げは継続する`() =
        runTest(testDispatcher) {
            val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                flagChannel = flagChannel,
                ttsEngine = tts,
            )
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = LmuWindowsReadoutItemKey.Flag.Root,
                    narratedText = any(),
                    narrationOutcome = any(),
                    telemetryJson = capture(slot()),
                )
            } throws IllegalStateException("Failed to save")

            flagChannel.send(clearFlags())
            flagChannel.send(clearFlags(playerFlag = PrimaryFlag.BLUE))
            flagChannel.send(clearFlags())
            flagChannel.send(clearFlags(gamePhase = SessionPhase.RED_FLAG))

            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsBlueFlag(resolvedText = "ブルーフラッグ"),
                    LmuWindowsRedFlag(resolvedText = "レッドフラッグ"),
                ),
                spokenTexts,
            )
        }

    @Test
    fun `オーバーヒート読み上げが発生したら現在と直前のテレメトリを保存する`() =
        runTest(testDispatcher) {
            var fakeTime = 0L
            val damageChannel = Channel<LmuWindowsVehicleDamageData>(Channel.UNLIMITED)
            val logs = mutableListOf<TelemetryLog>()
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                damageChannel = damageChannel,
                ttsEngine = tts,
                currentTimeMs = { fakeTime },
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.VehicleDamage.Root to true),
            )
            stubTelemetryLogSave(logs, createdAt = 987L, LmuWindowsReadoutItemKey.VehicleDamage.Root)

            damageChannel.send(noDamage())
            fakeTime = 987L
            damageChannel.send(noDamage(overheating = true))

            assertEquals(1, logs.size)
            val log = logs.single()
            assertEquals(987L, log.createdAt)
            assertEquals(Simulator.LmuWindows, log.simulator)
            assertEquals(LmuWindowsReadoutItemKey.VehicleDamage.Root, log.readoutItemKey)
            assertContains(log.telemetryJson, """"state":{""")
            assertContains(log.telemetryJson, """"previousVehicleDamage":{"overheating":false""")
            assertContains(log.telemetryJson, """"vehicleDamage":{"overheating":true""")
            assertContains(log.telemetryJson, """"settings":{""")
            assertContains(log.telemetryJson, """"observedAtMs":987""")
            assertContains(log.telemetryJson, """"finalState":{""")
        }

    // --- タイヤ温度 ---

    @Test
    fun `閾値以上のタイヤ温度が来ると TyreOverheat を読み上げる`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreTemperatureChannel = channel,
                flagChannel = flagChannel,
                ttsEngine = tts,
                tyreTemperatureHighThreshold = 90,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.TyreTemperature.Root to true),
            )
            flagChannel.send(clearFlags())

            channel.send(tyreTemperature(fl = 95.0))

            assertEquals(listOf<SpeechEvent>(LmuWindowsTyreOverheat(95, "タイヤを冷やして")), spokenTexts)
        }

    @Test
    fun `走行中の車両クラスに対応する閾値未満では TyreOverheat を読み上げない`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreTemperatureChannel = channel,
                flagChannel = flagChannel,
                ttsEngine = tts,
                vehicleClass = LmuWindowsVehicleClassData.Gt3,
                tyreTemperatureHighThresholdByVehicleClass =
                    lmuWindowsAllVehicleClasses.associateWith { vehicleClass ->
                        if (vehicleClass == LmuWindowsVehicleClassData.Gt3) 100 else 90
                    },
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.TyreTemperature.Root to true),
            )
            flagChannel.send(clearFlags())

            channel.send(tyreTemperature(fl = 95.0))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `高温状態が継続しても2回目は読み上げない`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreTemperatureChannel = channel,
                flagChannel = flagChannel,
                ttsEngine = tts,
                tyreTemperatureHighThreshold = 90,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.TyreTemperature.Root to true),
            )
            flagChannel.send(clearFlags())

            channel.send(tyreTemperature(fl = 95.0))
            channel.send(tyreTemperature(fl = 95.0))

            assertEquals(listOf<SpeechEvent>(LmuWindowsTyreOverheat(95, "タイヤを冷やして")), spokenTexts)
        }

    @Test
    fun `全タイヤが閾値以下に戻ると再度読み上げ可能になる`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreTemperatureChannel = channel,
                flagChannel = flagChannel,
                ttsEngine = tts,
                tyreTemperatureHighThreshold = 90,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.TyreTemperature.Root to true),
            )
            flagChannel.send(clearFlags())

            channel.send(tyreTemperature(fl = 95.0))
            channel.send(tyreTemperature(fl = 20.0))
            channel.send(tyreTemperature(fl = 95.0))

            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsTyreOverheat(95, "タイヤを冷やして"),
                    LmuWindowsTyreOverheat(95, "タイヤを冷やして"),
                ),
                spokenTexts,
            )
        }

    @Test
    fun `タイヤ温度項目が無効なら読み上げない`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreTemperatureChannel = channel,
                flagChannel = flagChannel,
                ttsEngine = tts,
                tyreTemperatureHighThreshold = 90,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.TyreTemperature.Root to false),
            )
            flagChannel.send(clearFlags())

            channel.send(tyreTemperature(fl = 95.0))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `過熱警告スイッチがOFFのときは読み上げない`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreTemperatureChannel = channel,
                flagChannel = flagChannel,
                ttsEngine = tts,
                tyreTemperatureHighThreshold = 90,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.TyreTemperature.Root to true),
                tyreTemperatureOverheatWarningEnabled = false,
            )
            flagChannel.send(clearFlags())

            channel.send(tyreTemperature(fl = 95.0))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `タイヤ温度がOFFの場合は読み上げない`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreTemperatureChannel = channel,
                flagChannel = flagChannel,
                ttsEngine = tts,
                tyreTemperatureHighThreshold = 90,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.TyreTemperature.Root to false),
            )
            flagChannel.send(clearFlags())

            channel.send(tyreTemperature(fl = 95.0))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `タイヤ温度読み上げが発生したらテレメトリを保存する`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val logs = mutableListOf<TelemetryLog>()
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreTemperatureChannel = channel,
                flagChannel = flagChannel,
                ttsEngine = tts,
                tyreTemperatureHighThreshold = 90,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.TyreTemperature.Root to true),
                currentTimeMs = { 123L },
            )
            stubTelemetryLogSave(logs, createdAt = 123L, LmuWindowsReadoutItemKey.TyreTemperature.Root)
            flagChannel.send(clearFlags())

            channel.send(tyreTemperature(fl = 95.0))

            assertEquals(1, logs.size)
            val log = logs.first()
            assertEquals("タイヤを冷やして", log.narratedText)
            assertEquals(NarrationOutcome.QUEUED, log.narrationOutcome)
            assertEquals(123L, log.createdAt)
            assertEquals(Simulator.LmuWindows, log.simulator)
            assertEquals(LmuWindowsReadoutItemKey.TyreTemperature.Root, log.readoutItemKey)
            assertContains(log.telemetryJson, """"state":{""")
            assertContains(
                log.telemetryJson,
                """"input":{"tyreCarcassTemperature":{"wheels":{"FRONT_LEFT":95.0,"FRONT_RIGHT":20.0,""" +
                    """"REAR_LEFT":20.0,"REAR_RIGHT":20.0}},"raceFlags":{""",
            )
            assertContains(log.telemetryJson, """"settings":{""")
            assertContains(log.telemetryJson, """"observedAtMs":123""")
            assertContains(log.telemetryJson, """"overheatState":{""")
            assertContains(log.telemetryJson, """"finalState":{""")
        }

    @Test
    fun `過熱文言が空白またはTTS不可なら読み上げずSKIPPEDを保存する`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val logs = mutableListOf<TelemetryLog>()
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreTemperatureChannel = channel,
                flagChannel = flagChannel,
                ttsEngine = tts,
                tyreTemperatureHighThreshold = 90,
                tyreOverheatReadoutText = null,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.TyreTemperature.Root to true),
                currentTimeMs = { 123L },
            )
            stubTelemetryLogSave(logs, createdAt = 123L, LmuWindowsReadoutItemKey.TyreTemperature.Root)
            flagChannel.send(clearFlags())

            channel.send(tyreTemperature(fl = 95.0))

            assertEquals(1, logs.size)
            val log = logs.first()
            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
            assertEquals("", log.narratedText)
            assertEquals(NarrationOutcome.SKIPPED, log.narrationOutcome)
            assertEquals(123L, log.createdAt)
            assertEquals(Simulator.LmuWindows, log.simulator)
            assertEquals(LmuWindowsReadoutItemKey.TyreTemperature.Root, log.readoutItemKey)
            assertContains(log.telemetryJson, """"state":{""")
            assertContains(
                log.telemetryJson,
                """"input":{"tyreCarcassTemperature":{"wheels":{"FRONT_LEFT":95.0,"FRONT_RIGHT":20.0,""" +
                    """"REAR_LEFT":20.0,"REAR_RIGHT":20.0}},"raceFlags":{""",
            )
            assertContains(log.telemetryJson, """"settings":{""")
            assertContains(log.telemetryJson, """"observedAtMs":123""")
            assertContains(log.telemetryJson, """"overheatState":{""")
            assertContains(log.telemetryJson, """"finalState":{""")
            verify(exactly = 0) { tts.speak(LmuWindowsTyreOverheat(95, "タイヤを冷やして"), queue = false) }
            verify(exactly = 0) { tts.currentReadoutItemKey }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 123L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = LmuWindowsReadoutItemKey.TyreTemperature.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = log.telemetryJson,
                )
            }
            confirmVerified(tts, telemetryLogRepository)
        }

    // --- タイヤ摩耗 ---

    @Test
    fun `閾値以下のタイヤ残存率が来ると TyreWearWarning を読み上げる`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreWearChannel = channel,
                ttsEngine = tts,
                tyreWearThresholdPercentage = 50,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.TyreWear.Root to true),
            )

            channel.send(tyreWear(fl = 0.4))

            assertEquals(
                listOf<SpeechEvent>(LmuWindowsTyreWearWarning(50, resolvedText = "タイヤ残存率50%以下")),
                spokenTexts,
            )
        }

    @Test
    fun `摩耗警告状態が継続しても2回目は読み上げない`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreWearChannel = channel,
                ttsEngine = tts,
                tyreWearThresholdPercentage = 50,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.TyreWear.Root to true),
            )

            channel.send(tyreWear(fl = 0.4))
            channel.send(tyreWear(fl = 0.4))

            assertEquals(
                listOf<SpeechEvent>(LmuWindowsTyreWearWarning(50, resolvedText = "タイヤ残存率50%以下")),
                spokenTexts,
            )
        }

    @Test
    fun `全タイヤが閾値未満に戻るとタイヤ摩耗を再度読み上げる`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreWearChannel = channel,
                ttsEngine = tts,
                tyreWearThresholdPercentage = 50,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.TyreWear.Root to true),
            )

            channel.send(tyreWear(fl = 0.4))
            channel.send(tyreWear(fl = 0.6))
            channel.send(tyreWear(fl = 0.4))

            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsTyreWearWarning(50, resolvedText = "タイヤ残存率50%以下"),
                    LmuWindowsTyreWearWarning(50, resolvedText = "タイヤ残存率50%以下"),
                ),
                spokenTexts,
            )
        }

    @Test
    fun `タイヤ摩耗項目が無効なら読み上げない`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreWearChannel = channel,
                ttsEngine = tts,
                tyreWearThresholdPercentage = 50,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.TyreWear.Root to false),
            )

            channel.send(tyreWear(fl = 0.4))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `タイヤ摩耗の読み上げでテレメトリログを保存する`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val logs = mutableListOf<TelemetryLog>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreWearChannel = channel,
                ttsEngine = tts,
                tyreWearThresholdPercentage = 50,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.TyreWear.Root to true),
                currentTimeMs = { 123L },
            )
            stubTelemetryLogSave(logs, createdAt = 123L, LmuWindowsReadoutItemKey.TyreWear.Root)

            channel.send(tyreWear(fl = 0.4))

            assertEquals(1, logs.size)
            val log = logs.first()
            assertEquals(123L, log.createdAt)
            assertEquals(Simulator.LmuWindows, log.simulator)
            assertEquals(LmuWindowsReadoutItemKey.TyreWear.Root, log.readoutItemKey)
            assertContains(log.telemetryJson, """"state":{""")
            assertContains(log.telemetryJson, """"previousTyreWear":null""")
            assertContains(log.telemetryJson, """"tyreWear":{"wheels":{"FRONT_LEFT":0.4""")
            assertContains(log.telemetryJson, """"settings":{""")
            assertContains(log.telemetryJson, """"observedAtMs":123""")
            assertContains(log.telemetryJson, """"finalState":{""")
        }

    // --- ブレーキ温度 ---

    @Test
    fun `閾値以上のブレーキ温度が来ると BrakeOverheat を読み上げる`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsBrakeTemperatureData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                brakeTemperatureChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.BrakeTemperature.Root to true),
            )

            channel.send(brakeTemperature(fl = 850.0))

            assertEquals(
                listOf<SpeechEvent>(LmuWindowsBrakeOverheat(800, resolvedText = "ブレーキ温度800℃以上")),
                spokenTexts,
            )
        }

    @Test
    fun `ブレーキ過熱状態が継続しても2回目は読み上げない`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsBrakeTemperatureData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                brakeTemperatureChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.BrakeTemperature.Root to true),
            )

            channel.send(brakeTemperature(fl = 850.0))
            channel.send(brakeTemperature(fl = 850.0))

            assertEquals(
                listOf<SpeechEvent>(LmuWindowsBrakeOverheat(800, resolvedText = "ブレーキ温度800℃以上")),
                spokenTexts,
            )
        }

    @Test
    fun `全ブレーキが冷えると再度読み上げる`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsBrakeTemperatureData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                brakeTemperatureChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.BrakeTemperature.Root to true),
            )

            channel.send(brakeTemperature(fl = 850.0))
            channel.send(brakeTemperature(fl = 20.0))
            channel.send(brakeTemperature(fl = 850.0))

            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsBrakeOverheat(800, resolvedText = "ブレーキ温度800℃以上"),
                    LmuWindowsBrakeOverheat(800, resolvedText = "ブレーキ温度800℃以上"),
                ),
                spokenTexts,
            )
        }

    @Test
    fun `ブレーキ温度項目が無効なら読み上げない`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsBrakeTemperatureData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                brakeTemperatureChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.BrakeTemperature.Root to false),
            )

            channel.send(brakeTemperature(fl = 850.0))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `ブレーキ過熱の読み上げでテレメトリログを保存する`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsBrakeTemperatureData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val logs = mutableListOf<TelemetryLog>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                brakeTemperatureChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.BrakeTemperature.Root to true),
                currentTimeMs = { 123L },
            )
            stubTelemetryLogSave(logs, createdAt = 123L, LmuWindowsReadoutItemKey.BrakeTemperature.Root)

            channel.send(brakeTemperature(fl = 850.0))

            assertEquals(1, logs.size)
            val log = logs.first()
            assertEquals(123L, log.createdAt)
            assertEquals(Simulator.LmuWindows, log.simulator)
            assertEquals(LmuWindowsReadoutItemKey.BrakeTemperature.Root, log.readoutItemKey)
            assertContains(log.telemetryJson, """"state":{""")
            assertContains(log.telemetryJson, """"previousBrakeTemperature":null""")
            assertContains(log.telemetryJson, """"brakeTemperature":{"wheels":{"FRONT_LEFT":850.0""")
            assertContains(log.telemetryJson, """"settings":{""")
            assertContains(log.telemetryJson, """"observedAtMs":123""")
            assertContains(log.telemetryJson, """"finalState":{""")
        }

    // --- ブレーキ摩耗 ---

    @Test
    fun `ブレーキ残量が閾値以下になると BrakeWearLow を読み上げる`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsBrakeWearData?>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                brakeWearChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.BrakeWear.Root to true),
            )

            channel.send(brakeWearData(thickness = 0.036f))
            channel.send(brakeWearData(thickness = 0.026f))

            assertEquals(
                listOf<SpeechEvent>(LmuWindowsBrakeWearLow(20, resolvedText = "ブレーキ残量20%以下")),
                spokenTexts,
            )
        }

    @Test
    fun `ブレーキ残量の低下が継続しても2回目は読み上げない`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsBrakeWearData?>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                brakeWearChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.BrakeWear.Root to true),
            )

            channel.send(brakeWearData(thickness = 0.036f))
            channel.send(brakeWearData(thickness = 0.026f))
            channel.send(brakeWearData(thickness = 0.026f))

            assertEquals(
                listOf<SpeechEvent>(LmuWindowsBrakeWearLow(20, resolvedText = "ブレーキ残量20%以下")),
                spokenTexts,
            )
        }

    @Test
    fun `ブレーキ交換後に再び閾値以下になると再度読み上げる`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsBrakeWearData?>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                brakeWearChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.BrakeWear.Root to true),
            )

            channel.send(brakeWearData(thickness = 0.036f))
            channel.send(brakeWearData(thickness = 0.026f))
            channel.send(brakeWearData(thickness = 0.036f))
            channel.send(brakeWearData(thickness = 0.026f))

            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsBrakeWearLow(20, resolvedText = "ブレーキ残量20%以下"),
                    LmuWindowsBrakeWearLow(20, resolvedText = "ブレーキ残量20%以下"),
                ),
                spokenTexts,
            )
        }

    @Test
    fun `ブレーキ残量を取得できない周期は読み上げに影響しない`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsBrakeWearData?>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                brakeWearChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.BrakeWear.Root to true),
            )

            channel.send(null)

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `ブレーキ摩耗項目が無効なら読み上げない`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsBrakeWearData?>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                brakeWearChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.BrakeWear.Root to false),
            )

            channel.send(brakeWearData(thickness = 0.036f))
            channel.send(brakeWearData(thickness = 0.026f))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `ブレーキ残量低下の読み上げでテレメトリログを保存する`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsBrakeWearData?>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val logs = mutableListOf<TelemetryLog>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                brakeWearChannel = channel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.BrakeWear.Root to true),
                currentTimeMs = { 123L },
            )
            stubTelemetryLogSave(logs, createdAt = 123L, LmuWindowsReadoutItemKey.BrakeWear.Root)

            channel.send(brakeWearData(thickness = 0.036f))
            channel.send(brakeWearData(thickness = 0.026f))

            assertEquals(1, logs.size)
            val log = logs.first()
            assertEquals(123L, log.createdAt)
            assertEquals(Simulator.LmuWindows, log.simulator)
            assertEquals(LmuWindowsReadoutItemKey.BrakeWear.Root, log.readoutItemKey)
            assertContains(log.telemetryJson, """"previousBrakeWear":{""")
            assertContains(log.telemetryJson, """"brakeWear":{"wheels":{"FRONT_LEFT":""")
            assertContains(log.telemetryJson, """"settings":{""")
            assertContains(log.telemetryJson, """"finalState":{""")
        }

    // --- バーチャルエネルギー残量 ---

    @Test
    fun `閾値以下のバーチャルエネルギー残量が来ると RemainingVirtualEnergyWarning を読み上げる`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                remainingVirtualEnergyChannel = channel,
                ttsEngine = tts,
                remainingVirtualEnergyThresholdPercentage = 50,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true),
            )

            channel.send(remainingVirtualEnergy(remainingRatio = 0.4))

            assertEquals(listOf<SpeechEvent>(resolvedWarning), spokenTexts)
        }

    @Test
    fun `残量警告状態が継続しても2回目は読み上げない`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                remainingVirtualEnergyChannel = channel,
                ttsEngine = tts,
                remainingVirtualEnergyThresholdPercentage = 50,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true),
            )

            channel.send(remainingVirtualEnergy(remainingRatio = 0.4))
            channel.send(remainingVirtualEnergy(remainingRatio = 0.4))

            assertEquals(listOf<SpeechEvent>(resolvedWarning), spokenTexts)
        }

    @Test
    fun `燃料周回追跡は自動設定に依存せずLMU選択中だけ状態を更新する`() =
        runTest(testDispatcher) {
            val selected = MutableStateFlow<Simulator>(Simulator.Gt7Ps5)
            val telemetry = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val logs = mutableListOf<TelemetryLog>()
            val key = LmuWindowsReadoutItemKey.RemainingFuel.Root
            createViewModel(
                telemetryChannel = telemetry,
                selectedSimulatorFlow = selected,
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(key to false, LmuWindowsReadoutItemKey.PitTiming.Root to false),
                currentTimeMs = { 123L },
            )
            stubTelemetryLogSave(logs, createdAt = 123L, key)
            telemetry.send(fakeTelemetryData(currentLap = 2, fuelCurrentLiters = 75.25, fuelCapacityLiters = 100.0))
            assertEquals(emptyList<SpeechEvent>(), spoken)
            val request = DebugReadoutRequest(Simulator.LmuWindows, key, "状態確認")
            debugRequests.emit(request)
            val initialTracking =
                Json.decodeFromJsonElement<LmuWindowsPitTimingTrackingState>(
                    Json
                        .parseToJsonElement(logs.single().telemetryJson)
                        .jsonObject
                        .getValue("state")
                        .jsonObject
                        .getValue("fuelLapsTrackingState"),
                )
            assertEquals(LmuWindowsPitTimingTrackingState(), initialTracking)

            selected.value = Simulator.LmuWindows
            telemetry.send(fakeTelemetryData(currentLap = 2, fuelCurrentLiters = 75.25, fuelCapacityLiters = 100.0))
            assertEquals(1, spoken.size)
            debugRequests.emit(request)
            val tracking =
                Json.decodeFromJsonElement<LmuWindowsPitTimingTrackingState>(
                    Json
                        .parseToJsonElement(logs.last().telemetryJson)
                        .jsonObject
                        .getValue("state")
                        .jsonObject
                        .getValue("fuelLapsTrackingState"),
                )
            assertEquals(2, tracking.currentLap)
            assertEquals(0.7525, tracking.currentValue)
            assertEquals(123L, tracking.observedAtMs)
            assertEquals(null, tracking.lastValidLapConsumption)
        }

    @Test
    fun `燃料周回追跡はLMUの選択解除で蓄積した消費量を破棄する`() =
        runTest(testDispatcher) {
            val selected = MutableStateFlow<Simulator>(Simulator.LmuWindows)
            val telemetry = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val logs = mutableListOf<TelemetryLog>()
            val key = LmuWindowsReadoutItemKey.RemainingFuel.Root
            createViewModel(
                telemetryChannel = telemetry,
                selectedSimulatorFlow = selected,
                ttsEngine = mockTts(mutableListOf()),
                enabledOverrides = mapOf(key to false, LmuWindowsReadoutItemKey.PitTiming.Root to false),
                currentTimeMs = { 123L },
            )
            stubTelemetryLogSave(logs, createdAt = 123L, key)
            val request = DebugReadoutRequest(Simulator.LmuWindows, key, "状態確認")

            fun trackingFromLastLog(): LmuWindowsPitTimingTrackingState =
                Json.decodeFromJsonElement<LmuWindowsPitTimingTrackingState>(
                    Json
                        .parseToJsonElement(logs.last().telemetryJson)
                        .jsonObject
                        .getValue("state")
                        .jsonObject
                        .getValue("fuelLapsTrackingState"),
                )

            listOf(2 to 80.0, 3 to 70.0, 3 to 60.0, 4 to 50.0).forEach { (lap, liters) ->
                telemetry.send(
                    fakeTelemetryData(currentLap = lap, fuelCurrentLiters = liters, fuelCapacityLiters = 100.0),
                )
            }
            debugRequests.emit(request)
            assertEquals(0.1, assertNotNull(trackingFromLastLog().lastValidLapConsumption), 1e-10)

            selected.value = Simulator.Gt7Ps5
            selected.value = Simulator.LmuWindows
            telemetry.send(fakeTelemetryData(currentLap = 4, fuelCurrentLiters = 40.0, fuelCapacityLiters = 100.0))
            debugRequests.emit(request)
            val tracking = trackingFromLastLog()
            assertEquals(null, tracking.lastValidLapConsumption)
            assertEquals(0.4, tracking.currentLapStartValue)
        }

    @Test
    fun `燃料クラスでは閾値以下で燃料警告を読み上げVE警告は出さない`() =
        runTest(testDispatcher) {
            val fuel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            createViewModel(
                telemetryChannel = fuel,
                remainingVirtualEnergyChannel = energy,
                ttsEngine = mockTts(spoken),
                vehicleClass = LmuWindowsVehicleClassData.P3,
                enabledOverrides =
                    mapOf<ReadoutItemKey, Boolean>(
                        LmuWindowsReadoutItemKey.RemainingFuel.Root to true,
                        LmuWindowsReadoutItemKey.RemainingFuel.WarningReadout to true,
                        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true,
                    ),
            )
            fuel.send(telemetryWithFuel(currentLiters = 10.0, capacityLiters = 100.0))
            energy.send(remainingVirtualEnergy(remainingRatio = 0.1))
            assertEquals(listOf<SpeechEvent>(resolvedFuelWarning), spoken)
        }

    @Test
    fun `VEクラスではVE警告を出し燃料警告は出さない`() =
        runTest(testDispatcher) {
            val fuel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            createViewModel(
                telemetryChannel = fuel,
                remainingVirtualEnergyChannel = energy,
                ttsEngine = mockTts(spoken),
                vehicleClass = LmuWindowsVehicleClassData.Gt3,
                remainingVirtualEnergyThresholdPercentage = 50,
                enabledOverrides =
                    mapOf<ReadoutItemKey, Boolean>(
                        LmuWindowsReadoutItemKey.RemainingFuel.Root to true,
                        LmuWindowsReadoutItemKey.RemainingFuel.WarningReadout to true,
                        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true,
                    ),
            )
            fuel.send(telemetryWithFuel(currentLiters = 10.0, capacityLiters = 100.0))
            energy.send(remainingVirtualEnergy(remainingRatio = 0.1))
            assertEquals(listOf<SpeechEvent>(resolvedWarning), spoken)
        }

    @Test
    fun `燃料警告のRootまたはWarningReadoutが無効なら通知しない`() =
        runTest(testDispatcher) {
            listOf<Map<ReadoutItemKey, Boolean>>(
                mapOf(LmuWindowsReadoutItemKey.RemainingFuel.Root to false),
                mapOf(
                    LmuWindowsReadoutItemKey.RemainingFuel.Root to true,
                    LmuWindowsReadoutItemKey.RemainingFuel.WarningReadout to false,
                ),
            ).forEach { overrides ->
                val fuel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
                val spoken = mutableListOf<SpeechEvent>()
                createViewModel(
                    telemetryChannel = fuel,
                    ttsEngine = mockTts(spoken),
                    vehicleClass = LmuWindowsVehicleClassData.Unknown("UNKNOWN"),
                    enabledOverrides = overrides,
                )
                fuel.send(telemetryWithFuel(currentLiters = 10.0, capacityLiters = 100.0))
                assertEquals(emptyList<SpeechEvent>(), spoken)
            }
        }

    @Test
    fun `他シミュレーター選択中は燃料を購読せず切替後に最新燃料から警告する`() =
        runTest(testDispatcher) {
            val selected = MutableStateFlow<Simulator>(Simulator.Gt7Ps5)
            val fuel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            createViewModel(
                telemetryChannel = fuel,
                selectedSimulatorFlow = selected,
                ttsEngine = mockTts(spoken),
                vehicleClass = LmuWindowsVehicleClassData.P3,
                enabledOverrides =
                    mapOf<ReadoutItemKey, Boolean>(
                        LmuWindowsReadoutItemKey.RemainingFuel.Root to true,
                        LmuWindowsReadoutItemKey.RemainingFuel.WarningReadout to true,
                    ),
            )
            fuel.send(telemetryWithFuel(currentLiters = 1.0, capacityLiters = 100.0))
            assertEquals(emptyList<SpeechEvent>(), spoken)
            selected.value = Simulator.LmuWindows
            fuel.send(telemetryWithFuel(currentLiters = 10.0, capacityLiters = 100.0))
            assertEquals(listOf<SpeechEvent>(resolvedFuelWarning), spoken)
        }

    @Test
    fun `残量が閾値より上に戻るとバーチャルエネルギー残量を再度読み上げる`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                remainingVirtualEnergyChannel = channel,
                ttsEngine = tts,
                remainingVirtualEnergyThresholdPercentage = 50,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true),
            )

            channel.send(remainingVirtualEnergy(remainingRatio = 0.4))
            channel.send(remainingVirtualEnergy(remainingRatio = 0.6))
            channel.send(remainingVirtualEnergy(remainingRatio = 0.4))

            assertEquals(
                listOf<SpeechEvent>(
                    resolvedWarning,
                    resolvedWarning,
                ),
                spokenTexts,
            )
        }

    @Test
    fun `バーチャルエネルギー残量項目が無効なら読み上げない`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                remainingVirtualEnergyChannel = channel,
                ttsEngine = tts,
                remainingVirtualEnergyThresholdPercentage = 50,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to false),
            )

            channel.send(remainingVirtualEnergy(remainingRatio = 0.4))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `バーチャルエネルギー残量の読み上げでテレメトリログを保存する`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val logs = mutableListOf<TelemetryLog>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                remainingVirtualEnergyChannel = channel,
                ttsEngine = tts,
                remainingVirtualEnergyThresholdPercentage = 50,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true),
                currentTimeMs = { 123L },
            )
            stubTelemetryLogSave(logs, createdAt = 123L, LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root)

            channel.send(remainingVirtualEnergy(remainingRatio = 0.4))

            assertEquals(1, logs.size)
            val log = logs.first()
            assertEquals(123L, log.createdAt)
            assertEquals(Simulator.LmuWindows, log.simulator)
            assertEquals(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root, log.readoutItemKey)
            assertContains(log.telemetryJson, """"state":{""")
            assertContains(log.telemetryJson, """"previousRemainingVirtualEnergy":null""")
            assertContains(log.telemetryJson, """"remainingVirtualEnergy":{"remainingRatio":0.4""")
            assertContains(log.telemetryJson, """"settings":{""")
            assertContains(log.telemetryJson, """"observedAtMs":123""")
            assertContains(log.telemetryJson, """"finalState":{""")
        }

    @Test
    fun `ホットキーは最新残量を繰り返し読み上げ警告状態を変えない`() =
        runTest(testDispatcher) {
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val hotkey = Channel<Unit>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val events = mutableListOf<SpeechEvent>()
            val logs = mutableListOf<TelemetryLog>()
            createViewModel(
                remainingVirtualEnergyChannel = energy,
                hotkeyChannel = hotkey,
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true),
                readoutTextOverride = {
                    events.add(it)
                    "現在値"
                },
                currentTimeMs = { 123L },
            )
            stubTelemetryLogSave(logs, 123L, LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root)

            energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
            hotkey.send(Unit)
            energy.send(remainingVirtualEnergy(remainingRatio = 0.2))
            hotkey.send(Unit)
            hotkey.send(Unit)
            energy.send(remainingVirtualEnergy(remainingRatio = 0.2))

            assertEquals(
                listOf(80, 20, 20),
                events.filterIsInstance<LmuWindowsRemainingVirtualEnergyCurrentValue>().map { it.percentage },
            )
            assertEquals(4, events.size)
            assertEquals(4, spoken.size)
            assertEquals(4, logs.size)
            assertEquals(123L, logs.last().createdAt)
            assertContains(logs.last().telemetryJson, "\"remainingRatio\":0.2")
            assertContains(logs.last().telemetryJson, "\"finalState\":")
        }

    @Test
    fun `最後の残量受信から一定時間が経過したホットキーは読み上げない`() =
        runTest(testDispatcher) {
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val hotkey = Channel<Unit>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            var fakeTime = 0L
            createViewModel(
                remainingVirtualEnergyChannel = energy,
                hotkeyChannel = hotkey,
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true),
                currentTimeMs = { fakeTime },
            )

            energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
            fakeTime = 5_001L
            hotkey.send(Unit)

            assertEquals(emptyList(), spoken)
        }

    @Test
    fun `データ未受信のホットキーは何もしない`() =
        runTest(testDispatcher) {
            val hotkey = Channel<Unit>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            createViewModel(hotkeyChannel = hotkey, ttsEngine = mockTts(spoken))

            hotkey.send(Unit)

            assertEquals(emptyList(), spoken)
        }

    @Test
    fun `残量スイッチが無効ならホットキーでも読み上げない`() =
        runTest(testDispatcher) {
            val disabledKeys =
                listOf(
                    LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
                    LmuWindowsReadoutItemKey.RemainingVirtualEnergy.WarningReadout,
                )
            for (disabledKey in disabledKeys) {
                val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
                val hotkey = Channel<Unit>(Channel.UNLIMITED)
                val spoken = mutableListOf<SpeechEvent>()
                createViewModel(
                    remainingVirtualEnergyChannel = energy,
                    hotkeyChannel = hotkey,
                    ttsEngine = mockTts(spoken),
                    enabledOverrides = mapOf(disabledKey to false),
                )

                energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
                hotkey.send(Unit)

                assertEquals(emptyList(), spoken)
            }
        }

    @Test
    fun `他のシミュレーターではホットキーを無視する`() =
        runTest(testDispatcher) {
            val hotkey = Channel<Unit>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            createViewModel(
                hotkeyChannel = hotkey,
                ttsEngine = mockTts(spoken),
                simulator = Simulator.Gt7Ps5,
            )

            hotkey.send(Unit)

            assertEquals(emptyList(), spoken)
        }

    @Test
    fun `シミュレーター切り替え後は古い残量を読み上げない`() =
        runTest(testDispatcher) {
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val hotkey = Channel<Unit>(Channel.UNLIMITED)
            val selected = MutableStateFlow<Simulator>(Simulator.LmuWindows)
            val spoken = mutableListOf<SpeechEvent>()
            createViewModel(
                remainingVirtualEnergyChannel = energy,
                hotkeyChannel = hotkey,
                selectedSimulatorFlow = selected,
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true),
            )

            energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
            selected.value = Simulator.Gt7Ps5
            hotkey.send(Unit)
            selected.value = Simulator.LmuWindows
            hotkey.send(Unit)
            assertEquals(emptyList(), spoken)

            energy.send(remainingVirtualEnergy(remainingRatio = 0.7))
            hotkey.send(Unit)
            assertEquals(1, spoken.size)
        }

    @Test
    fun `ホットキーの読み上げは既存のキュー設定に従う`() =
        runTest(testDispatcher) {
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val hotkey = Channel<Unit>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val queued = mutableListOf<SpeechEvent>()
            val tts = mockTts(spoken)
            every { ttsEngine.speak(capture(queued), queue = true) } just Runs
            createViewModel(
                remainingVirtualEnergyChannel = energy,
                hotkeyChannel = hotkey,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true),
                queueEnabledOverrides = mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true),
            )

            energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
            hotkey.send(Unit)

            assertEquals(emptyList(), spoken)
            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsRemainingVirtualEnergyCurrentValue(
                        percentage = 80,
                        resolvedText = LmuWindowsRemainingVirtualEnergyCurrentValue(80).narratedText,
                    ),
                ),
                queued,
            )
        }

    @Test
    fun `手動依頼は最新残量を繰り返し読み上げ警告状態を変えない`() =
        runTest(testDispatcher) {
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val events = mutableListOf<SpeechEvent>()
            val logs = mutableListOf<TelemetryLog>()
            createViewModel(
                remainingVirtualEnergyChannel = energy,
                manualRequestChannel = requests,
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true),
                readoutTextOverride = {
                    events.add(it)
                    "現在値"
                },
                currentTimeMs = { 123L },
            )
            stubTelemetryLogSave(logs, 123L, LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root)

            energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "set-1"))
            energy.send(remainingVirtualEnergy(remainingRatio = 0.2))
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "set-1"))
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "set-1"))
            energy.send(remainingVirtualEnergy(remainingRatio = 0.2))

            assertEquals(
                listOf("バーチャルエネルギー残量は80パーセント", "バーチャルエネルギー残量は20パーセント", "バーチャルエネルギー残量は20パーセント"),
                events.filterIsInstance<LmuWindowsManualReadoutText>().map { it.narratedText },
            )
            assertEquals(4, events.size)
            assertEquals(4, spoken.size)
            assertEquals(4, logs.size)
            assertEquals(123L, logs.last().createdAt)
            assertContains(logs[1].telemetryJson, "\"remainingRatio\":0.2")
            assertContains(logs[1].telemetryJson, "\"finalState\":")
            assertFalse(logs.last().telemetryJson.contains("remainingRatio"))
            assertContains(logs.last().telemetryJson, "\"observedAtMs\":123")
        }

    @Test
    fun `存在しないセットの手動依頼とホットキーは読み上げずログも保存しない`() =
        runTest(testDispatcher) {
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val presses = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            createViewModel(
                manualRequestChannel = requests,
                manualHotkeyChannel = presses,
                manualSets = listOf(ManualReadoutSet("empty")),
                currentTimeMs = { 123L },
                ttsEngine = mockTts(spoken),
            )
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "missing"))
            presses.send(ManualReadoutRequest(Simulator.LmuWindows.id, "missing"))

            assertEquals(emptyList(), spoken)
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            coVerify(exactly = 0) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 123L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
                    narratedText = "読み上げる項目がありません",
                    narrationOutcome = NarrationOutcome.QUEUED,
                    telemetryJson = "",
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `項目なしセットのタブ依頼とホットキーはデータの有無に関係なく固定文言をキューへ追加してログを保存する`() =
        runTest(testDispatcher) {
            val key = LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root
            val expected = LmuWindowsManualReadoutText(key, "読み上げる項目がありません", "読み上げる項目がありません")
            val logs = mutableListOf<TelemetryLog>()
            stubTelemetryLogSave(logs, 5_001L, key)
            for (hasData in listOf(false, true)) {
                for (hotkey in listOf(false, true)) {
                    val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
                    val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
                    val presses = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
                    val spoken = mutableListOf<SpeechEvent>()
                    var fakeTime = 0L
                    createViewModel(
                        remainingVirtualEnergyChannel = energy,
                        manualRequestChannel = requests,
                        manualHotkeyChannel = presses,
                        manualSets =
                            listOf(
                                ManualReadoutSet(
                                    "set-1",
                                    emptyList(),
                                    mapOf(key.value to "編集された残量{percent}%"),
                                ),
                            ),
                        ttsEngine = mockTts(spoken),
                        queueEnabledOverrides = mapOf(key to false),
                        currentTimeMs = { fakeTime },
                    )
                    fakeTime = 5_001L
                    if (hasData) energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
                    val channel = if (hotkey) presses else requests
                    channel.send(ManualReadoutRequest(Simulator.LmuWindows.id, "set-1"))

                    assertEquals(listOf<SpeechEvent>(expected), spoken)
                    val log = logs.last()
                    assertEquals("読み上げる項目がありません", log.narratedText)
                    assertEquals(NarrationOutcome.QUEUED, log.narrationOutcome)
                    assertContains(log.telemetryJson, "\"state\":")
                    assertContains(log.telemetryJson, "\"settings\":")
                    assertContains(log.telemetryJson, "\"observedAtMs\":5001")
                    assertFalse(log.telemetryJson.contains("remainingRatio"))
                    assertFalse(log.telemetryJson.contains("debugReadout"))
                }
            }
            assertEquals(4, logs.size)
            verify(exactly = 4) { ttsEngine.speak(expected, queue = true) }
            coVerify(exactly = 4) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 5_001L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = key,
                    narratedText = "読み上げる項目がありません",
                    narrationOutcome = NarrationOutcome.QUEUED,
                    telemetryJson = withArg { json -> assertTrue(logs.any { it.telemetryJson == json }) },
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `未受信と鮮度切れのタブ依頼とセットホットキーはデータ無しをキューへ追加してログを保存する`() =
        runTest(testDispatcher) {
            val key = LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root
            val expected = LmuWindowsManualReadoutText(key, "データがありません", "データがありません")
            val logs = mutableListOf<TelemetryLog>()
            stubTelemetryLogSave(logs, 5_001L, key)
            for (stale in listOf(false, true)) {
                for (hotkey in listOf(false, true)) {
                    val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
                    val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
                    val presses = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
                    val spoken = mutableListOf<SpeechEvent>()
                    var fakeTime = 0L
                    createViewModel(
                        remainingVirtualEnergyChannel = energy,
                        manualRequestChannel = requests,
                        manualHotkeyChannel = presses,
                        ttsEngine = mockTts(spoken),
                        queueEnabledOverrides = mapOf(key to false),
                        currentTimeMs = { fakeTime },
                    )
                    if (stale) energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
                    fakeTime = 5_001L
                    val channel = if (hotkey) presses else requests
                    channel.send(ManualReadoutRequest(Simulator.LmuWindows.id, "set-1"))

                    assertEquals(listOf<SpeechEvent>(expected), spoken)
                    val log = logs.last()
                    assertEquals("データがありません", log.narratedText)
                    assertEquals(NarrationOutcome.QUEUED, log.narrationOutcome)
                    assertContains(log.telemetryJson, "\"state\":")
                    assertContains(log.telemetryJson, "\"settings\":")
                    assertContains(log.telemetryJson, "\"observedAtMs\":5001")
                    assertFalse(log.telemetryJson.contains("remainingRatio"))
                    assertFalse(log.telemetryJson.contains("debugReadout"))
                }
            }
            assertEquals(4, logs.size)
            verify(exactly = 4) { ttsEngine.speak(expected, queue = true) }
            coVerify(exactly = 4) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 5_001L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = key,
                    narratedText = "データがありません",
                    narrationOutcome = NarrationOutcome.QUEUED,
                    telemetryJson = withArg { json -> assertTrue(logs.any { it.telemetryJson == json }) },
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `残量スイッチが無効でも手動依頼は読み上げる`() =
        runTest(testDispatcher) {
            val disabledKeys =
                listOf(
                    LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
                    LmuWindowsReadoutItemKey.RemainingVirtualEnergy.WarningReadout,
                )
            for (disabledKey in disabledKeys) {
                val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
                val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
                val spoken = mutableListOf<SpeechEvent>()
                createViewModel(
                    remainingVirtualEnergyChannel = energy,
                    manualRequestChannel = requests,
                    ttsEngine = mockTts(spoken),
                    enabledOverrides = mapOf(disabledKey to false),
                )

                energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
                requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "set-1"))

                assertEquals(1, spoken.size)
            }

            verify(exactly = 2) {
                ttsEngine.speak(
                    LmuWindowsManualReadoutText(
                        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
                        "バーチャルエネルギー残量は80パーセント",
                        "バーチャルエネルギー残量は80パーセント",
                    ),
                    queue = true,
                )
            }
            confirmVerified(ttsEngine)
        }

    @Test
    fun `他のシミュレーターでは手動依頼を無視する`() =
        runTest(testDispatcher) {
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            createViewModel(
                manualRequestChannel = requests,
                ttsEngine = mockTts(spoken),
                simulator = Simulator.Gt7Ps5,
            )

            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "set-1"))

            assertEquals(emptyList(), spoken)
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) {
                ttsEngine.speak(
                    LmuWindowsManualReadoutText(
                        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
                        "バーチャルエネルギー残量は80パーセント",
                        "バーチャルエネルギー残量は80パーセント",
                    ),
                    queue = false,
                )
            }
            confirmVerified(ttsEngine)
        }

    @Test
    fun `手動依頼はシミュレーター切り替え後の古い残量を読み上げない`() =
        runTest(testDispatcher) {
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val selected = MutableStateFlow<Simulator>(Simulator.LmuWindows)
            val spoken = mutableListOf<SpeechEvent>()
            createViewModel(
                remainingVirtualEnergyChannel = energy,
                manualRequestChannel = requests,
                selectedSimulatorFlow = selected,
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true),
            )

            energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
            selected.update { Simulator.Gt7Ps5 }
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "set-1"))
            selected.update { Simulator.LmuWindows }
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "set-1"))
            assertEquals(listOf("データがありません"), spoken.map { it.narratedText })

            energy.send(remainingVirtualEnergy(remainingRatio = 0.7))
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "set-1"))
            assertEquals(2, spoken.size)
        }

    @Test
    fun `キュー設定が無効でも手動依頼はキューへ追加してログを保存する`() =
        runTest(testDispatcher) {
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val logs = mutableListOf<TelemetryLog>()
            val queued = mutableListOf<SpeechEvent>()
            val tts = mockTts(spoken)
            every { ttsEngine.speak(capture(queued), queue = true) } just Runs
            createViewModel(
                remainingVirtualEnergyChannel = energy,
                manualRequestChannel = requests,
                manualSets =
                    listOf(
                        ManualReadoutSet(
                            "set-1",
                            listOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value),
                            mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value to "残量{percent}%"),
                        ),
                    ),
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true),
                queueEnabledOverrides = mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to false),
                currentTimeMs = { 123L },
            )

            stubTelemetryLogSave(logs, 123L, LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root)

            energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "set-1"))

            assertEquals(emptyList(), spoken)
            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsManualReadoutText(
                        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
                        "残量80%",
                        resolvedText = "残量80%",
                    ),
                ),
                queued,
            )
            verify(exactly = 1) { ttsEngine.speak(queued.single(), queue = true) }
            val log = logs.single()
            assertEquals("残量80%", queued.single().narratedText)
            assertEquals("残量80%", log.narratedText)
            assertEquals(NarrationOutcome.QUEUED, log.narrationOutcome)
            assertContains(log.telemetryJson, "\"observedAtMs\":123")
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 123L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
                    narratedText = queued.single().narratedText,
                    narrationOutcome = NarrationOutcome.QUEUED,
                    telemetryJson = log.telemetryJson,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `自動読み上げとキュー設定が無効でも手動依頼は残りVE現在値をキューへ追加する`() =
        runTest(testDispatcher) {
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val logs = mutableListOf<TelemetryLog>()
            val queued = mutableListOf<SpeechEvent>()
            val tts = mockTts(spoken)
            every { ttsEngine.speak(capture(queued), queue = true) } just Runs
            createViewModel(
                remainingVirtualEnergyChannel = energy,
                manualRequestChannel = requests,
                manualSets =
                    listOf(
                        ManualReadoutSet(
                            "set-1",
                            listOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value),
                            mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value to "残量{percent}%"),
                        ),
                    ),
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to false),
                queueEnabledOverrides = mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to false),
                currentTimeMs = { 123L },
            )

            stubTelemetryLogSave(logs, 123L, LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root)

            energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "set-1"))

            assertEquals(emptyList(), spoken)
            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsManualReadoutText(
                        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
                        "残量80%",
                        resolvedText = "残量80%",
                    ),
                ),
                queued,
            )
            verify(exactly = 1) { ttsEngine.speak(queued.single(), queue = true) }
            val log = logs.single()
            assertEquals("残量80%", queued.single().narratedText)
            assertEquals("残量80%", log.narratedText)
            assertEquals(NarrationOutcome.QUEUED, log.narrationOutcome)
            assertContains(log.telemetryJson, "\"observedAtMs\":123")
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 123L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
                    narratedText = queued.single().narratedText,
                    narrationOutcome = NarrationOutcome.QUEUED,
                    telemetryJson = log.telemetryJson,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `キュー設定が無効でもセットホットキーはキューへ追加してログを保存する`() =
        runTest(testDispatcher) {
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val presses = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val logs = mutableListOf<TelemetryLog>()
            val queued = mutableListOf<SpeechEvent>()
            val tts = mockTts(spoken)
            every { ttsEngine.speak(capture(queued), queue = true) } just Runs
            createViewModel(
                remainingVirtualEnergyChannel = energy,
                manualHotkeyChannel = presses,
                manualSets =
                    listOf(
                        ManualReadoutSet(
                            "set-1",
                            listOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value),
                            mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value to "残量{percent}%"),
                        ),
                    ),
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true),
                queueEnabledOverrides = mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to false),
                currentTimeMs = { 123L },
            )

            stubTelemetryLogSave(logs, 123L, LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root)

            energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
            presses.send(ManualReadoutRequest(Simulator.LmuWindows.id, "set-1"))

            assertEquals(emptyList(), spoken)
            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsManualReadoutText(
                        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
                        "残量80%",
                        resolvedText = "残量80%",
                    ),
                ),
                queued,
            )
            verify(exactly = 1) { ttsEngine.speak(queued.single(), queue = true) }
            val log = logs.single()
            assertEquals("残量80%", queued.single().narratedText)
            assertEquals("残量80%", log.narratedText)
            assertEquals(NarrationOutcome.QUEUED, log.narrationOutcome)
            assertContains(log.telemetryJson, "\"observedAtMs\":123")
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 123L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
                    narratedText = queued.single().narratedText,
                    narrationOutcome = NarrationOutcome.QUEUED,
                    telemetryJson = log.telemetryJson,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `別シミュレーター宛ての手動依頼はLMU選択中でも読み上げない`() =
        runTest(testDispatcher) {
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            createViewModel(
                remainingVirtualEnergyChannel = energy,
                manualRequestChannel = requests,
                ttsEngine = mockTts(spoken),
            )
            energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
            requests.send(ManualReadoutRequest(Simulator.Gt7Ps5.id, "set-1"))
            requests.send(ManualReadoutRequest(Simulator.AceWindows.id, "set-2"))

            assertEquals(emptyList(), spoken)
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) {
                ttsEngine.speak(
                    LmuWindowsManualReadoutText(
                        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
                        "バーチャルエネルギー残量は80パーセント",
                        "バーチャルエネルギー残量は80パーセント",
                    ),
                    queue = false,
                )
            }
            confirmVerified(ttsEngine)
        }

    @Test
    fun `手動依頼は鮮度の上限までは残量を読み上げ上限を超えたらデータ無しを読み上げる`() =
        runTest(testDispatcher) {
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            var fakeTime = 0L
            createViewModel(
                remainingVirtualEnergyChannel = energy,
                manualRequestChannel = requests,
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true),
                currentTimeMs = { fakeTime },
            )
            energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
            fakeTime = 5_000L
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "set-1"))
            assertEquals(1, spoken.size)
            fakeTime = 5_001L
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "set-1"))
            assertEquals(2, spoken.size)
            assertEquals("データがありません", spoken.last().narratedText)
        }

    @Test
    fun `手動燃料は未受信と5秒超をデータ無しにし鮮度内は自動スイッチに関係なく読む`() =
        runTest(testDispatcher) {
            val fuel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val key = LmuWindowsReadoutItemKey.RemainingFuel.Root
            var time = 0L
            createViewModel(
                telemetryChannel = fuel,
                remainingVirtualEnergyChannel = energy,
                manualRequestChannel = requests,
                manualSets = listOf(ManualReadoutSet("fuel", listOf(key.value))),
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(key to false),
                queueEnabledOverrides = mapOf(key to false),
                currentTimeMs = { time },
            )
            val request = ManualReadoutRequest(Simulator.LmuWindows.id, "fuel")
            energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
            requests.send(request)
            fuel.send(telemetryWithFuel(currentLiters = 43.0, capacityLiters = 100.0))
            requests.send(request)
            time = 5_000L
            requests.send(request)
            time = 5_001L
            requests.send(request)
            assertEquals(
                listOf("データがありません", "燃料は残り43パーセント", "燃料は残り43パーセント", "データがありません"),
                spoken.map { it.narratedText },
            )
            assertEquals(listOf(key, key, key, key), spoken.map { it.readoutItemKey })
        }

    @Test
    fun `手動燃料は切り捨て前の割合を四捨五入して読む`() =
        runTest(testDispatcher) {
            val fuel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val key = LmuWindowsReadoutItemKey.RemainingFuel.Root
            createViewModel(
                telemetryChannel = fuel,
                manualRequestChannel = requests,
                manualSets = listOf(ManualReadoutSet("fuel", listOf(key.value))),
                ttsEngine = mockTts(spoken),
                currentTimeMs = { 0L },
            )
            fuel.send(telemetryWithFuel(currentLiters = 42.6, capacityLiters = 100.0))
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "fuel"))
            assertEquals(listOf("燃料は残り43パーセント"), spoken.map { it.narratedText })
        }

    @Test
    fun `手動燃料はシミュレーター切り替えで古い値を破棄する`() =
        runTest(testDispatcher) {
            val selected = MutableStateFlow<Simulator>(Simulator.LmuWindows)
            val fuel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val key = LmuWindowsReadoutItemKey.RemainingFuel.Root
            createViewModel(
                telemetryChannel = fuel,
                manualRequestChannel = requests,
                manualSets = listOf(ManualReadoutSet("fuel", listOf(key.value))),
                selectedSimulatorFlow = selected,
                enabledOverrides = mapOf(key to false),
                ttsEngine = mockTts(spoken),
            )
            fuel.send(telemetryWithFuel(currentLiters = 43.0, capacityLiters = 100.0))
            selected.update { Simulator.Gt7Ps5 }
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "fuel"))
            selected.update { Simulator.LmuWindows }
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "fuel"))
            assertEquals(listOf("データがありません"), spoken.map { it.narratedText })
        }

    @Test
    fun `手動燃料を読んでも自動警告済み状態を変えず補給後は再び警告する`() =
        runTest(testDispatcher) {
            val fuel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val key = LmuWindowsReadoutItemKey.RemainingFuel.Root
            createViewModel(
                telemetryChannel = fuel,
                manualRequestChannel = requests,
                manualSets = listOf(ManualReadoutSet("fuel", listOf(key.value))),
                vehicleClass = LmuWindowsVehicleClassData.P3,
                enabledOverrides = mapOf(key to true, LmuWindowsReadoutItemKey.RemainingFuel.WarningReadout to true),
                ttsEngine = mockTts(spoken),
            )
            val request = ManualReadoutRequest(Simulator.LmuWindows.id, "fuel")
            fuel.send(telemetryWithFuel(currentLiters = 10.0, capacityLiters = 100.0))
            requests.send(request)
            fuel.send(telemetryWithFuel(currentLiters = 9.0, capacityLiters = 100.0))
            assertEquals(
                listOf(resolvedFuelWarning.narratedText, "燃料は残り10パーセント"),
                spoken.map { it.narratedText },
            )
            fuel.send(telemetryWithFuel(currentLiters = 80.0, capacityLiters = 100.0))
            requests.send(request)
            fuel.send(telemetryWithFuel(currentLiters = 10.0, capacityLiters = 100.0))
            assertEquals(
                listOf(
                    resolvedFuelWarning.narratedText,
                    "燃料は残り10パーセント",
                    "燃料は残り80パーセント",
                    resolvedFuelWarning.narratedText,
                ),
                spoken.map { it.narratedText },
            )
        }

    @Test
    fun `手動温度を読んでも過熱警告済み状態と自動警告の前回温度を維持する`() =
        runTest(testDispatcher) {
            val temperature = Channel<LmuWindowsBrakeTemperatureData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val logs = mutableListOf<TelemetryLog>()
            val key = LmuWindowsReadoutItemKey.BrakeTemperature.Root
            createViewModel(
                brakeTemperatureChannel = temperature,
                manualRequestChannel = requests,
                manualSets = listOf(ManualReadoutSet("temperature", listOf(key.value))),
                enabledOverrides = mapOf(key to true),
                ttsEngine = mockTts(spoken),
            )
            stubTelemetryLogSave(logs, createdAt = 0L, key)
            temperature.send(brakeTemperature(fl = 850.0))
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "temperature"))
            temperature.send(brakeTemperature(fl = 900.0))
            assertEquals(listOf("ブレーキ温度800℃以上", "ブレーキ温度850℃"), spoken.map { it.narratedText })
            temperature.send(brakeTemperature(fl = 20.0))
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "temperature"))
            temperature.send(brakeTemperature(fl = 850.0))
            assertEquals(
                listOf("ブレーキ温度800℃以上", "ブレーキ温度850℃", "ブレーキ温度20℃", "ブレーキ温度800℃以上"),
                spoken.map { it.narratedText },
            )
            assertContains(logs.last().telemetryJson, "\"previousBrakeTemperature\":{\"wheels\":{\"FRONT_LEFT\":20.0")
        }

    @Test
    fun `手動自己ベストを読んでも自動の更新判定と記録は変わらない`() =
        runTest(testDispatcher) {
            val telemetry = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val key = LmuWindowsReadoutItemKey.MyBestLap.Root
            createViewModel(
                telemetryChannel = telemetry,
                manualRequestChannel = requests,
                manualSets = listOf(ManualReadoutSet("best", listOf(key.value))),
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(key to true),
            )
            val request = ManualReadoutRequest(Simulator.LmuWindows.id, "best")
            telemetry.send(fakeTelemetryData(bestLapTimeMs = 83_456L))
            requests.send(request)
            telemetry.send(fakeTelemetryData(bestLapTimeMs = 82_345L))
            requests.send(request)
            telemetry.send(fakeTelemetryData(bestLapTimeMs = 82_345L))
            assertEquals(
                listOf(
                    "自己ベストラップ 1分23秒456",
                    "自己ベストラップ更新 1分22秒345",
                    "自己ベストラップ 1分22秒345",
                ),
                spoken.map { it.narratedText },
            )
        }

    @Test
    fun `手動自己ベストは未受信とゼロ以下と5秒超をデータ無しにし自動スイッチを無視する`() =
        runTest(testDispatcher) {
            val telemetry = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val key = LmuWindowsReadoutItemKey.MyBestLap.Root
            var time = 0L
            createViewModel(
                telemetryChannel = telemetry,
                manualRequestChannel = requests,
                manualSets = listOf(ManualReadoutSet("best", listOf(key.value))),
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(key to false, LmuWindowsReadoutItemKey.MyBestLap.DetailEnabled to false),
                queueEnabledOverrides = mapOf(key to false),
                currentTimeMs = { time },
            )
            val request = ManualReadoutRequest(Simulator.LmuWindows.id, "best")
            requests.send(request)
            telemetry.send(fakeTelemetryData(bestLapTimeMs = 83_456L))
            time = 5_000L
            requests.send(request)
            time = 5_001L
            requests.send(request)
            telemetry.send(fakeTelemetryData(bestLapTimeMs = 0L))
            requests.send(request)
            telemetry.send(fakeTelemetryData(bestLapTimeMs = -1L))
            requests.send(request)
            telemetry.send(fakeTelemetryData(bestLapTimeMs = 82_345L))
            requests.send(request)
            assertEquals(
                listOf(
                    "データがありません",
                    "自己ベストラップ 1分23秒456",
                    "データがありません",
                    "データがありません",
                    "データがありません",
                    "自己ベストラップ 1分22秒345",
                ),
                spoken.map { it.narratedText },
            )
            assertEquals(List(6) { key }, spoken.map { it.readoutItemKey })
            verify(exactly = 4) {
                ttsEngine.speak(LmuWindowsManualReadoutText(key, "データがありません", "データがありません"), queue = true)
            }
            listOf("自己ベストラップ 1分23秒456", "自己ベストラップ 1分22秒345").forEach { text ->
                verify(exactly = 1) { ttsEngine.speak(LmuWindowsManualReadoutText(key, text, text), queue = true) }
            }
            confirmVerified(ttsEngine)
        }

    @Test
    fun `タイヤ温度のみの手動セットはVEを読まず未受信と空輪と鮮度切れをデータ無しにする`() =
        runTest(testDispatcher) {
            val temperature = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val key = LmuWindowsReadoutItemKey.TyreTemperature.Root
            var time = 0L
            createViewModel(
                tyreTemperatureChannel = temperature,
                manualRequestChannel = requests,
                manualSets = listOf(ManualReadoutSet("temperature", listOf(key.value))),
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(key to false),
                queueEnabledOverrides = mapOf(key to false),
                currentTimeMs = { time },
            )
            val request = ManualReadoutRequest(Simulator.LmuWindows.id, "temperature")
            requests.send(request)
            temperature.send(tyreTemperature(fl = 70.0, fr = 90.5, rl = 85.0, rr = 80.0))
            time = 5_000L
            requests.send(request)
            time = 5_001L
            requests.send(request)
            temperature.send(LmuWindowsTyreCarcassTemperatureData(emptyMap()))
            requests.send(request)
            assertEquals(
                listOf("データがありません", "タイヤ温度91℃", "データがありません", "データがありません"),
                spoken.map { it.narratedText },
            )
            assertEquals(listOf(key, key, key, key), spoken.map { it.readoutItemKey })
            verify(exactly = 3) {
                ttsEngine.speak(LmuWindowsManualReadoutText(key, "データがありません", "データがありません"), queue = true)
            }
            verify(exactly = 1) {
                ttsEngine.speak(LmuWindowsManualReadoutText(key, "タイヤ温度91℃", "タイヤ温度91℃"), queue = true)
            }
            confirmVerified(ttsEngine)
        }

    @Test
    fun `ブレーキ温度のみの手動セットはVEを読まず未受信と空輪と鮮度切れをデータ無しにする`() =
        runTest(testDispatcher) {
            val wear = Channel<LmuWindowsBrakeTemperatureData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val key = LmuWindowsReadoutItemKey.BrakeTemperature.Root
            var time = 0L
            createViewModel(
                brakeTemperatureChannel = wear,
                manualRequestChannel = requests,
                manualSets = listOf(ManualReadoutSet("brake", listOf(key.value))),
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(key to false),
                queueEnabledOverrides = mapOf(key to false),
                currentTimeMs = { time },
            )
            val request = ManualReadoutRequest(Simulator.LmuWindows.id, "brake")
            requests.send(request)
            wear.send(brakeTemperature(fl = 300.0, fr = 500.5, rl = 450.0, rr = 400.0))
            time = 5_000L
            requests.send(request)
            time = 5_001L
            requests.send(request)
            wear.send(LmuWindowsBrakeTemperatureData(emptyMap()))
            requests.send(request)
            assertEquals(
                listOf("データがありません", "ブレーキ温度501℃", "データがありません", "データがありません"),
                spoken.map { it.narratedText },
            )
            assertEquals(listOf(key, key, key, key), spoken.map { it.readoutItemKey })
            verify(exactly = 3) {
                ttsEngine.speak(LmuWindowsManualReadoutText(key, "データがありません", "データがありません"), queue = true)
            }
            verify(exactly = 1) {
                ttsEngine.speak(LmuWindowsManualReadoutText(key, "ブレーキ温度501℃", "ブレーキ温度501℃"), queue = true)
            }
            confirmVerified(ttsEngine)
        }

    @Test
    fun `手動自己ベストはシミュレーター切り替えで古い値を破棄する`() =
        runTest(testDispatcher) {
            val selected = MutableStateFlow<Simulator>(Simulator.LmuWindows)
            val telemetry = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val key = LmuWindowsReadoutItemKey.MyBestLap.Root
            createViewModel(
                telemetryChannel = telemetry,
                manualRequestChannel = requests,
                manualSets = listOf(ManualReadoutSet("best", listOf(key.value))),
                selectedSimulatorFlow = selected,
                enabledOverrides = mapOf(key to false),
                ttsEngine = mockTts(spoken),
            )
            telemetry.send(fakeTelemetryData(bestLapTimeMs = 83_456L))
            selected.update { Simulator.Gt7Ps5 }
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "best"))
            selected.update { Simulator.LmuWindows }
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "best"))
            assertEquals(listOf("データがありません"), spoken.map { it.narratedText })
        }

    @Test
    fun `手動タイヤ温度はシミュレーター切り替えで古い値を破棄する`() =
        runTest(testDispatcher) {
            val selected = MutableStateFlow<Simulator>(Simulator.LmuWindows)
            val temperature = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val key = LmuWindowsReadoutItemKey.TyreTemperature.Root
            createViewModel(
                tyreTemperatureChannel = temperature,
                manualRequestChannel = requests,
                manualSets = listOf(ManualReadoutSet("temperature", listOf(key.value))),
                selectedSimulatorFlow = selected,
                enabledOverrides = mapOf(key to false),
                ttsEngine = mockTts(spoken),
            )
            temperature.send(tyreTemperature(fl = 90.0))
            selected.update { Simulator.Gt7Ps5 }
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "temperature"))
            selected.update { Simulator.LmuWindows }
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "temperature"))
            assertEquals(listOf("データがありません"), spoken.map { it.narratedText })
        }

    @Test
    fun `手動タイヤ温度は非有限値を除外して最高値を読み有効値が無ければデータ無しにする`() =
        runTest(testDispatcher) {
            val temperature = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val key = LmuWindowsReadoutItemKey.TyreTemperature.Root
            createViewModel(
                tyreTemperatureChannel = temperature,
                manualRequestChannel = requests,
                manualSets = listOf(ManualReadoutSet("temperature", listOf(key.value))),
                enabledOverrides = mapOf(key to false),
                ttsEngine = mockTts(spoken),
            )
            val request = ManualReadoutRequest(Simulator.LmuWindows.id, "temperature")
            temperature.send(tyreTemperature(fl = Double.NaN, fr = 80.0, rl = Double.POSITIVE_INFINITY, rr = 70.0))
            requests.send(request)
            temperature.send(tyreTemperature(fl = Double.NaN, fr = Double.NaN, rl = Double.NaN, rr = Double.NaN))
            requests.send(request)
            assertEquals(listOf("タイヤ温度80℃", "データがありません"), spoken.map { it.narratedText })
        }

    @Test
    fun `手動ブレーキ温度は非有限値を除外して最高値を読み有効値が無ければデータ無しにする`() =
        runTest(testDispatcher) {
            val temperature = Channel<LmuWindowsBrakeTemperatureData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val key = LmuWindowsReadoutItemKey.BrakeTemperature.Root
            createViewModel(
                brakeTemperatureChannel = temperature,
                manualRequestChannel = requests,
                manualSets = listOf(ManualReadoutSet("brake", listOf(key.value))),
                enabledOverrides = mapOf(key to false),
                ttsEngine = mockTts(spoken),
            )
            val request = ManualReadoutRequest(Simulator.LmuWindows.id, "brake")
            temperature.send(brakeTemperature(fl = Double.NaN, fr = 400.0, rl = Double.POSITIVE_INFINITY, rr = 300.0))
            requests.send(request)
            temperature.send(brakeTemperature(fl = Double.NaN, fr = Double.NaN, rl = Double.NaN, rr = Double.NaN))
            requests.send(request)
            assertEquals(listOf("ブレーキ温度400℃", "データがありません"), spoken.map { it.narratedText })
        }

    @Test
    fun `手動ブレーキ温度はシミュレーター切り替えで古い値を破棄する`() =
        runTest(testDispatcher) {
            val selected = MutableStateFlow<Simulator>(Simulator.LmuWindows)
            val wear = Channel<LmuWindowsBrakeTemperatureData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val key = LmuWindowsReadoutItemKey.BrakeTemperature.Root
            createViewModel(
                brakeTemperatureChannel = wear,
                manualRequestChannel = requests,
                manualSets = listOf(ManualReadoutSet("brake", listOf(key.value))),
                selectedSimulatorFlow = selected,
                enabledOverrides = mapOf(key to false),
                ttsEngine = mockTts(spoken),
            )
            wear.send(brakeTemperature(fl = 500.0))
            selected.update { Simulator.Gt7Ps5 }
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "brake"))
            selected.update { Simulator.LmuWindows }
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "brake"))
            assertEquals(listOf("データがありません"), spoken.map { it.narratedText })
        }

    @Test
    fun `ブレーキのみの手動セットはVEを読まず未受信と空輪と鮮度切れをデータ無しにする`() =
        runTest(testDispatcher) {
            val wear = Channel<LmuWindowsBrakeWearData?>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val key = LmuWindowsReadoutItemKey.BrakeWear.Root
            var time = 0L
            createViewModel(
                brakeWearChannel = wear,
                manualRequestChannel = requests,
                manualSets = listOf(ManualReadoutSet("brake", listOf(key.value))),
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(key to false),
                queueEnabledOverrides = mapOf(key to false),
                currentTimeMs = { time },
            )
            val request = ManualReadoutRequest(Simulator.LmuWindows.id, "brake")
            requests.send(request)
            wear.send(brakeWearData(thickness = 0.035f))
            wear.send(brakeWearData(thickness = 0.03f))
            time = 5_000L
            requests.send(request)
            time = 5_001L
            requests.send(request)
            wear.send(LmuWindowsBrakeWearData(emptyMap()))
            requests.send(request)
            wear.send(null)
            requests.send(request)
            assertEquals(
                listOf("データがありません", "ブレーキ残量50%", "データがありません", "データがありません", "データがありません"),
                spoken.map { it.narratedText },
            )
            assertEquals(listOf(key, key, key, key, key), spoken.map { it.readoutItemKey })
        }

    @Test
    fun `手動ブレーキ残量はシミュレーター切り替えで古い値を破棄する`() =
        runTest(testDispatcher) {
            val selected = MutableStateFlow<Simulator>(Simulator.LmuWindows)
            val wear = Channel<LmuWindowsBrakeWearData?>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val key = LmuWindowsReadoutItemKey.BrakeWear.Root
            createViewModel(
                brakeWearChannel = wear,
                manualRequestChannel = requests,
                manualSets = listOf(ManualReadoutSet("brake", listOf(key.value))),
                selectedSimulatorFlow = selected,
                enabledOverrides = mapOf(key to false),
                ttsEngine = mockTts(spoken),
            )
            wear.send(brakeWearData(thickness = 0.036f))
            selected.update { Simulator.Gt7Ps5 }
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "brake"))
            selected.update { Simulator.LmuWindows }
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "brake"))
            assertEquals(listOf("データがありません"), spoken.map { it.narratedText })
        }

    @Test
    fun `タイヤのみの手動セットはVEを読まず未受信と空輪と鮮度切れをデータ無しにする`() =
        runTest(testDispatcher) {
            val wear = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val key = LmuWindowsReadoutItemKey.TyreWear.Root
            var time = 0L
            createViewModel(
                tyreWearChannel = wear,
                manualRequestChannel = requests,
                manualSets = listOf(ManualReadoutSet("tyre", listOf(key.value))),
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(key to false),
                queueEnabledOverrides = mapOf(key to false),
                currentTimeMs = { time },
            )
            val request = ManualReadoutRequest(Simulator.LmuWindows.id, "tyre")
            requests.send(request)
            wear.send(tyreWear(fl = 0.424))
            time = 5_000L
            requests.send(request)
            time = 5_001L
            requests.send(request)
            wear.send(LmuWindowsTyreWearData(emptyMap()))
            requests.send(request)
            assertEquals(
                listOf("データがありません", "タイヤ摩耗残り42%", "データがありません", "データがありません"),
                spoken.map { it.narratedText },
            )
            assertEquals(listOf(key, key, key, key), spoken.map { it.readoutItemKey })
        }

    @Test
    fun `手動タイヤ摩耗はシミュレーター切り替えで古い値を破棄する`() =
        runTest(testDispatcher) {
            val selected = MutableStateFlow<Simulator>(Simulator.LmuWindows)
            val wear = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val key = LmuWindowsReadoutItemKey.TyreWear.Root
            createViewModel(
                tyreWearChannel = wear,
                manualRequestChannel = requests,
                manualSets = listOf(ManualReadoutSet("tyre", listOf(key.value))),
                selectedSimulatorFlow = selected,
                enabledOverrides = mapOf(key to false),
                ttsEngine = mockTts(spoken),
            )
            wear.send(tyreWear(fl = 0.7))
            selected.update { Simulator.Gt7Ps5 }
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "tyre"))
            selected.update { Simulator.LmuWindows }
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "tyre"))
            assertEquals(listOf("データがありません"), spoken.map { it.narratedText })
        }

    @Test
    fun `手動セットは保存した項目順と文言で一イベントをキューへ追加する`() =
        runTest(testDispatcher) {
            val wear = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val tyreKey = LmuWindowsReadoutItemKey.TyreWear.Root
            val energyKey = LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root
            createViewModel(
                tyreWearChannel = wear,
                remainingVirtualEnergyChannel = energy,
                manualRequestChannel = requests,
                manualSets =
                    listOf(
                        ManualReadoutSet(
                            "ordered",
                            listOf(tyreKey.value, energyKey.value),
                            itemTexts = mapOf(tyreKey.value to "タイヤ{percent}%", energyKey.value to "VE{percent}%"),
                        ),
                    ),
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(tyreKey to false, energyKey to false),
            )
            wear.send(tyreWear(fl = 0.7))
            energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "ordered"))
            assertEquals(listOf(tyreKey), spoken.map { it.readoutItemKey })
            assertEquals(listOf("タイヤ70%、VE80%"), spoken.map { it.narratedText })
        }

    // --- ピットタイミング ---

    @Test
    fun `最速ラップの30秒前を過ぎて閾値以下になるとPitTimingWarningを読み上げる`() =
        runTest(testDispatcher) {
            val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val virtualEnergyChannel = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val tyreWearChannel = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            var currentTime = 0L
            createViewModel(
                telemetryChannel = telemetryChannel,
                remainingVirtualEnergyChannel = virtualEnergyChannel,
                tyreWearChannel = tyreWearChannel,
                ttsEngine = tts,
                pitTimingVirtualEnergyLapsThreshold = 3,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.PitTiming.Root to true),
                currentTimeMs = { currentTime },
            )

            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 1.0))
            tyreWearChannel.send(tyreWear())
            // 初回取得周を除外し、次の周回境界から計測する。
            telemetryChannel.send(fakeTelemetryData(currentLap = 0, bestLapTimeMs = 90_000L))
            telemetryChannel.send(fakeTelemetryData(currentLap = 1, bestLapTimeMs = 90_000L))
            currentTime = 45_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.9))
            currentTime = 90_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.8))
            telemetryChannel.send(fakeTelemetryData(currentLap = 2, bestLapTimeMs = 90_000L))
            currentTime = 150_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.05))

            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsPitTimingWarning(
                        0,
                        source = PitTimingSource.VirtualEnergy,
                        resolvedText = "エネルギー切れのため必ずピットイン",
                    ),
                ),
                spokenTexts,
            )
        }

    @Test
    fun `ピットタイミング項目が無効ならPitTimingWarningを読み上げない`() =
        runTest(testDispatcher) {
            val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val virtualEnergyChannel = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val tyreWearChannel = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            var currentTime = 0L
            createViewModel(
                telemetryChannel = telemetryChannel,
                remainingVirtualEnergyChannel = virtualEnergyChannel,
                tyreWearChannel = tyreWearChannel,
                ttsEngine = tts,
                pitTimingVirtualEnergyLapsThreshold = 3,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.PitTiming.Root to false),
                currentTimeMs = { currentTime },
            )

            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 1.0))
            tyreWearChannel.send(tyreWear())
            // 初回取得周を除外し、次の周回境界から計測する。
            telemetryChannel.send(fakeTelemetryData(currentLap = 0, bestLapTimeMs = 90_000L))
            telemetryChannel.send(fakeTelemetryData(currentLap = 1, bestLapTimeMs = 90_000L))
            currentTime = 45_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.9))
            currentTime = 90_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.8))
            telemetryChannel.send(fakeTelemetryData(currentLap = 2, bestLapTimeMs = 90_000L))
            currentTime = 150_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.05))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `バーチャルエネルギーのピットタイミングが無効ならRoot有効でもPitTimingWarningを読み上げない`() =
        runTest(testDispatcher) {
            val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val virtualEnergyChannel = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val tyreWearChannel = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            var currentTime = 0L
            createViewModel(
                telemetryChannel = telemetryChannel,
                remainingVirtualEnergyChannel = virtualEnergyChannel,
                tyreWearChannel = tyreWearChannel,
                ttsEngine = tts,
                pitTimingVirtualEnergyLapsThreshold = 3,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.PitTiming.Root to true),
                pitTimingEnabledOverrides = mapOf(LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy to false),
                currentTimeMs = { currentTime },
            )

            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 1.0))
            tyreWearChannel.send(tyreWear())
            // 初回取得周を除外し、次の周回境界から計測する。
            telemetryChannel.send(fakeTelemetryData(currentLap = 0, bestLapTimeMs = 90_000L))
            telemetryChannel.send(fakeTelemetryData(currentLap = 1, bestLapTimeMs = 90_000L))
            currentTime = 45_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.9))
            currentTime = 90_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.8))
            telemetryChannel.send(fakeTelemetryData(currentLap = 2, bestLapTimeMs = 90_000L))
            currentTime = 150_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.05))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `タイヤ摩耗のピットタイミングが無効ならRoot有効でもPitTimingWarningを読み上げない`() =
        runTest(testDispatcher) {
            val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val virtualEnergyChannel = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val tyreWearChannel = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            var currentTime = 0L
            createViewModel(
                telemetryChannel = telemetryChannel,
                remainingVirtualEnergyChannel = virtualEnergyChannel,
                tyreWearChannel = tyreWearChannel,
                ttsEngine = tts,
                pitTimingTyreWearLapsThreshold = 3,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.PitTiming.Root to true),
                pitTimingEnabledOverrides = mapOf(LmuWindowsReadoutItemKey.PitTiming.TyreWear to false),
                currentTimeMs = { currentTime },
            )

            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 1.0))
            tyreWearChannel.send(tyreWear(fl = 1.0))
            // 初回取得周を除外し、次の周回境界から計測する。
            telemetryChannel.send(fakeTelemetryData(currentLap = 0, bestLapTimeMs = 90_000L))
            telemetryChannel.send(fakeTelemetryData(currentLap = 1, bestLapTimeMs = 90_000L))
            currentTime = 45_000L
            tyreWearChannel.send(tyreWear(fl = 0.9))
            currentTime = 90_000L
            tyreWearChannel.send(tyreWear(fl = 0.8))
            telemetryChannel.send(fakeTelemetryData(currentLap = 2, bestLapTimeMs = 90_000L))
            currentTime = 150_000L
            tyreWearChannel.send(tyreWear(fl = 0.05))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `最も摩耗した車輪を基準に閾値以下になるとPitTimingWarningを読み上げる`() =
        runTest(testDispatcher) {
            val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val virtualEnergyChannel = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val tyreWearChannel = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val logs = mutableListOf<TelemetryLog>()
            val tts = mockTts(spokenTexts)
            var currentTime = 0L
            createViewModel(
                telemetryChannel = telemetryChannel,
                remainingVirtualEnergyChannel = virtualEnergyChannel,
                tyreWearChannel = tyreWearChannel,
                ttsEngine = tts,
                pitTimingTyreWearLapsThreshold = 3,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.PitTiming.Root to true),
                currentTimeMs = { currentTime },
            )

            stubTelemetryLogSave(logs, createdAt = 150_000L, LmuWindowsReadoutItemKey.PitTiming.Root)

            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 1.0))
            tyreWearChannel.send(tyreWear(fl = 1.0))
            // 初回取得周を除外し、次の周回境界から計測する。
            telemetryChannel.send(fakeTelemetryData(currentLap = 0, bestLapTimeMs = 90_000L))
            telemetryChannel.send(fakeTelemetryData(currentLap = 1, bestLapTimeMs = 90_000L))
            currentTime = 45_000L
            tyreWearChannel.send(tyreWear(fl = 0.9))
            currentTime = 90_000L
            tyreWearChannel.send(tyreWear(fl = 0.8))
            telemetryChannel.send(fakeTelemetryData(currentLap = 2, bestLapTimeMs = 90_000L))
            currentTime = 150_000L
            tyreWearChannel.send(tyreWear(fl = 0.05))

            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsPitTimingWarning(
                        0,
                        source = PitTimingSource.TyreWear,
                        resolvedText = "タイヤ交換へ",
                    ),
                ),
                spokenTexts,
            )
            assertEquals("タイヤ交換へ", logs.single().narratedText)
        }

    @Test
    fun `給油後はピットタイミングのバーチャルエネルギー予想残り周回数を再度読み上げる`() =
        runTest(testDispatcher) {
            val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val virtualEnergyChannel = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val tyreWearChannel = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val logs = mutableListOf<TelemetryLog>()
            val tts = mockTts(spokenTexts)
            var currentTime = 0L
            createViewModel(
                telemetryChannel = telemetryChannel,
                remainingVirtualEnergyChannel = virtualEnergyChannel,
                tyreWearChannel = tyreWearChannel,
                ttsEngine = tts,
                pitTimingVirtualEnergyLapsThreshold = 3,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.PitTiming.Root to true),
                currentTimeMs = { currentTime },
            )
            stubTelemetryLogSave(logs, createdAt = 150_000L, LmuWindowsReadoutItemKey.PitTiming.Root)
            stubTelemetryLogSave(logs, createdAt = 330_000L, LmuWindowsReadoutItemKey.PitTiming.Root)

            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 1.0))
            tyreWearChannel.send(tyreWear())
            // 初回取得周を除外し、次の周回境界から計測する。
            telemetryChannel.send(fakeTelemetryData(currentLap = 0, bestLapTimeMs = 90_000L))
            telemetryChannel.send(fakeTelemetryData(currentLap = 1, bestLapTimeMs = 90_000L))
            currentTime = 45_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.9))
            currentTime = 90_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.8))
            telemetryChannel.send(fakeTelemetryData(currentLap = 2, bestLapTimeMs = 90_000L))
            currentTime = 150_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.05))
            currentTime = 155_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 1.0))
            currentTime = 180_000L
            telemetryChannel.send(fakeTelemetryData(currentLap = 3, bestLapTimeMs = 90_000L))
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.9))
            currentTime = 225_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.8))
            currentTime = 270_000L
            telemetryChannel.send(fakeTelemetryData(currentLap = 4, bestLapTimeMs = 90_000L))
            currentTime = 330_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.05))

            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsPitTimingWarning(
                        0,
                        source = PitTimingSource.VirtualEnergy,
                        resolvedText = "エネルギー切れのため必ずピットイン",
                    ),
                    LmuWindowsPitTimingWarning(
                        0,
                        source = PitTimingSource.VirtualEnergy,
                        resolvedText = "エネルギー切れのため必ずピットイン",
                    ),
                ),
                spokenTexts,
            )
            assertEquals(2, logs.size)
        }

    @Test
    fun `タイヤ交換後はピットタイミングのタイヤ摩耗予想残り周回数を再度読み上げる`() =
        runTest(testDispatcher) {
            val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val virtualEnergyChannel = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val tyreWearChannel = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val logs = mutableListOf<TelemetryLog>()
            val tts = mockTts(spokenTexts)
            var currentTime = 0L
            createViewModel(
                telemetryChannel = telemetryChannel,
                remainingVirtualEnergyChannel = virtualEnergyChannel,
                tyreWearChannel = tyreWearChannel,
                ttsEngine = tts,
                pitTimingTyreWearLapsThreshold = 3,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.PitTiming.Root to true),
                currentTimeMs = { currentTime },
            )
            stubTelemetryLogSave(logs, createdAt = 150_000L, LmuWindowsReadoutItemKey.PitTiming.Root)
            stubTelemetryLogSave(logs, createdAt = 330_000L, LmuWindowsReadoutItemKey.PitTiming.Root)

            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 1.0))
            tyreWearChannel.send(tyreWear(fl = 1.0))
            // 初回取得周を除外し、次の周回境界から計測する。
            telemetryChannel.send(fakeTelemetryData(currentLap = 0, bestLapTimeMs = 90_000L))
            telemetryChannel.send(fakeTelemetryData(currentLap = 1, bestLapTimeMs = 90_000L))
            currentTime = 45_000L
            tyreWearChannel.send(tyreWear(fl = 0.9))
            currentTime = 90_000L
            tyreWearChannel.send(tyreWear(fl = 0.8))
            telemetryChannel.send(fakeTelemetryData(currentLap = 2, bestLapTimeMs = 90_000L))
            currentTime = 150_000L
            tyreWearChannel.send(tyreWear(fl = 0.05))
            currentTime = 155_000L
            tyreWearChannel.send(tyreWear(fl = 1.0))
            currentTime = 180_000L
            telemetryChannel.send(fakeTelemetryData(currentLap = 3, bestLapTimeMs = 90_000L))
            tyreWearChannel.send(tyreWear(fl = 0.9))
            currentTime = 225_000L
            tyreWearChannel.send(tyreWear(fl = 0.8))
            currentTime = 270_000L
            telemetryChannel.send(fakeTelemetryData(currentLap = 4, bestLapTimeMs = 90_000L))
            currentTime = 330_000L
            tyreWearChannel.send(tyreWear(fl = 0.05))

            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsPitTimingWarning(
                        0,
                        source = PitTimingSource.TyreWear,
                        resolvedText = "タイヤ交換へ",
                    ),
                    LmuWindowsPitTimingWarning(
                        0,
                        source = PitTimingSource.TyreWear,
                        resolvedText = "タイヤ交換へ",
                    ),
                ),
                spokenTexts,
            )
            assertEquals(2, logs.size)
        }

    @Test
    fun `バーチャルエネルギーとタイヤ摩耗が同時に閾値以下になった場合は予想残り周回数が低い方のみ読み上げる`() =
        runTest(testDispatcher) {
            val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val virtualEnergyChannel = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val tyreWearChannel = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            var currentTime = 0L
            createViewModel(
                telemetryChannel = telemetryChannel,
                remainingVirtualEnergyChannel = virtualEnergyChannel,
                tyreWearChannel = tyreWearChannel,
                ttsEngine = tts,
                pitTimingVirtualEnergyLapsThreshold = 3,
                pitTimingTyreWearLapsThreshold = 3,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.PitTiming.Root to true),
                currentTimeMs = { currentTime },
            )

            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 1.0))
            tyreWearChannel.send(tyreWear(fl = 1.0))
            // 初回取得周を除外し、次の周回境界から計測する。
            telemetryChannel.send(fakeTelemetryData(currentLap = 0, bestLapTimeMs = 90_000L))
            telemetryChannel.send(fakeTelemetryData(currentLap = 1, bestLapTimeMs = 90_000L))
            currentTime = 45_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.9))
            tyreWearChannel.send(tyreWear(fl = 0.9))
            currentTime = 90_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.8))
            tyreWearChannel.send(tyreWear(fl = 0.8))
            telemetryChannel.send(fakeTelemetryData(currentLap = 2, bestLapTimeMs = 90_000L))
            currentTime = 150_000L
            // 消費率(0.2/ラップ)基準で バーチャルエネルギー予想残り0周、タイヤ摩耗予想残り1周となるようにする
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.05))
            tyreWearChannel.send(tyreWear(fl = 0.25))

            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsPitTimingWarning(
                        0,
                        source = PitTimingSource.VirtualEnergy,
                        resolvedText = "エネルギー切れのため必ずピットイン",
                    ),
                ),
                spokenTexts,
            )
        }

    @Test
    fun `タイヤ摩耗の予想残り周回数が低い場合は算出元を維持して読み上げる`() =
        runTest(testDispatcher) {
            val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val virtualEnergyChannel = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val tyreWearChannel = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            var currentTime = 0L
            createViewModel(
                telemetryChannel = telemetryChannel,
                remainingVirtualEnergyChannel = virtualEnergyChannel,
                tyreWearChannel = tyreWearChannel,
                ttsEngine = tts,
                pitTimingVirtualEnergyLapsThreshold = 3,
                pitTimingTyreWearLapsThreshold = 3,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.PitTiming.Root to true),
                currentTimeMs = { currentTime },
            )

            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 1.0))
            tyreWearChannel.send(tyreWear(fl = 1.0))
            // 初回取得周を除外し、次の周回境界から計測する。
            telemetryChannel.send(fakeTelemetryData(currentLap = 0, bestLapTimeMs = 90_000L))
            telemetryChannel.send(fakeTelemetryData(currentLap = 1, bestLapTimeMs = 90_000L))
            currentTime = 45_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.9))
            tyreWearChannel.send(tyreWear(fl = 0.9))
            currentTime = 90_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.8))
            tyreWearChannel.send(tyreWear(fl = 0.8))
            telemetryChannel.send(fakeTelemetryData(currentLap = 2, bestLapTimeMs = 90_000L))
            currentTime = 150_000L
            // タイヤ摩耗予想残り0周を先に通知し、VE予想残り1周は同一ラップのゲートで抑止する。
            tyreWearChannel.send(tyreWear(fl = 0.05))
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.25))

            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsPitTimingWarning(
                        0,
                        source = PitTimingSource.TyreWear,
                        resolvedText = "タイヤ交換へ",
                    ),
                ),
                spokenTexts,
            )
        }

    @Test
    fun `両方のピットタイミング警告が同時に発火しタイヤ摩耗の残り周回数が低い場合はタイヤ摩耗のみ読み上げる`() =
        runTest(testDispatcher) {
            val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val virtualEnergyChannel = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val tyreWearChannel = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            var currentTime = 0L
            createViewModel(
                telemetryChannel = telemetryChannel,
                remainingVirtualEnergyChannel = virtualEnergyChannel,
                tyreWearChannel = tyreWearChannel,
                ttsEngine = tts,
                pitTimingVirtualEnergyLapsThreshold = 3,
                pitTimingTyreWearLapsThreshold = 3,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.PitTiming.Root to true),
                currentTimeMs = { currentTime },
            )

            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 1.0))
            tyreWearChannel.send(tyreWear(fl = 1.0))
            // 初回取得周を除外し、次の周回境界から計測する。
            telemetryChannel.send(fakeTelemetryData(currentLap = 0, bestLapTimeMs = 90_000L))
            telemetryChannel.send(fakeTelemetryData(currentLap = 1, bestLapTimeMs = 90_000L))
            currentTime = 45_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.9))
            tyreWearChannel.send(tyreWear(fl = 0.9))
            currentTime = 90_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.8))
            tyreWearChannel.send(tyreWear(fl = 0.8))
            telemetryChannel.send(fakeTelemetryData(currentLap = 2, bestLapTimeMs = 90_000L))
            // 読み上げ可能時刻の直前にVE残り1周、タイヤ摩耗残り0周の入力を揃える。
            currentTime = 149_999L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.25))
            tyreWearChannel.send(tyreWear(fl = 0.05))
            assertEquals(emptyList(), spokenTexts)

            currentTime = 150_000L
            telemetryChannel.send(
                fakeTelemetryData(currentLap = 2, bestLapTimeMs = 90_000L).copy(timestampMs = currentTime),
            )

            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsPitTimingWarning(
                        0,
                        source = PitTimingSource.TyreWear,
                        resolvedText = "タイヤ交換へ",
                    ),
                ),
                spokenTexts,
            )
        }

    @Test
    fun `バーチャルエネルギーとタイヤ摩耗が同時に閾値以下になり残り周回数が同じ場合はバーチャルエネルギーを読み上げる`() =
        runTest(testDispatcher) {
            val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val virtualEnergyChannel = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val tyreWearChannel = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            var currentTime = 0L
            createViewModel(
                telemetryChannel = telemetryChannel,
                remainingVirtualEnergyChannel = virtualEnergyChannel,
                tyreWearChannel = tyreWearChannel,
                ttsEngine = tts,
                pitTimingVirtualEnergyLapsThreshold = 3,
                pitTimingTyreWearLapsThreshold = 3,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.PitTiming.Root to true),
                currentTimeMs = { currentTime },
            )

            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 1.0))
            tyreWearChannel.send(tyreWear(fl = 1.0))
            // 初回取得周を除外し、次の周回境界から計測する。
            telemetryChannel.send(fakeTelemetryData(currentLap = 0, bestLapTimeMs = 90_000L))
            telemetryChannel.send(fakeTelemetryData(currentLap = 1, bestLapTimeMs = 90_000L))
            currentTime = 45_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.9))
            tyreWearChannel.send(tyreWear(fl = 0.9))
            currentTime = 90_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.8))
            tyreWearChannel.send(tyreWear(fl = 0.8))
            telemetryChannel.send(fakeTelemetryData(currentLap = 2, bestLapTimeMs = 90_000L))
            // 読み上げ可能時刻の直前に両方の予想残り周回数を0周に揃える。
            currentTime = 149_999L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.05))
            tyreWearChannel.send(tyreWear(fl = 0.05))
            assertEquals(emptyList(), spokenTexts)

            // 同じtickで両方の警告を発火させ、同点時のVE優先を検証する。
            currentTime = 150_000L
            telemetryChannel.send(
                fakeTelemetryData(currentLap = 2, bestLapTimeMs = 90_000L).copy(timestampMs = currentTime),
            )

            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsPitTimingWarning(
                        0,
                        source = PitTimingSource.VirtualEnergy,
                        resolvedText = "エネルギー切れのため必ずピットイン",
                    ),
                ),
                spokenTexts,
            )
        }

    @Test
    fun `同一周でゲートに抑止された警告は翌周に読み上げ通過済みの同じ残り周回数は再度読み上げない`() =
        runTest(testDispatcher) {
            val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val virtualEnergyChannel = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val tyreWearChannel = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            var currentTime = 0L
            createViewModel(
                telemetryChannel = telemetryChannel,
                remainingVirtualEnergyChannel = virtualEnergyChannel,
                tyreWearChannel = tyreWearChannel,
                ttsEngine = tts,
                pitTimingVirtualEnergyLapsThreshold = 3,
                pitTimingTyreWearLapsThreshold = 3,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.PitTiming.Root to true),
                currentTimeMs = { currentTime },
            )

            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 1.0))
            tyreWearChannel.send(tyreWear(fl = 1.0))
            // 初回取得周を除外し、次の周回境界から計測する。
            telemetryChannel.send(fakeTelemetryData(currentLap = 0, bestLapTimeMs = 90_000L))
            telemetryChannel.send(fakeTelemetryData(currentLap = 1, bestLapTimeMs = 90_000L))
            currentTime = 45_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.9))
            tyreWearChannel.send(tyreWear(fl = 0.9))
            currentTime = 90_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.8))
            tyreWearChannel.send(tyreWear(fl = 0.8))
            telemetryChannel.send(fakeTelemetryData(currentLap = 2, bestLapTimeMs = 90_000L))
            currentTime = 150_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.05))
            tyreWearChannel.send(tyreWear(fl = 0.05))

            assertEquals(
                listOf<SpeechEvent>(
                    LmuWindowsPitTimingWarning(
                        0,
                        source = PitTimingSource.VirtualEnergy,
                        resolvedText = "エネルギー切れのため必ずピットイン",
                    ),
                ),
                spokenTexts,
            )

            // 同じ残り0周のタイヤ摩耗警告はラップ2のゲートで抑止されたが、履歴には記録されない。
            currentTime = 180_000L
            telemetryChannel.send(fakeTelemetryData(currentLap = 3, bestLapTimeMs = 90_000L))
            currentTime = 240_000L
            telemetryChannel.send(fakeTelemetryData(currentLap = 3, bestLapTimeMs = 90_000L))

            val expectedEvents =
                listOf<SpeechEvent>(
                    LmuWindowsPitTimingWarning(
                        0,
                        source = PitTimingSource.VirtualEnergy,
                        resolvedText = "エネルギー切れのため必ずピットイン",
                    ),
                    LmuWindowsPitTimingWarning(
                        0,
                        source = PitTimingSource.TyreWear,
                        resolvedText = "タイヤ交換へ",
                    ),
                )
            assertEquals(expectedEvents, spokenTexts)

            // 両ソースとも通過済みなので、次の周でも残り0周なら再度読み上げない。
            currentTime = 270_000L
            telemetryChannel.send(fakeTelemetryData(currentLap = 4, bestLapTimeMs = 90_000L))
            currentTime = 330_000L
            telemetryChannel.send(fakeTelemetryData(currentLap = 4, bestLapTimeMs = 90_000L))

            assertEquals(expectedEvents, spokenTexts)
        }

    @Test
    fun `ピットタイミングの読み上げでテレメトリログを保存する`() =
        runTest(testDispatcher) {
            val telemetryChannel = Channel<LmuWindowsTelemetryData>(Channel.UNLIMITED)
            val virtualEnergyChannel = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val tyreWearChannel = Channel<LmuWindowsTyreWearData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val logs = mutableListOf<TelemetryLog>()
            val tts = mockTts(spokenTexts)
            var currentTime = 0L
            createViewModel(
                telemetryChannel = telemetryChannel,
                remainingVirtualEnergyChannel = virtualEnergyChannel,
                tyreWearChannel = tyreWearChannel,
                ttsEngine = tts,
                pitTimingVirtualEnergyLapsThreshold = 3,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.PitTiming.Root to true),
                currentTimeMs = { currentTime },
            )
            stubTelemetryLogSave(logs, createdAt = 150_000L, LmuWindowsReadoutItemKey.PitTiming.Root)

            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 1.0))
            tyreWearChannel.send(tyreWear())
            // 初回取得周を除外し、次の周回境界から計測する。
            telemetryChannel.send(fakeTelemetryData(currentLap = 0, bestLapTimeMs = 90_000L))
            telemetryChannel.send(fakeTelemetryData(currentLap = 1, bestLapTimeMs = 90_000L))
            currentTime = 45_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.9))
            currentTime = 90_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.8))
            telemetryChannel.send(fakeTelemetryData(currentLap = 2, bestLapTimeMs = 90_000L))
            currentTime = 150_000L
            virtualEnergyChannel.send(remainingVirtualEnergy(remainingRatio = 0.05))

            assertEquals(1, logs.size)
            val log = logs.first()
            assertEquals(150_000L, log.createdAt)
            assertEquals(Simulator.LmuWindows, log.simulator)
            assertEquals(LmuWindowsReadoutItemKey.PitTiming.Root, log.readoutItemKey)
            assertContains(log.telemetryJson, """"state":{""")
            assertContains(log.telemetryJson, """"telemetry":{"currentLapTimeMs":0""")
            assertContains(log.telemetryJson, """"virtualEnergy":{"remainingRatio":0.05""")
            assertContains(log.telemetryJson, """"tyreWear":{"wheels":{"FRONT_LEFT":1.0""")
            assertContains(log.telemetryJson, """"settings":{""")
            assertContains(log.telemetryJson, """"observedAtMs":150000""")
            assertContains(log.telemetryJson, """"finalState":{""")
        }

    // --- タイヤ低温警告 ---

    @Test
    fun `ガレージに遷移した瞬間に低温タイヤがあるとTyreColdを読み上げる`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreTemperatureChannel = channel,
                flagChannel = flagChannel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.TyreTemperature.Root to true),
                tyreTemperatureLowWarningPhasesOverride = mapOf(SessionPhase.GARAGE to true),
            )
            flagChannel.send(clearFlags(gamePhase = SessionPhase.GREEN_FLAG))
            channel.send(tyreTemperature(fl = 55.0))

            flagChannel.send(clearFlags(gamePhase = SessionPhase.GARAGE))

            assertEquals(listOf<SpeechEvent>(LmuWindowsTyreCold(55, "タイヤを温めて")), spokenTexts)
        }

    @Test
    fun `gamePhaseが変化しなければ低温でも読み上げない`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreTemperatureChannel = channel,
                flagChannel = flagChannel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.TyreTemperature.Root to true),
                tyreTemperatureLowWarningPhasesOverride = mapOf(SessionPhase.GARAGE to true),
            )
            flagChannel.send(clearFlags(gamePhase = SessionPhase.GARAGE))
            channel.send(tyreTemperature(fl = 55.0))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `低温警告スイッチがOFFのときは読み上げない`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreTemperatureChannel = channel,
                flagChannel = flagChannel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.TyreTemperature.Root to true),
                tyreTemperatureLowWarningEnabled = false,
                tyreTemperatureLowWarningPhasesOverride = mapOf(SessionPhase.GARAGE to true),
            )
            flagChannel.send(clearFlags(gamePhase = SessionPhase.GREEN_FLAG))
            channel.send(tyreTemperature(fl = 55.0))

            flagChannel.send(clearFlags(gamePhase = SessionPhase.GARAGE))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `detailPaneで選択解除したgamePhaseに遷移しても読み上げない`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreTemperatureChannel = channel,
                flagChannel = flagChannel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.TyreTemperature.Root to true),
                tyreTemperatureLowWarningPhasesOverride = mapOf(SessionPhase.GARAGE to false),
            )
            flagChannel.send(clearFlags(gamePhase = SessionPhase.GREEN_FLAG))
            channel.send(tyreTemperature(fl = 55.0))

            flagChannel.send(clearFlags(gamePhase = SessionPhase.GARAGE))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `低温警告読み上げが発生したらテレメトリを保存する`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val logs = mutableListOf<TelemetryLog>()
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreTemperatureChannel = channel,
                flagChannel = flagChannel,
                ttsEngine = tts,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.TyreTemperature.Root to true),
                currentTimeMs = { 123L },
                tyreTemperatureLowWarningPhasesOverride = mapOf(SessionPhase.GARAGE to true),
            )
            stubTelemetryLogSave(logs, createdAt = 123L, LmuWindowsReadoutItemKey.TyreTemperature.Root)
            flagChannel.send(clearFlags(gamePhase = SessionPhase.GREEN_FLAG))
            channel.send(tyreTemperature(fl = 55.0))

            flagChannel.send(clearFlags(gamePhase = SessionPhase.GARAGE))

            assertEquals(1, logs.size)
            val log = logs.first()
            assertEquals(123L, log.createdAt)
            assertEquals(Simulator.LmuWindows, log.simulator)
            assertEquals(LmuWindowsReadoutItemKey.TyreTemperature.Root, log.readoutItemKey)
            assertContains(log.telemetryJson, """"state":{""")
            assertContains(
                log.telemetryJson,
                """"input":{"tyreCarcassTemperature":{"wheels":{"FRONT_LEFT":55.0,"FRONT_RIGHT":20.0,""" +
                    """"REAR_LEFT":20.0,"REAR_RIGHT":20.0}},"raceFlags":{""",
            )
            assertContains(log.telemetryJson, """"settings":{""")
            assertContains(log.telemetryJson, """"observedAtMs":123""")
            assertContains(log.telemetryJson, """"overheatState":{""")
            assertContains(log.telemetryJson, """"finalState":{""")
        }

    @Test
    fun `低温文言が空白またはTTS不可なら読み上げずSKIPPEDを保存する`() =
        runTest(testDispatcher) {
            val channel = Channel<LmuWindowsTyreCarcassTemperatureData>(Channel.UNLIMITED)
            val flagChannel = Channel<LmuWindowsRaceFlagsData>(Channel.UNLIMITED)
            val logs = mutableListOf<TelemetryLog>()
            val spokenTexts = mutableListOf<SpeechEvent>()
            val tts = mockTts(spokenTexts)
            createViewModel(
                tyreTemperatureChannel = channel,
                flagChannel = flagChannel,
                ttsEngine = tts,
                tyreColdReadoutText = null,
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.TyreTemperature.Root to true),
                currentTimeMs = { 123L },
                tyreTemperatureLowWarningPhasesOverride = mapOf(SessionPhase.GARAGE to true),
            )
            stubTelemetryLogSave(logs, createdAt = 123L, LmuWindowsReadoutItemKey.TyreTemperature.Root)
            flagChannel.send(clearFlags(gamePhase = SessionPhase.GREEN_FLAG))
            channel.send(tyreTemperature(fl = 55.0))

            flagChannel.send(clearFlags(gamePhase = SessionPhase.GARAGE))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
            assertEquals(1, logs.size)
            val log = logs.first()
            assertEquals("", log.narratedText)
            assertEquals(NarrationOutcome.SKIPPED, log.narrationOutcome)
            assertEquals(123L, log.createdAt)
            assertEquals(Simulator.LmuWindows, log.simulator)
            assertEquals(LmuWindowsReadoutItemKey.TyreTemperature.Root, log.readoutItemKey)
            assertContains(log.telemetryJson, """"state":{""")
            assertContains(
                log.telemetryJson,
                """"input":{"tyreCarcassTemperature":{"wheels":{"FRONT_LEFT":55.0,"FRONT_RIGHT":20.0,""" +
                    """"REAR_LEFT":20.0,"REAR_RIGHT":20.0}},"raceFlags":{""",
            )
            assertContains(log.telemetryJson, """"settings":{""")
            assertContains(log.telemetryJson, """"observedAtMs":123""")
            assertContains(log.telemetryJson, """"overheatState":{""")
            assertContains(log.telemetryJson, """"finalState":{""")
            verify(exactly = 0) { tts.speak(LmuWindowsTyreCold(55, "タイヤを温めて"), queue = false) }
            verify(exactly = 0) { tts.currentReadoutItemKey }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 123L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = LmuWindowsReadoutItemKey.TyreTemperature.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = log.telemetryJson,
                )
            }
            confirmVerified(tts, telemetryLogRepository)
        }

    @Test
    fun `デバッグ要求は未受信かつ項目無効でも読み上げ他シミュレーターと空白は読み上げない`() =
        runTest(testDispatcher) {
            val key = ReadoutItemKey.topLevelEntriesOf(Simulator.LmuWindows).first()
            val request = DebugReadoutRequest(Simulator.LmuWindows, key, "入力 {laps}")
            val spokenJson = slot<String>()
            val skippedJson = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(DebugReadoutText(key, request.text), false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    123L,
                    Simulator.LmuWindows,
                    key,
                    request.text,
                    NarrationOutcome.SPOKEN,
                    capture(spokenJson),
                )
            } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    123L,
                    Simulator.LmuWindows,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    capture(skippedJson),
                )
            } just Runs
            val viewModel =
                createViewModel(
                    ttsEngine = ttsEngine,
                    enabledOverrides = mapOf(key to false),
                    currentTimeMs = { 123L },
                )
            try {
                Simulator.entries.filter { it != Simulator.LmuWindows }.forEach { simulator ->
                    debugRequests.emit(request.copy(simulator = simulator))
                }
                debugRequests.emit(request)
                debugRequests.emit(request.copy(text = " \n\t "))
                val json = Json.parseToJsonElement(spokenJson.captured).jsonObject
                assertEquals("true", json.getValue("debugReadout").jsonPrimitive.content)
                assertEquals("123", json.getValue("observedAtMs").jsonPrimitive.content)
                assertEquals(true, json.containsKey("state"))
                assertEquals(true, json.containsKey("settings"))
                assertEquals(setOf("state", "settings", "observedAtMs", "debugReadout"), json.keys)
                assertEquals(spokenJson.captured, skippedJson.captured)
                verify(exactly = 1) { debugRequestRepository.observeRequests() }
                verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
                verify(exactly = 1) { ttsEngine.speak(DebugReadoutText(key, request.text), false) }
                verify(exactly = 0) { ttsEngine.speak(DebugReadoutText(key, " \n\t "), false) }
                coVerify(exactly = 1) {
                    telemetryLogRepository.saveTelemetryLog(
                        123L,
                        Simulator.LmuWindows,
                        key,
                        request.text,
                        NarrationOutcome.SPOKEN,
                        spokenJson.captured,
                    )
                }
                coVerify(exactly = 1) {
                    telemetryLogRepository.saveTelemetryLog(
                        123L,
                        Simulator.LmuWindows,
                        key,
                        "",
                        NarrationOutcome.SKIPPED,
                        skippedJson.captured,
                    )
                }
                confirmVerified(debugRequestRepository, ttsEngine, telemetryLogRepository)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `手動依頼はセットごとの文言を使い空白文言は読み上げずF9は従来文言を使う`() =
        runTest(testDispatcher) {
            val key = LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val requests = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val hotkey = Channel<Unit>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val logs = mutableListOf<TelemetryLog>()
            createViewModel(
                remainingVirtualEnergyChannel = energy,
                manualRequestChannel = requests,
                hotkeyChannel = hotkey,
                manualSets =
                    listOf(
                        ManualReadoutSet("custom", listOf(key), mapOf(key to "残り{percent}%、{unknown}")),
                        ManualReadoutSet("default", listOf(key)),
                        ManualReadoutSet("empty", listOf(key), mapOf(key to "")),
                        ManualReadoutSet("blank", listOf(key), mapOf(key to "  ")),
                    ),
                ttsEngine = mockTts(spoken),
                enabledOverrides = mapOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true),
                currentTimeMs = { 123L },
            )
            stubTelemetryLogSave(logs, 123L, LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root)
            energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "custom"))
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "default"))
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "empty"))
            requests.send(ManualReadoutRequest(Simulator.LmuWindows.id, "blank"))
            hotkey.send(Unit)
            assertEquals(
                listOf("残り80%、{unknown}", "バーチャルエネルギー残量は80パーセント", "バーチャルエネルギー残量は80パーセント"),
                spoken.map { it.narratedText },
            )
            assertEquals(5, logs.size)
            assertEquals(
                listOf("残り80%、{unknown}", "バーチャルエネルギー残量は80パーセント", "", "", "バーチャルエネルギー残量は80パーセント"),
                logs.map { it.narratedText },
            )
            assertEquals(
                listOf(NarrationOutcome.SKIPPED, NarrationOutcome.SKIPPED),
                logs.slice(2..3).map { it.narrationOutcome },
            )
        }

    @Test
    fun `セットホットキーはLMUのセットだけ読み上げる`() =
        runTest(testDispatcher) {
            val energy = Channel<LmuWindowsVirtualEnergyData>(Channel.UNLIMITED)
            val presses = Channel<ManualReadoutRequest>(Channel.UNLIMITED)
            val spoken = mutableListOf<SpeechEvent>()
            val events = mutableListOf<SpeechEvent>()
            val logs = mutableListOf<TelemetryLog>()
            createViewModel(
                remainingVirtualEnergyChannel = energy,
                manualHotkeyChannel = presses,
                ttsEngine = mockTts(spoken),
                readoutTextOverride = {
                    events.add(it)
                    "現在値"
                },
                currentTimeMs = { 123L },
            )
            stubTelemetryLogSave(logs, 123L, LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root)
            energy.send(remainingVirtualEnergy(remainingRatio = 0.8))
            presses.send(ManualReadoutRequest(Simulator.Gt7Ps5.id, "set-1"))
            presses.send(ManualReadoutRequest(Simulator.AceWindows.id, "set-1"))
            testScheduler.runCurrent()
            assertEquals(emptyList(), spoken)
            presses.send(ManualReadoutRequest(Simulator.LmuWindows.id, "set-1"))
            testScheduler.runCurrent()
            assertEquals(
                "バーチャルエネルギー残量は80パーセント",
                events.filterIsInstance<LmuWindowsManualReadoutText>().single().narratedText,
            )
            assertEquals(1, spoken.size)
            assertEquals(1, logs.size)
        }
}

private fun noVehicleApproach() =
    LmuWindowsVehicleApproachData(
        sideBySideLeftVehicleIds = emptySet(),
        sideBySideRightVehicleIds = emptySet(),
        lateralDistanceLeftMeters = LateralDistanceMeters(Double.MAX_VALUE),
        lateralDistanceRightMeters = LateralDistanceMeters(Double.MAX_VALUE),
    )

private fun leftVehicleApproach(vehicleId: Int) =
    LmuWindowsVehicleApproachData(
        sideBySideLeftVehicleIds = setOf(vehicleId),
        sideBySideRightVehicleIds = emptySet(),
        lateralDistanceLeftMeters = LateralDistanceMeters(3.0),
        lateralDistanceRightMeters = LateralDistanceMeters(Double.MAX_VALUE),
    )

private fun clearFlags(
    gamePhase: SessionPhase = SessionPhase.GREEN_FLAG,
    playerFlag: PrimaryFlag = PrimaryFlag.GREEN,
    sectorFlags: List<SectorFlagState> =
        listOf(
            SectorFlagState.CLEAR,
            SectorFlagState.CLEAR,
            SectorFlagState.CLEAR,
        ),
) = LmuWindowsRaceFlagsData(
    gamePhase = gamePhase,
    yellowFlagState = SessionYellowFlagState.NONE,
    sectorFlags = sectorFlags,
    playerFlag = playerFlag,
    playerUnderYellow = false,
)

private fun noDamage(
    overheating: Boolean = false,
    partDetached: Boolean = false,
) = LmuWindowsVehicleDamageData(
    overheating = overheating,
    partDetached = partDetached,
    lastImpactMagnitude = 0.0,
)

private fun noTyreDetached(vararg detachedWheels: WheelIndex) =
    LmuWindowsTyreDetachedData(
        wheels = WheelIndex.entries.associateWith { it in detachedWheels },
    )

private fun fakeTelemetryData(
    currentLap: Int = 0,
    bestLapTimeMs: Long = 0L,
    fuelCurrentLiters: Double = 0.0,
    fuelCapacityLiters: Double = 0.0,
) = LmuWindowsTelemetryData(
    timestampMs = 0L,
    engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
    inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
    tyres = LmuWindowsTyreData(wheels = emptyMap()),
    fuel =
        LmuWindowsFuelData(
            currentLiters = LmuWindowsFuelUnit(fuelCurrentLiters),
            capacityLiters = LmuWindowsFuelUnit(fuelCapacityLiters),
        ),
    timing =
        LmuWindowsTimingData(
            currentLapTimeMs = 0L,
            lastLapTimeMs = 0L,
            bestLapTimeMs = bestLapTimeMs,
            sector1Ms = 0L,
            sector1And2Ms = 0L,
            currentLap = currentLap,
            maxLaps = 0,
        ),
    vehicle =
        LmuWindowsVehicleData(
            localVelocityX = 0.0,
            localVelocityY = 0.0,
            localVelocityZ = 0.0,
            positionX = 0.0,
            positionY = 0.0,
            positionZ = 0.0,
        ),
)

private fun telemetryWithFuel(
    currentLiters: Double,
    capacityLiters: Double,
) = fakeTelemetryData(fuelCurrentLiters = currentLiters, fuelCapacityLiters = capacityLiters)

private fun tyreTemperature(
    fl: Double = 20.0,
    fr: Double = 20.0,
    rl: Double = 20.0,
    rr: Double = 20.0,
) = LmuWindowsTyreCarcassTemperatureData(
    wheels =
        mapOf(
            WheelIndex.FRONT_LEFT to CelsiusReading(fl.toFloat()),
            WheelIndex.FRONT_RIGHT to CelsiusReading(fr.toFloat()),
            WheelIndex.REAR_LEFT to CelsiusReading(rl.toFloat()),
            WheelIndex.REAR_RIGHT to CelsiusReading(rr.toFloat()),
        ),
)

private fun tyreWear(
    fl: Double = 1.0,
    fr: Double = 1.0,
    rl: Double = 1.0,
    rr: Double = 1.0,
) = LmuWindowsTyreWearData(
    wheels =
        mapOf(
            WheelIndex.FRONT_LEFT to LmuWindowsTyreWearRatio(fl),
            WheelIndex.FRONT_RIGHT to LmuWindowsTyreWearRatio(fr),
            WheelIndex.REAR_LEFT to LmuWindowsTyreWearRatio(rl),
            WheelIndex.REAR_RIGHT to LmuWindowsTyreWearRatio(rr),
        ),
)

private fun brakeWearData(thickness: Float) =
    LmuWindowsBrakeWearData(
        wheels = WheelIndex.entries.associateWith { BrakeThicknessMeters(thickness) },
    )

private fun brakeTemperature(
    fl: Double = 20.0,
    fr: Double = 20.0,
    rl: Double = 20.0,
    rr: Double = 20.0,
) = LmuWindowsBrakeTemperatureData(
    wheels =
        mapOf(
            WheelIndex.FRONT_LEFT to CelsiusReading(fl.toFloat()),
            WheelIndex.FRONT_RIGHT to CelsiusReading(fr.toFloat()),
            WheelIndex.REAR_LEFT to CelsiusReading(rl.toFloat()),
            WheelIndex.REAR_RIGHT to CelsiusReading(rr.toFloat()),
        ),
)

private fun remainingVirtualEnergy(
    remainingRatio: Double = 1.0,
    session: Int = 0,
) = LmuWindowsVirtualEnergyData(
    remainingRatio = LmuWindowsVirtualEnergyRatio(remainingRatio),
    session = session,
)
