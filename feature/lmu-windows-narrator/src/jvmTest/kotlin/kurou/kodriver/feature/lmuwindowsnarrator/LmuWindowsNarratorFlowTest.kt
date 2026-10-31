package kurou.kodriver.feature.lmuwindowsnarrator

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_CLASS_UNKNOWN_KEY
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsEnergySource
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsEngineData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsFuelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsFuelUnit
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsInputsData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRemainingFuelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTimingData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyRatio
import kurou.kodriver.domain.lmuwindows.model.PrimaryFlag
import kurou.kodriver.domain.lmuwindows.model.SessionPhase
import kurou.kodriver.domain.lmuwindows.model.SessionYellowFlagState
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsNarratorFlowTest {
    @Test
    fun `テレメトリは収集時だけ購読しLMU選択解除と収集終了でキャンセルする`() =
        runTest {
            val selected = MutableStateFlow<Simulator>(Simulator.Gt7Ps5)
            val source = MutableSharedFlow<Int>()
            var subscriptions = 0
            var cancellations = 0
            val values = mutableListOf<Int>()
            val telemetry =
                LmuWindowsNarratorFlow(selected).telemetry {
                    flow {
                        subscriptions++
                        try {
                            emitAll(source)
                        } finally {
                            cancellations++
                        }
                    }
                }
            assertEquals(0, subscriptions)
            val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { telemetry.collect(values::add) }
            runCurrent()
            assertEquals(0, subscriptions)
            selected.update { Simulator.LmuWindows }
            runCurrent()
            source.emit(1)
            runCurrent()
            selected.update { Simulator.AceWindows }
            runCurrent()
            source.emit(2)
            selected.update { Simulator.LmuWindows }
            runCurrent()
            source.emit(3)
            runCurrent()
            assertEquals(listOf(1, 3), values)
            assertEquals(2, subscriptions)
            assertEquals(1, cancellations)
            job.cancel()
            runCurrent()
            assertEquals(2, cancellations)
        }

    @Test
    fun `有効状態は後の詳細設定で上書きし選択先と各設定の更新を反映する`() =
        runTest {
            val selected = MutableStateFlow<Simulator>(Simulator.LmuWindows)
            val key = LmuWindowsReadoutItemKey.Flag.Root
            val lmu = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(mapOf(key to false))
            val other =
                MutableStateFlow<Map<ReadoutItemKey, Boolean>>(
                    mapOf(LmuWindowsReadoutItemKey.MyBestLap.Root to true),
                )
            val details = List(5) { MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap()) }
            val states = mutableListOf<Map<ReadoutItemKey, Boolean>>()
            val merged =
                LmuWindowsNarratorFlow(selected).mergedEnabledStates(
                    { if (it == Simulator.LmuWindows.id) lmu else other },
                    details[0],
                    details[1],
                    details[2],
                    details[3],
                    details[4],
                )
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { merged.collect(states::add) }
            runCurrent()
            assertEquals(mapOf<ReadoutItemKey, Boolean>(key to false), states.last())
            details.forEachIndexed { index, detail ->
                detail.update { mapOf(key to (index % 2 == 0)) }
                runCurrent()
                assertEquals(index % 2 == 0, states.last()[key])
            }
            selected.update { Simulator.Gt7Ps5 }
            runCurrent()
            lmu.update { emptyMap() }
            runCurrent()
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    key to true,
                    LmuWindowsReadoutItemKey.MyBestLap.Root to true,
                ),
                states.last(),
            )
            details.last().update { mapOf(key to false) }
            runCurrent()
            assertEquals(false, states.last()[key])
        }

    @Test
    fun `優先順は選択先の設定へ切り替えて古い設定の更新を受けない`() =
        runTest {
            val selected = MutableStateFlow<Simulator>(Simulator.LmuWindows)
            val lmu = MutableStateFlow<List<ReadoutItemKey>>(listOf(LmuWindowsReadoutItemKey.Flag.Root))
            val other = MutableStateFlow<List<ReadoutItemKey>>(emptyList())
            val orders = mutableListOf<List<ReadoutItemKey>>()
            val order =
                LmuWindowsNarratorFlow(selected).autoReadoutOrder {
                    if (it == Simulator.LmuWindows) {
                        lmu
                    } else {
                        other
                    }
                }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { order.collect(orders::add) }
            runCurrent()
            assertEquals(lmu.first(), orders.last())
            selected.update { Simulator.Gt7Ps5 }
            runCurrent()
            lmu.update { listOf(LmuWindowsReadoutItemKey.TyreWear.Root) }
            runCurrent()
            assertEquals(emptyList(), orders.last())
            other.update { listOf(LmuWindowsReadoutItemKey.MyBestLap.Root) }
            runCurrent()
            assertEquals(other.first(), orders.last())
        }

    @Test
    fun `クラスと閾値設定の更新および未知クラスと未設定の既定値を反映する`() =
        runTest {
            val vehicleClass = MutableStateFlow<LmuWindowsVehicleClassData>(LmuWindowsVehicleClassData.Hypercar)
            val unknown = LmuWindowsVehicleClassData.Unknown(LMU_WINDOWS_VEHICLE_CLASS_UNKNOWN_KEY)
            val tyre =
                MutableStateFlow(
                    mapOf<LmuWindowsVehicleClassData, Celsius>(
                        LmuWindowsVehicleClassData.Hypercar to Celsius(110),
                        unknown to Celsius(95),
                    ),
                )
            val brake =
                MutableStateFlow(
                    mapOf<LmuWindowsVehicleClassData, Int>(LmuWindowsVehicleClassData.Hypercar to 900, unknown to 850),
                )
            val wear =
                MutableStateFlow(
                    mapOf<LmuWindowsVehicleClassData, Int>(
                        LmuWindowsVehicleClassData.Hypercar to 30,
                        unknown to 25,
                    ),
                )
            val narrator = LmuWindowsNarratorFlow(flowOf(Simulator.LmuWindows))
            val tyres = mutableListOf<Celsius>()
            val brakes = mutableListOf<Celsius>()
            val wears = mutableListOf<Int>()
            val sources = mutableListOf<LmuWindowsEnergySource>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                narrator.tyreHighThreshold(vehicleClass, tyre).collect(tyres::add)
            }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                narrator.brakeHighThreshold(vehicleClass, brake).collect(brakes::add)
            }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                narrator.brakeLowThreshold(vehicleClass, wear).collect(wears::add)
            }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                narrator.energySource(vehicleClass).collect(sources::add)
            }
            runCurrent()
            assertEquals(
                listOf(Celsius(110), Celsius(900), 30, LmuWindowsEnergySource.VirtualEnergy),
                listOf(tyres.last(), brakes.last(), wears.last(), sources.last()),
            )
            vehicleClass.update { LmuWindowsVehicleClassData.Unknown("raw class") }
            runCurrent()
            assertEquals(
                listOf(Celsius(95), Celsius(850), 25, LmuWindowsEnergySource.Fuel),
                listOf(tyres.last(), brakes.last(), wears.last(), sources.last()),
            )
            tyre.update { emptyMap() }
            brake.update { emptyMap() }
            wear.update { emptyMap() }
            runCurrent()
            assertEquals(listOf(Celsius(90), Celsius(800), 20), listOf(tyres.last(), brakes.last(), wears.last()))
            vehicleClass.update { LmuWindowsVehicleClassData.Hypercar }
            runCurrent()
            assertEquals(Celsius(100), tyres.last())
        }

    @Test
    fun `タイヤ温度は両入力を待ちフラッグだけの更新でも最新温度と合成する`() =
        runTest {
            val temperature = MutableSharedFlow<LmuWindowsTyreCarcassTemperatureData>()
            val flags = MutableSharedFlow<LmuWindowsRaceFlagsData>()
            val values = mutableListOf<Pair<LmuWindowsTyreCarcassTemperatureData, LmuWindowsRaceFlagsData>>()
            val narrator = LmuWindowsNarratorFlow(flowOf(Simulator.LmuWindows))
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                narrator.tyreTemperature(temperature, flags).collect(values::add)
            }
            val currentTemperature = LmuWindowsTyreCarcassTemperatureData(emptyMap())
            val green =
                LmuWindowsRaceFlagsData(
                    SessionPhase.GREEN_FLAG,
                    SessionYellowFlagState.NONE,
                    emptyList(),
                    PrimaryFlag.GREEN,
                    false,
                )
            temperature.emit(currentTemperature)
            runCurrent()
            assertEquals(emptyList(), values)
            flags.emit(green)
            runCurrent()
            val garage = green.copy(gamePhase = SessionPhase.GARAGE)
            flags.emit(garage)
            runCurrent()
            assertEquals(listOf(currentTemperature to green, currentTemperature to garage), values)
        }

    @Test
    fun `ピット入力はテレメトリを待ち未受信ソースをnullにして各更新を合成する`() =
        runTest {
            val telemetry = MutableSharedFlow<LmuWindowsTelemetryData>()
            val energy = MutableSharedFlow<LmuWindowsVirtualEnergyData>()
            val wear = MutableSharedFlow<LmuWindowsTyreWearData>()
            val fuel = MutableSharedFlow<LmuWindowsRemainingFuelData>()
            val values = mutableListOf<LmuWindowsPitTimingSnapshot>()
            val narrator = LmuWindowsNarratorFlow(flowOf(Simulator.LmuWindows))
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                narrator.pitTiming(telemetry, energy, wear, fuel).collect(values::add)
            }
            runCurrent()
            assertEquals(emptyList(), values)
            val first = fakeTelemetryData(currentLap = 2)
            telemetry.emit(first)
            runCurrent()
            assertEquals(LmuWindowsPitTimingSnapshot(first, null, null, null), values.last())
            val remainingFuel = LmuWindowsRemainingFuelData(40, receivedAtMs = 123L)
            fuel.emit(remainingFuel)
            runCurrent()
            assertEquals(LmuWindowsPitTimingSnapshot(first, null, null, remainingFuel), values.last())
            val virtualEnergy = LmuWindowsVirtualEnergyData(LmuWindowsVirtualEnergyRatio(0.8), session = 0)
            val tyreWear = LmuWindowsTyreWearData(emptyMap())
            energy.emit(virtualEnergy)
            wear.emit(tyreWear)
            runCurrent()
            val next = fakeTelemetryData(currentLap = 3)
            telemetry.emit(next)
            runCurrent()
            assertEquals(LmuWindowsPitTimingSnapshot(next, virtualEnergy, tyreWear, remainingFuel), values.last())
            assertEquals(3, narrator.currentLap(flowOf(next)).first())
        }
}

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
