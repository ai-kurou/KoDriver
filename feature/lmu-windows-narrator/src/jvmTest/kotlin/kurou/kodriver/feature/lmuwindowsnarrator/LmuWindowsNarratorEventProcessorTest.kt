@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.lmuwindowsnarrator

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kurou.kodriver.core.model.CountLapFlag
import kurou.kodriver.core.model.LmuWindowsEngineData
import kurou.kodriver.core.model.LmuWindowsFuelData
import kurou.kodriver.core.model.LmuWindowsInputsData
import kurou.kodriver.core.model.LmuWindowsRaceFlagsData
import kurou.kodriver.core.model.LmuWindowsTelemetryData
import kurou.kodriver.core.model.LmuWindowsTimingData
import kurou.kodriver.core.model.LmuWindowsTyreData
import kurou.kodriver.core.model.LmuWindowsTyreWearData
import kurou.kodriver.core.model.LmuWindowsVehicleApproachData
import kurou.kodriver.core.model.LmuWindowsVehicleData
import kurou.kodriver.core.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.core.model.MyBestLapVoiceType
import kurou.kodriver.core.model.PrimaryFlag
import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.core.model.RedFlagVoiceType
import kurou.kodriver.core.model.SessionPhase
import kurou.kodriver.core.model.SessionYellowFlagState
import kurou.kodriver.core.model.Simulator
import kurou.kodriver.core.model.VehicleApproachStartReadoutType
import kurou.kodriver.core.model.VehicleApproachSustainedReadoutType
import kurou.kodriver.core.model.WheelIndex
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.LmuWindowsNarratorReadoutSettings
import kurou.kodriver.domain.usecase.LmuWindowsNarratorState
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class LmuWindowsNarratorEventProcessorTest {
    @MockK
    private lateinit var telemetryLogRepository: TelemetryLogRepository

    @MockK
    private lateinit var ttsEngine: TextToSpeechEngine

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `読み上げたイベントを直前と現在の接近データとともに保存する`() =
        runTest {
            val telemetryJsonSlot = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(SpeechEvent.CarLeft, queue = false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    telemetryJson = capture(telemetryJsonSlot),
                )
            } just Runs
            val processor = createProcessor()

            processor.processVehicleApproach(
                vehicleApproach = leftVehicleApproach(distance = 4.0),
                events = emptyList(),
                readoutOrder = emptyList(),
                queueEnabledStates = emptyMap(),
                observedAtMs = 100L,
                logContext = logContext(),
            )
            processor.processVehicleApproach(
                vehicleApproach = leftVehicleApproach(distance = 3.0),
                events = listOf(SpeechEvent.CarLeft),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleApproach.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )

            val telemetryJson = telemetryJsonSlot.captured
            assertContains(telemetryJson, """"previousVehicleApproach":{"sideBySideLeftVehicleIds":[1]""")
            assertContains(telemetryJson, """"lateralDistanceLeftMeters":4.0""")
            assertContains(telemetryJson, """"vehicleApproach":{"sideBySideLeftVehicleIds":[1]""")
            assertContains(telemetryJson, """"lateralDistanceLeftMeters":3.0""")
            assertContains(telemetryJson, """"observedAtMs":200""")
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.CarLeft, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    telemetryJson = telemetryJson,
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `読み上げたタイヤ摩耗イベントを直前と現在のタイヤ摩耗データとともに保存する`() =
        runTest {
            val telemetryJsonSlot = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(SpeechEvent.TyreWearWarning, queue = false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.TyreWear.Root,
                    telemetryJson = capture(telemetryJsonSlot),
                )
            } just Runs
            val processor = createProcessor()

            processor.processTyreWear(
                tyreWear = tyreWear(frontLeft = 0.9),
                events = emptyList(),
                readoutOrder = emptyList(),
                queueEnabledStates = emptyMap(),
                observedAtMs = 100L,
                logContext = logContext(),
            )
            processor.processTyreWear(
                tyreWear = tyreWear(frontLeft = 0.4),
                events = listOf(SpeechEvent.TyreWearWarning),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.TyreWear.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )

            val telemetryJson = telemetryJsonSlot.captured
            val root = Json.parseToJsonElement(telemetryJson).jsonObject
            assertWheelValues(
                root["previousTyreWear"]!!.jsonObject["wheels"]!!.jsonObject,
                frontLeft = 0.9,
                frontRight = 0.9,
                rearLeft = 0.9,
                rearRight = 0.9,
            )
            assertWheelValues(
                root["tyreWear"]!!.jsonObject["wheels"]!!.jsonObject,
                frontLeft = 0.4,
                frontRight = 0.9,
                rearLeft = 0.9,
                rearRight = 0.9,
            )
            assertContains(telemetryJson, """"observedAtMs":200""")
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.TyreWearWarning, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.TyreWear.Root,
                    telemetryJson = telemetryJson,
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `読み上げたバーチャルエナジー残量イベントを直前と現在の残量データとともに保存する`() =
        runTest {
            val telemetryJsonSlot = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(SpeechEvent.RemainingVirtualEnergyWarning, queue = false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root,
                    telemetryJson = capture(telemetryJsonSlot),
                )
            } just Runs
            val processor = createProcessor()

            processor.processRemainingVirtualEnergy(
                remainingVirtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = 0.8),
                events = emptyList(),
                readoutOrder = emptyList(),
                queueEnabledStates = emptyMap(),
                observedAtMs = 100L,
                logContext = logContext(),
            )
            processor.processRemainingVirtualEnergy(
                remainingVirtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = 0.3),
                events = listOf(SpeechEvent.RemainingVirtualEnergyWarning),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )

            val telemetryJson = telemetryJsonSlot.captured
            val root = Json.parseToJsonElement(telemetryJson).jsonObject
            assertEquals(
                0.8,
                root["previousRemainingVirtualEnergy"]!!.jsonObject["remainingRatio"]!!.jsonPrimitive.double,
            )
            assertEquals(0.3, root["remainingVirtualEnergy"]!!.jsonObject["remainingRatio"]!!.jsonPrimitive.double)
            assertEquals(200L, root["observedAtMs"]!!.jsonPrimitive.long)
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.RemainingVirtualEnergyWarning, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root,
                    telemetryJson = telemetryJson,
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `読み上げたフラグイベントを直前と現在のフラグデータとともに保存する`() =
        runTest {
            val telemetryJsonSlot = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(SpeechEvent.BlueFlag, queue = false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
                    telemetryJson = capture(telemetryJsonSlot),
                )
            } just Runs
            val processor = createProcessor()

            processor.processRaceFlags(
                raceFlags = raceFlags(playerFlag = PrimaryFlag.GREEN),
                events = emptyList(),
                readoutOrder = emptyList(),
                queueEnabledStates = emptyMap(),
                observedAtMs = 100L,
                logContext = logContext(),
            )
            processor.processRaceFlags(
                raceFlags = raceFlags(playerFlag = PrimaryFlag.BLUE),
                events = listOf(SpeechEvent.BlueFlag),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.Flag.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )

            val telemetryJson = telemetryJsonSlot.captured
            val root = Json.parseToJsonElement(telemetryJson).jsonObject
            assertEquals("GREEN", root["previousRaceFlags"]!!.jsonObject["playerFlag"]!!.jsonPrimitive.content)
            assertEquals("BLUE", root["raceFlags"]!!.jsonObject["playerFlag"]!!.jsonPrimitive.content)
            assertEquals(200L, root["observedAtMs"]!!.jsonPrimitive.long)
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.BlueFlag, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
                    telemetryJson = telemetryJson,
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `読み上げたピットタイミングイベントにタイヤ摩耗データがシリアライズされて保存される`() =
        runTest {
            val telemetryJsonSlot = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(SpeechEvent.PitTimingWarning(laps = 2), queue = false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.PitTiming.Root,
                    telemetryJson = capture(telemetryJsonSlot),
                )
            } just Runs
            val processor = createProcessor()

            processor.processPitTiming(
                snapshot = pitTimingSnapshot(tyreWear = tyreWear(frontLeft = 0.3)),
                events = listOf(SpeechEvent.PitTimingWarning(laps = 2)),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.PitTiming.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = pitTimingLogContext(),
            )

            val telemetryJson = telemetryJsonSlot.captured
            val root = Json.parseToJsonElement(telemetryJson).jsonObject
            assertWheelValues(
                root["tyreWear"]!!.jsonObject["wheels"]!!.jsonObject,
                frontLeft = 0.3,
                frontRight = 0.9,
                rearLeft = 0.9,
                rearRight = 0.9,
            )
            assertEquals(0.5, root["virtualEnergy"]!!.jsonObject["remainingRatio"]!!.jsonPrimitive.double)
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.PitTimingWarning(laps = 2), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.PitTiming.Root,
                    telemetryJson = telemetryJson,
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `優先度の高い項目を再生中なら読み上げも保存もしない`() =
        runTest {
            val currentKey = ReadoutItemKey.LmuWindows.Flag.Root
            val newEvent = SpeechEvent.CarLeft
            every { ttsEngine.currentReadoutItemKey } returns currentKey
            val processor = createProcessor()

            processor.processVehicleApproach(
                vehicleApproach = leftVehicleApproach(),
                events = listOf(newEvent),
                readoutOrder =
                    listOf(
                        currentKey,
                        newEvent.readoutItemKey,
                    ),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )

            verify(exactly = 0) { ttsEngine.stop() }
            verify(exactly = 0) { ttsEngine.speak(newEvent, false) }
            verify(exactly = 0) { ttsEngine.speak(newEvent, true) }
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            confirmVerified(ttsEngine)
        }

    @Test
    fun `優先度で本来無視される項目でもキュー設定が有効ならキュー再生する`() =
        runTest {
            val currentKey = ReadoutItemKey.LmuWindows.Flag.Root
            val newEvent = SpeechEvent.CarLeft
            every { ttsEngine.speak(newEvent, queue = true) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    telemetryJson = capture(slot()),
                )
            } just Runs
            val processor = createProcessor()

            processor.processVehicleApproach(
                vehicleApproach = leftVehicleApproach(),
                events = listOf(newEvent),
                readoutOrder = listOf(currentKey, newEvent.readoutItemKey),
                queueEnabledStates = mapOf(newEvent.readoutItemKey to true),
                observedAtMs = 0L,
                logContext = logContext(),
            )

            verify(exactly = 0) { ttsEngine.stop() }
            verify(exactly = 1) { ttsEngine.speak(newEvent, queue = true) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    telemetryJson = capture(slot()),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `優先度の低い項目を再生中なら停止して読み上げる`() =
        runTest {
            val currentKey = ReadoutItemKey.LmuWindows.TyreWear.Root
            val newEvent = SpeechEvent.CarLeft
            every { ttsEngine.currentReadoutItemKey } returns currentKey
            every { ttsEngine.stop() } just Runs
            every { ttsEngine.speak(newEvent, queue = false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    telemetryJson = capture(slot()),
                )
            } just Runs
            val processor = createProcessor()

            processor.processVehicleApproach(
                vehicleApproach = leftVehicleApproach(),
                events = listOf(newEvent),
                readoutOrder = listOf(newEvent.readoutItemKey, currentKey),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )

            verify(exactly = 1) { ttsEngine.stop() }
            verify(exactly = 1) { ttsEngine.speak(newEvent, queue = false) }
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    telemetryJson = capture(slot()),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `ログ保存に失敗しても次の読み上げは継続する`() =
        runTest {
            val spokenEvents = mutableListOf<SpeechEvent>()
            var saveCount = 0
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(capture(spokenEvents), queue = false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 100L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    telemetryJson = capture(slot()),
                )
            } answers {
                saveCount += 1
                error("failed")
            }
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    telemetryJson = capture(slot()),
                )
            } answers { saveCount += 1 }
            val processor = createProcessor()

            processor.processVehicleApproach(
                vehicleApproach = leftVehicleApproach(distance = 4.0),
                events = listOf(SpeechEvent.CarLeft),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleApproach.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 100L,
                logContext = logContext(),
            )
            processor.processVehicleApproach(
                vehicleApproach = leftVehicleApproach(distance = 3.0),
                events = listOf(SpeechEvent.CarLeft),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleApproach.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )

            assertEquals(listOf<SpeechEvent>(SpeechEvent.CarLeft, SpeechEvent.CarLeft), spokenEvents)
            assertEquals(2, saveCount)
            verify(exactly = 2) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 2) { ttsEngine.speak(SpeechEvent.CarLeft, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 100L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    telemetryJson = capture(slot()),
                )
            }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    telemetryJson = capture(slot()),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    private fun createProcessor() =
        LmuWindowsNarratorEventProcessor(
            ttsEngine = ttsEngine,
            saveTelemetryLog = SaveTelemetryLogUseCase(telemetryLogRepository),
        )
}

private fun logContext() =
    LmuWindowsTelemetryLogContext(
        state = LmuWindowsNarratorState(),
        settings =
            LmuWindowsNarratorReadoutSettings(
                enabledStates = mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.Root to true),
                myBestLapVoiceType = MyBestLapVoiceType.FORMAL,
                redFlagVoiceType = RedFlagVoiceType.SESSION_STOP,
                currentLap = 1,
                skipFirstLap = false,
                vehicleApproachStartReadoutType = VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
                vehicleApproachSustainedApproachDurationSeconds = 7,
                vehicleApproachSustainedReadoutType = VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT,
                tyreTemperatureHighThresholdCelsius = 95,
                tyreTemperatureLowWarningPhases = emptySet(),
                tyreWearThresholdPercentage = 50,
                remainingVirtualEnergyThresholdPercentage = 50,
                pitTimingVirtualEnergyLapsThreshold = 3,
                pitTimingTyreWearLapsThreshold = 3,
            ),
        finalState = LmuWindowsNarratorState(),
    )

private fun leftVehicleApproach(distance: Double = 3.0) =
    LmuWindowsVehicleApproachData(
        sideBySideLeftVehicleIds = setOf(1),
        sideBySideRightVehicleIds = emptySet(),
        lateralDistanceLeftMeters = distance,
        lateralDistanceRightMeters = Double.MAX_VALUE,
    )

private fun raceFlags(playerFlag: PrimaryFlag) =
    LmuWindowsRaceFlagsData(
        gamePhase = SessionPhase.GREEN_FLAG,
        yellowFlagState = SessionYellowFlagState.NONE,
        sectorFlags = emptyList(),
        startLight = 0,
        numRedLights = 0,
        playerFlag = playerFlag,
        playerUnderYellow = false,
        playerCountLapFlag = CountLapFlag.COUNT_LAP_AND_TIME,
    )

private fun tyreWear(frontLeft: Double) =
    LmuWindowsTyreWearData(
        wheels =
            mapOf(
                WheelIndex.FRONT_LEFT to frontLeft,
                WheelIndex.FRONT_RIGHT to 0.9,
                WheelIndex.REAR_LEFT to 0.9,
                WheelIndex.REAR_RIGHT to 0.9,
            ),
    )

private fun assertWheelValues(
    wheels: JsonObject,
    frontLeft: Double,
    frontRight: Double,
    rearLeft: Double,
    rearRight: Double,
) {
    assertEquals(frontLeft, wheels["FRONT_LEFT"]!!.jsonPrimitive.double)
    assertEquals(frontRight, wheels["FRONT_RIGHT"]!!.jsonPrimitive.double)
    assertEquals(rearLeft, wheels["REAR_LEFT"]!!.jsonPrimitive.double)
    assertEquals(rearRight, wheels["REAR_RIGHT"]!!.jsonPrimitive.double)
}

private fun pitTimingLogContext() =
    LmuWindowsPitTimingLogContext(
        state = LmuWindowsNarratorState(),
        settings =
            LmuWindowsNarratorReadoutSettings(
                enabledStates = mapOf(ReadoutItemKey.LmuWindows.PitTiming.Root to true),
                myBestLapVoiceType = MyBestLapVoiceType.FORMAL,
                redFlagVoiceType = RedFlagVoiceType.SESSION_STOP,
                currentLap = 1,
                skipFirstLap = false,
                vehicleApproachStartReadoutType = VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
                vehicleApproachSustainedApproachDurationSeconds = 7,
                vehicleApproachSustainedReadoutType = VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT,
                tyreTemperatureHighThresholdCelsius = 95,
                tyreTemperatureLowWarningPhases = emptySet(),
                tyreWearThresholdPercentage = 50,
                remainingVirtualEnergyThresholdPercentage = 50,
                pitTimingVirtualEnergyLapsThreshold = 3,
                pitTimingTyreWearLapsThreshold = 3,
            ),
        finalState = LmuWindowsNarratorState(),
    )

private fun pitTimingSnapshot(tyreWear: LmuWindowsTyreWearData) =
    LmuWindowsPitTimingSnapshot(
        telemetry = fakeTelemetryData(),
        virtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = 0.5),
        tyreWear = tyreWear,
    )

private fun fakeTelemetryData() =
    LmuWindowsTelemetryData(
        timestampMs = 0L,
        engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
        inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
        tyres = LmuWindowsTyreData(wheels = emptyMap()),
        fuel = LmuWindowsFuelData(currentLiters = 0.0, capacityLiters = 0.0),
        timing =
            LmuWindowsTimingData(
                currentLapTimeMs = 0L,
                lastLapTimeMs = 0L,
                bestLapTimeMs = 0L,
                sector1Ms = 0L,
                sector1And2Ms = 0L,
                currentLap = 0,
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
