@file:Suppress("TooManyFunctions")

package kurou.kodriver.feature.lmuwindowsnarrator

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kurou.kodriver.domain.engine.ReadoutTextEvent
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.LateralDistanceMeters
import kurou.kodriver.domain.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsFuelUnit
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.model.LmuWindowsTyreWearRatio
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyRatio
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.PitTimingSource
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.LmuWindowsNarratorReadoutSettings
import kurou.kodriver.domain.usecase.LmuWindowsNarratorState
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartRightReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kurou.kodriver.domain.usecase.TyreTemperatureReadoutInput
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class LmuWindowsNarratorEventProcessorTest {
    private val telemetryLogRepository: TelemetryLogRepository = mockk()

    private val ttsEngine: TextToSpeechEngine = mockk()

    private val observeSectorYellow: ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase = mockk()
    private val observeBlue: ObserveLmuWindowsBlueFlagReadoutTextUseCase = mockk()
    private val observeFullCourseYellow: ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase = mockk()
    private val observeRed: ObserveLmuWindowsRedFlagReadoutTextUseCase = mockk()
    private val observeLeft: ObserveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase = mockk()
    private val observeRight: ObserveLmuWindowsVehicleApproachStartRightReadoutTextUseCase = mockk()
    private val observeSustainedLeft: ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase = mockk()
    private val observeSustainedRight: ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase = mockk()
    private val observePitTimingVirtualEnergyReadoutText: ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase =
        mockk()
    private val observePitTimingVirtualEnergyImminentReadoutText:
        ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase = mockk()
    private val observePitTimingTyreWearReadoutText: ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase = mockk()
    private val observePitTimingTyreWearImminentReadoutText:
        ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase = mockk()
    private val observeRemainingText: ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase = mockk()
    private val observeBrakeText: ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase = mockk()
    private val observeTyreWearText: ObserveLmuWindowsTyreWearReadoutTextUseCase = mockk()
    private val observeTyreOverheatReadoutText: ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase = mockk()
    private val observeTyreColdReadoutText: ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase = mockk()
    private val observeOverheatReadoutText: ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase = mockk()
    private val observePartDetachedReadoutText: ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase = mockk()
    private val observeTyreDetachedReadoutText: ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase = mockk()
    private val observeMyBestLapReadoutText: ObserveLmuWindowsMyBestLapReadoutTextUseCase = mockk()
    private val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val speakText: SpeakTextUseCase = mockk()
    private val speaker =
        LmuWindowsReadoutTextSpeaker(
            observeSectorYellow,
            observeBlue,
            observeFullCourseYellow,
            observeRed,
            observeLeft,
            observeRight,
            observeSustainedLeft,
            observeSustainedRight,
            observePitTimingVirtualEnergyReadoutText,
            observePitTimingVirtualEnergyImminentReadoutText,
            observePitTimingTyreWearReadoutText,
            observePitTimingTyreWearImminentReadoutText,
            observeRemainingText,
            observeBrakeText,
            observeTyreWearText,
            observeTyreOverheatReadoutText,
            observeTyreColdReadoutText,
            observeOverheatReadoutText,
            observePartDetachedReadoutText,
            observeTyreDetachedReadoutText,
            observeMyBestLapReadoutText,
            checkTextToSpeechAvailable,
            speakText,
        )

    @Test
    fun `読み上げたイベントを直前と現在の接近データとともに保存する`() =
        runTest {
            val telemetryJsonSlot = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(SpeechEvent.CarLeft(resolvedText = "カーレフト"), queue = false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    narratedText = "カーレフト",
                    narrationOutcome = NarrationOutcome.SPOKEN,
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
                events = listOf(SpeechEvent.CarLeft()),
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
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.CarLeft(resolvedText = "カーレフト"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    narratedText = "カーレフト",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = telemetryJson,
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `テレメトリログの保存に失敗しても例外を投げない`() =
        runTest {
            val observedAtMs = 0L
            val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root
            val narratedText = "カーレフト"
            val telemetryJsonSlot = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(SpeechEvent.CarLeft(resolvedText = "カーレフト"), queue = false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = observedAtMs,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = readoutItemKey,
                    narratedText = narratedText,
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(telemetryJsonSlot),
                )
            } throws RuntimeException("db error")

            createProcessor().processVehicleApproach(
                vehicleApproach = leftVehicleApproach(distance = 3.0),
                events = listOf(SpeechEvent.CarLeft()),
                readoutOrder = listOf(readoutItemKey),
                queueEnabledStates = emptyMap(),
                observedAtMs = observedAtMs,
                logContext = logContext(),
            )

            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.CarLeft(resolvedText = "カーレフト"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = observedAtMs,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = readoutItemKey,
                    narratedText = narratedText,
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = telemetryJsonSlot.captured,
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `読み上げたタイヤ摩耗イベントを直前と現在のタイヤ摩耗データとともに保存する`() =
        runTest {
            val telemetryJsonSlot = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every {
                ttsEngine.speak(SpeechEvent.TyreWearWarning(50, resolvedText = "閾値50%です"), queue = false)
            } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.TyreWear.Root,
                    narratedText = "閾値50%です",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(telemetryJsonSlot),
                )
            } just Runs
            val processor = createProcessor { "閾値50%です" }

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
                events = listOf(SpeechEvent.TyreWearWarning(50)),
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
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.TyreWearWarning(50, resolvedText = "閾値50%です"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.TyreWear.Root,
                    narratedText = "閾値50%です",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = telemetryJson,
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `読み上げたブレーキ温度イベントを直前と現在のブレーキ温度データとともに保存する`() =
        runTest {
            val telemetryJsonSlot = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every {
                ttsEngine.speak(SpeechEvent.BrakeOverheat(700, resolvedText = "閾値700℃です"), queue = false)
            } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.BrakeTemperature.Root,
                    narratedText = "閾値700℃です",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(telemetryJsonSlot),
                )
            } just Runs
            val processor = createProcessor { "閾値700℃です" }

            processor.processBrakeTemperature(
                brakeTemperature = brakeTemperature(frontLeft = 600.0),
                events = emptyList(),
                readoutOrder = emptyList(),
                queueEnabledStates = emptyMap(),
                observedAtMs = 100L,
                logContext = logContext(),
            )
            processor.processBrakeTemperature(
                brakeTemperature = brakeTemperature(frontLeft = 950.0),
                events = listOf(SpeechEvent.BrakeOverheat(700)),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.BrakeTemperature.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )

            val telemetryJson = telemetryJsonSlot.captured
            val root = Json.parseToJsonElement(telemetryJson).jsonObject
            assertWheelValues(
                root["previousBrakeTemperature"]!!.jsonObject["wheels"]!!.jsonObject,
                frontLeft = 600.0,
                frontRight = 600.0,
                rearLeft = 600.0,
                rearRight = 600.0,
            )
            assertWheelValues(
                root["brakeTemperature"]!!.jsonObject["wheels"]!!.jsonObject,
                frontLeft = 950.0,
                frontRight = 600.0,
                rearLeft = 600.0,
                rearRight = 600.0,
            )
            assertContains(telemetryJson, """"observedAtMs":200""")
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.BrakeOverheat(700, resolvedText = "閾値700℃です"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.BrakeTemperature.Root,
                    narratedText = "閾値700℃です",
                    narrationOutcome = NarrationOutcome.SPOKEN,
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
            every {
                ttsEngine.speak(SpeechEvent.RemainingVirtualEnergyWarning(50, resolvedText = "閾値50%です"), queue = false)
            } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root,
                    narratedText = "閾値50%です",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(telemetryJsonSlot),
                )
            } just Runs
            val processor = createProcessor { "閾値50%です" }

            processor.processRemainingVirtualEnergy(
                remainingVirtualEnergy =
                    LmuWindowsVirtualEnergyData(
                        remainingRatio = LmuWindowsVirtualEnergyRatio(0.8),
                    ),
                events = emptyList(),
                readoutOrder = emptyList(),
                queueEnabledStates = emptyMap(),
                observedAtMs = 100L,
                logContext = logContext(),
            )
            processor.processRemainingVirtualEnergy(
                remainingVirtualEnergy =
                    LmuWindowsVirtualEnergyData(
                        remainingRatio = LmuWindowsVirtualEnergyRatio(0.3),
                    ),
                events = listOf(SpeechEvent.RemainingVirtualEnergyWarning(50)),
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
            verify(exactly = 1) {
                ttsEngine.speak(SpeechEvent.RemainingVirtualEnergyWarning(50, resolvedText = "閾値50%です"), false)
            }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root,
                    narratedText = "閾値50%です",
                    narrationOutcome = NarrationOutcome.SPOKEN,
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
            every { ttsEngine.speak(SpeechEvent.BlueFlag(resolvedText = "ブルーフラッグ"), queue = false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
                    narratedText = "ブルーフラッグ",
                    narrationOutcome = NarrationOutcome.SPOKEN,
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
                events = listOf(SpeechEvent.BlueFlag()),
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
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.BlueFlag(resolvedText = "ブルーフラッグ"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
                    narratedText = "ブルーフラッグ",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = telemetryJson,
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `直前のフラグデータがないイベントはnullとして保存する`() =
        runTest {
            val telemetryJsonSlot = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(SpeechEvent.BlueFlag(resolvedText = "ブルーフラッグ"), queue = false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
                    narratedText = "ブルーフラッグ",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(telemetryJsonSlot),
                )
            } just Runs

            createProcessor().processRaceFlags(
                raceFlags = raceFlags(playerFlag = PrimaryFlag.BLUE),
                events = listOf(SpeechEvent.BlueFlag()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.Flag.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )

            assertContains(telemetryJsonSlot.captured, "\"previousRaceFlags\":null")
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.BlueFlag(resolvedText = "ブルーフラッグ"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
                    narratedText = "ブルーフラッグ",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = telemetryJsonSlot.captured,
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `直前のテレメトリデータがないイベントはnullとして保存する`() =
        runTest {
            val telemetryJsonSlot = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every {
                ttsEngine.speak(SpeechEvent.LmuWindowsMyBestLap(83_456L, "自己ベストラップ更新 1分23秒456"), queue = false)
            } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.MyBestLap.Root,
                    narratedText = "自己ベストラップ更新 1分23秒456",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(telemetryJsonSlot),
                )
            } just Runs

            createProcessor().processMyBestLap(
                telemetry = fakeTelemetryData(),
                events = listOf(SpeechEvent.LmuWindowsMyBestLap(83_456L)),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.MyBestLap.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )

            assertContains(telemetryJsonSlot.captured, "\"previousTelemetry\":null")
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) {
                ttsEngine.speak(SpeechEvent.LmuWindowsMyBestLap(83_456L, "自己ベストラップ更新 1分23秒456"), false)
            }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.MyBestLap.Root,
                    narratedText = "自己ベストラップ更新 1分23秒456",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = telemetryJsonSlot.captured,
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `直前の車両ダメージデータがないイベントはnullとして保存する`() =
        runTest {
            val telemetryJsonSlot = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(SpeechEvent.Overheating(resolvedText = "オーバーヒート"), queue = false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "オーバーヒート",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(telemetryJsonSlot),
                )
            } just Runs

            createProcessor().processVehicleDamage(
                vehicleDamage = vehicleDamage(overheating = true),
                events = listOf(SpeechEvent.Overheating()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleDamage.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )

            assertContains(telemetryJsonSlot.captured, "\"previousVehicleDamage\":null")
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.Overheating(resolvedText = "オーバーヒート"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "オーバーヒート",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = telemetryJsonSlot.captured,
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `読み上げたイベントを直前と現在の車両ダメージデータとともに保存する`() =
        runTest {
            val telemetryJsonSlot = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(SpeechEvent.Overheating(resolvedText = "オーバーヒート"), queue = false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "オーバーヒート",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(telemetryJsonSlot),
                )
            } just Runs
            val processor = createProcessor()

            processor.processVehicleDamage(
                vehicleDamage = vehicleDamage(overheating = false),
                events = emptyList(),
                readoutOrder = emptyList(),
                queueEnabledStates = emptyMap(),
                observedAtMs = 100L,
                logContext = logContext(),
            )
            processor.processVehicleDamage(
                vehicleDamage = vehicleDamage(overheating = true),
                events = listOf(SpeechEvent.Overheating()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleDamage.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )

            val telemetryJson = telemetryJsonSlot.captured
            val root = Json.parseToJsonElement(telemetryJson).jsonObject
            assertEquals(false, root["previousVehicleDamage"]!!.jsonObject["overheating"]!!.jsonPrimitive.boolean)
            assertEquals(true, root["vehicleDamage"]!!.jsonObject["overheating"]!!.jsonPrimitive.boolean)
            assertEquals(200L, root["observedAtMs"]!!.jsonPrimitive.long)
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.Overheating(resolvedText = "オーバーヒート"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "オーバーヒート",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = telemetryJson,
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `読み上げたタイヤ脱落イベントを直前と現在のデータとともに保存する`() =
        runTest {
            val telemetryJsonSlot = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(SpeechEvent.TyreDetached(resolvedText = "タイヤ脱落"), queue = false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "タイヤ脱落",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(telemetryJsonSlot),
                )
            } just Runs
            val processor = createProcessor()

            processor.processTyreDetached(
                tyreDetached = tyreDetached(),
                events = emptyList(),
                readoutOrder = emptyList(),
                queueEnabledStates = emptyMap(),
                observedAtMs = 100L,
                logContext = logContext(),
            )
            processor.processTyreDetached(
                tyreDetached = tyreDetached(WheelIndex.FRONT_LEFT),
                events = listOf(SpeechEvent.TyreDetached()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleDamage.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )

            val telemetryJson = telemetryJsonSlot.captured
            val root = Json.parseToJsonElement(telemetryJson).jsonObject
            assertEquals(
                false,
                root["previousTyreDetached"]!!
                    .jsonObject["wheels"]!!
                    .jsonObject["FRONT_LEFT"]!!
                    .jsonPrimitive.boolean,
            )
            assertEquals(
                true,
                root["tyreDetached"]!!
                    .jsonObject["wheels"]!!
                    .jsonObject["FRONT_LEFT"]!!
                    .jsonPrimitive.boolean,
            )
            assertEquals(200L, root["observedAtMs"]!!.jsonPrimitive.long)
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.TyreDetached(resolvedText = "タイヤ脱落"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "タイヤ脱落",
                    narrationOutcome = NarrationOutcome.SPOKEN,
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
            every {
                ttsEngine.speak(
                    SpeechEvent.PitTimingWarning(
                        laps = 2,
                        source = PitTimingSource.TyreWear,
                        resolvedText = "タイヤ交換まであと2周",
                    ),
                    queue = false,
                )
            } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.PitTiming.Root,
                    narratedText = "タイヤ交換まであと2周",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(telemetryJsonSlot),
                )
            } just Runs
            val processor = createProcessor { "タイヤ交換まであと2周" }

            processor.processPitTiming(
                snapshot = pitTimingSnapshot(tyreWear = tyreWear(frontLeft = 0.3)),
                events = listOf(SpeechEvent.PitTimingWarning(laps = 2, source = PitTimingSource.TyreWear)),
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
            verify(exactly = 1) {
                ttsEngine.speak(
                    SpeechEvent.PitTimingWarning(
                        laps = 2,
                        source = PitTimingSource.TyreWear,
                        resolvedText = "タイヤ交換まであと2周",
                    ),
                    false,
                )
            }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.PitTiming.Root,
                    narratedText = "タイヤ交換まであと2周",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = telemetryJson,
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `VEの自由文言で読み上げて本文をログに保存する`() =
        runTest {
            val telemetryJsonSlot = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every {
                ttsEngine.speak(
                    SpeechEvent.PitTimingWarning(
                        laps = 2,
                        source = PitTimingSource.VirtualEnergy,
                        resolvedText = "あと2周でピットへ",
                    ),
                    queue = false,
                )
            } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.PitTiming.Root,
                    narratedText = "あと2周でピットへ",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(telemetryJsonSlot),
                )
            } just Runs
            val processor = createProcessor { "あと2周でピットへ" }

            processor.processPitTiming(
                snapshot = pitTimingSnapshot(tyreWear = tyreWear(frontLeft = 0.3)),
                events = listOf(SpeechEvent.PitTimingWarning(laps = 2, source = PitTimingSource.VirtualEnergy)),
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
            verify(exactly = 1) {
                ttsEngine.speak(
                    SpeechEvent.PitTimingWarning(
                        laps = 2,
                        source = PitTimingSource.VirtualEnergy,
                        resolvedText = "あと2周でピットへ",
                    ),
                    false,
                )
            }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.PitTiming.Root,
                    narratedText = "あと2周でピットへ",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = telemetryJson,
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `VE本文がnullなら開始音と本文を要求せずスキップを記録する`() =
        runTest {
            val json = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.PitTiming.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(json),
                )
            } just Runs

            createProcessor { null }.processPitTiming(
                snapshot = pitTimingSnapshot(tyreWear = tyreWear(frontLeft = 0.3)),
                events = listOf(SpeechEvent.PitTimingWarning(0, PitTimingSource.VirtualEnergy)),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.PitTiming.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = pitTimingLogContext(),
            )

            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.PitTiming.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `タイヤ摩耗のVE本文がnullなら開始音と本文を要求せずスキップを記録する`() =
        runTest {
            val json = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.PitTiming.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(json),
                )
            } just Runs

            createProcessor { null }.processPitTiming(
                snapshot = pitTimingSnapshot(tyreWear = tyreWear(frontLeft = 0.3)),
                events = listOf(SpeechEvent.PitTimingWarning(0, PitTimingSource.TyreWear)),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.PitTiming.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = pitTimingLogContext(),
            )

            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.PitTiming.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `優先度の高い項目を再生中なら読み上げずSKIPPEDとして保存する`() =
        runTest {
            val json = slot<String>()
            val currentKey = ReadoutItemKey.LmuWindows.Flag.Root
            val newEvent = SpeechEvent.CarLeft()
            every { ttsEngine.currentReadoutItemKey } returns currentKey
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    narratedText = "カーレフト",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(json),
                )
            } just Runs
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
            verify(exactly = 0) { ttsEngine.speak(newEvent.withResolvedText("カーレフト"), false) }
            verify(exactly = 0) { ttsEngine.speak(newEvent.withResolvedText("カーレフト"), true) }
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    narratedText = "カーレフト",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = json.captured,
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `優先度で本来無視される項目でもキュー設定が有効ならキュー再生する`() =
        runTest {
            val currentKey = ReadoutItemKey.LmuWindows.Flag.Root
            val newEvent = SpeechEvent.CarLeft()
            every { ttsEngine.speak(newEvent.withResolvedText("カーレフト"), queue = true) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    narratedText = "カーレフト",
                    narrationOutcome = NarrationOutcome.QUEUED,
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
            verify(exactly = 1) { ttsEngine.speak(newEvent.withResolvedText("カーレフト"), queue = true) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    narratedText = "カーレフト",
                    narrationOutcome = NarrationOutcome.QUEUED,
                    telemetryJson = capture(slot()),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `優先度の低い項目を再生中なら停止して読み上げる`() =
        runTest {
            val currentKey = ReadoutItemKey.LmuWindows.TyreWear.Root
            val newEvent = SpeechEvent.CarLeft()
            every { ttsEngine.currentReadoutItemKey } returns currentKey
            every { ttsEngine.stop() } just Runs
            every { ttsEngine.speak(newEvent.withResolvedText("カーレフト"), queue = false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    narratedText = "カーレフト",
                    narrationOutcome = NarrationOutcome.INTERRUPTED,
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
            verify(exactly = 1) { ttsEngine.speak(newEvent.withResolvedText("カーレフト"), queue = false) }
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    narratedText = "カーレフト",
                    narrationOutcome = NarrationOutcome.INTERRUPTED,
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
                    narratedText = "カーレフト",
                    narrationOutcome = NarrationOutcome.SPOKEN,
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
                    narratedText = "カーレフト",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(slot()),
                )
            } answers { saveCount += 1 }
            val processor = createProcessor()

            processor.processVehicleApproach(
                vehicleApproach = leftVehicleApproach(distance = 4.0),
                events = listOf(SpeechEvent.CarLeft()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleApproach.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 100L,
                logContext = logContext(),
            )
            processor.processVehicleApproach(
                vehicleApproach = leftVehicleApproach(distance = 3.0),
                events = listOf(SpeechEvent.CarLeft()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleApproach.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )

            assertEquals(listOf<SpeechEvent>(SpeechEvent.CarLeft("カーレフト"), SpeechEvent.CarLeft("カーレフト")), spokenEvents)
            assertEquals(2, saveCount)
            verify(exactly = 2) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 2) { ttsEngine.speak(SpeechEvent.CarLeft(resolvedText = "カーレフト"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 100L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    narratedText = "カーレフト",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(slot()),
                )
            }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    narratedText = "カーレフト",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(slot()),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `フラッグの自由文字列をログに保存する`() =
        runTest {
            val json = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(SpeechEvent.BlueFlag(resolvedText = "後続に譲ってください"), false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
                    narratedText = "後続に譲ってください",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(json),
                )
            } just Runs
            createProcessor { "後続に譲ってください" }.processRaceFlags(
                raceFlags = raceFlags(playerFlag = PrimaryFlag.BLUE),
                events = listOf(SpeechEvent.BlueFlag()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.Flag.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.BlueFlag(resolvedText = "後続に譲ってください"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
                    narratedText = "後続に譲ってください",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `本文を読み上げられないフラッグはスキップとして記録する`() =
        runTest {
            val json = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(json),
                )
            } just Runs
            createProcessor { null }.processRaceFlags(
                raceFlags = raceFlags(playerFlag = PrimaryFlag.BLUE),
                events = listOf(SpeechEvent.BlueFlag()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.Flag.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `車両接近開始時の自由文字列をログに保存する`() =
        runTest {
            val json = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(SpeechEvent.CarRight(resolvedText = "後続に譲ってください"), false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    narratedText = "後続に譲ってください",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(json),
                )
            } just Runs
            createProcessor { "後続に譲ってください" }.processVehicleApproach(
                vehicleApproach = leftVehicleApproach(distance = 3.0),
                events = listOf(SpeechEvent.CarRight()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleApproach.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.CarRight(resolvedText = "後続に譲ってください"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    narratedText = "後続に譲ってください",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `本文を読み上げられない車両接近開始時はスキップとして記録する`() =
        runTest {
            val json = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(json),
                )
            } just Runs
            createProcessor { null }.processVehicleApproach(
                vehicleApproach = leftVehicleApproach(distance = 3.0),
                events = listOf(SpeechEvent.CarRight()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleApproach.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `車両接近継続時Leftの自由文字列をログに保存する`() =
        runTest {
            val json = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(SpeechEvent.CarLeftSustained(resolvedText = "後続に譲ってください"), false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    narratedText = "後続に譲ってください",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(json),
                )
            } just Runs
            createProcessor { "後続に譲ってください" }.processVehicleApproach(
                vehicleApproach = leftVehicleApproach(distance = 3.0),
                events = listOf(SpeechEvent.CarLeftSustained()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleApproach.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.CarLeftSustained(resolvedText = "後続に譲ってください"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    narratedText = "後続に譲ってください",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `本文を読み上げられない車両接近継続時Leftはスキップとして記録する`() =
        runTest {
            val json = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(json),
                )
            } just Runs
            createProcessor { null }.processVehicleApproach(
                vehicleApproach = leftVehicleApproach(distance = 3.0),
                events = listOf(SpeechEvent.CarLeftSustained()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleApproach.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `車両接近継続時Rightの自由文字列をログに保存する`() =
        runTest {
            val json = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(SpeechEvent.CarRightSustained(resolvedText = "後続に譲ってください"), false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    narratedText = "後続に譲ってください",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(json),
                )
            } just Runs
            createProcessor { "後続に譲ってください" }.processVehicleApproach(
                vehicleApproach = leftVehicleApproach(distance = 3.0),
                events = listOf(SpeechEvent.CarRightSustained()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleApproach.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.CarRightSustained(resolvedText = "後続に譲ってください"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    narratedText = "後続に譲ってください",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `本文を読み上げられない車両接近継続時Rightはスキップとして記録する`() =
        runTest {
            val json = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(json),
                )
            } just Runs
            createProcessor { null }.processVehicleApproach(
                vehicleApproach = leftVehicleApproach(distance = 3.0),
                events = listOf(SpeechEvent.CarRightSustained()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleApproach.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `残量警告の文言がnullなら読み上げず空文言でSKIPPEDを保存する`() =
        runTest {
            val json = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(json),
                )
            } just Runs
            createProcessor { null }.processRemainingVirtualEnergy(
                remainingVirtualEnergy =
                    LmuWindowsVirtualEnergyData(
                        remainingRatio = LmuWindowsVirtualEnergyRatio(0.3),
                    ),
                events = listOf(SpeechEvent.RemainingVirtualEnergyWarning(50)),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )
            verify(exactly = 0) { ttsEngine.speak(SpeechEvent.RemainingVirtualEnergyWarning(50), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `摩耗警告の文言がnullなら読み上げず空文言でSKIPPEDを保存する`() =
        runTest {
            val json = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.TyreWear.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(json),
                )
            } just Runs
            createProcessor { null }.processTyreWear(
                tyreWear = tyreWear(frontLeft = 0.3),
                events = listOf(SpeechEvent.TyreWearWarning(50)),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.TyreWear.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )
            verify(exactly = 0) { ttsEngine.speak(SpeechEvent.TyreWearWarning(50), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.TyreWear.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `ブレーキ過熱警告の文言がnullなら読み上げず空文言でSKIPPEDを保存する`() =
        runTest {
            val json = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.BrakeTemperature.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(json),
                )
            } just Runs
            createProcessor { null }.processBrakeTemperature(
                brakeTemperature = brakeTemperature(frontLeft = 950.0),
                events = listOf(SpeechEvent.BrakeOverheat(700)),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.BrakeTemperature.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )
            verify(exactly = 0) { ttsEngine.speak(SpeechEvent.BrakeOverheat(700), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 0L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.BrakeTemperature.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `過熱警告は自由文言を読み上げてログに保存する`() =
        runTest {
            val telemetryJsonSlot = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(SpeechEvent.TyreOverheat(100, "タイヤを冷やして"), queue = false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.TyreTemperature.Root,
                    narratedText = "タイヤを冷やして",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(telemetryJsonSlot),
                )
            } just Runs
            val context = logContext()
            createProcessor { "タイヤを冷やして" }.processTyreTemperature(
                input =
                    TyreTemperatureReadoutInput(
                        tyreCarcassTemperature =
                            LmuWindowsTyreCarcassTemperatureData(
                                wheels = mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(100f)),
                            ),
                        raceFlags = raceFlags(PrimaryFlag.GREEN),
                    ),
                events = listOf(SpeechEvent.TyreOverheat(100)),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext =
                    LmuWindowsTyreTemperatureLogContext(
                        context.state,
                        context.settings,
                        context.state,
                        context.finalState,
                    ),
            )
            assertContains(telemetryJsonSlot.captured, "\"observedAtMs\":200")
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.TyreOverheat(100, "タイヤを冷やして"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.TyreTemperature.Root,
                    narratedText = "タイヤを冷やして",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = telemetryJsonSlot.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `過熱警告の空白文言では読み上げずSKIPPEDを記録する`() =
        runTest {
            val telemetryJsonSlot = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.TyreTemperature.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(telemetryJsonSlot),
                )
            } just Runs
            val context = logContext()
            createProcessor { null }.processTyreTemperature(
                input =
                    TyreTemperatureReadoutInput(
                        tyreCarcassTemperature =
                            LmuWindowsTyreCarcassTemperatureData(
                                wheels = mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(100f)),
                            ),
                        raceFlags = raceFlags(PrimaryFlag.GREEN),
                    ),
                events = listOf(SpeechEvent.TyreOverheat(100)),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext =
                    LmuWindowsTyreTemperatureLogContext(
                        context.state,
                        context.settings,
                        context.state,
                        context.finalState,
                    ),
            )
            assertContains(telemetryJsonSlot.captured, "\"observedAtMs\":200")
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(SpeechEvent.TyreOverheat(100, "タイヤを冷やして"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.TyreTemperature.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = telemetryJsonSlot.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `過熱警告のTTS利用不可では読み上げずSKIPPEDを記録する`() =
        runTest {
            val telemetryJsonSlot = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.TyreTemperature.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(telemetryJsonSlot),
                )
            } just Runs
            val context = logContext()
            createProcessor { null }.processTyreTemperature(
                input =
                    TyreTemperatureReadoutInput(
                        tyreCarcassTemperature =
                            LmuWindowsTyreCarcassTemperatureData(
                                wheels = mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(100f)),
                            ),
                        raceFlags = raceFlags(PrimaryFlag.GREEN),
                    ),
                events = listOf(SpeechEvent.TyreOverheat(100)),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext =
                    LmuWindowsTyreTemperatureLogContext(
                        context.state,
                        context.settings,
                        context.state,
                        context.finalState,
                    ),
            )
            assertContains(telemetryJsonSlot.captured, "\"observedAtMs\":200")
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(SpeechEvent.TyreOverheat(100, "タイヤを冷やして"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.TyreTemperature.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = telemetryJsonSlot.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `低温警告は自由文言を読み上げてログに保存する`() =
        runTest {
            val telemetryJsonSlot = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(SpeechEvent.TyreCold(60, "タイヤを温めて"), queue = false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.TyreTemperature.Root,
                    narratedText = "タイヤを温めて",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(telemetryJsonSlot),
                )
            } just Runs
            val context = logContext()
            createProcessor { "タイヤを温めて" }.processTyreTemperature(
                input =
                    TyreTemperatureReadoutInput(
                        tyreCarcassTemperature =
                            LmuWindowsTyreCarcassTemperatureData(
                                wheels = mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(100f)),
                            ),
                        raceFlags = raceFlags(PrimaryFlag.GREEN),
                    ),
                events = listOf(SpeechEvent.TyreCold(60)),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext =
                    LmuWindowsTyreTemperatureLogContext(
                        context.state,
                        context.settings,
                        context.state,
                        context.finalState,
                    ),
            )
            assertContains(telemetryJsonSlot.captured, "\"observedAtMs\":200")
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.TyreCold(60, "タイヤを温めて"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.TyreTemperature.Root,
                    narratedText = "タイヤを温めて",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = telemetryJsonSlot.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `低温警告の空白文言では読み上げずSKIPPEDを記録する`() =
        runTest {
            val telemetryJsonSlot = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.TyreTemperature.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(telemetryJsonSlot),
                )
            } just Runs
            val context = logContext()
            createProcessor { " " }.processTyreTemperature(
                input =
                    TyreTemperatureReadoutInput(
                        tyreCarcassTemperature =
                            LmuWindowsTyreCarcassTemperatureData(
                                wheels = mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(100f)),
                            ),
                        raceFlags = raceFlags(PrimaryFlag.GREEN),
                    ),
                events = listOf(SpeechEvent.TyreCold(60)),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext =
                    LmuWindowsTyreTemperatureLogContext(
                        context.state,
                        context.settings,
                        context.state,
                        context.finalState,
                    ),
            )
            assertContains(telemetryJsonSlot.captured, "\"observedAtMs\":200")
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(SpeechEvent.TyreCold(60, "タイヤを温めて"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.TyreTemperature.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = telemetryJsonSlot.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `低温警告のTTS利用不可では読み上げずSKIPPEDを記録する`() =
        runTest {
            val telemetryJsonSlot = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.TyreTemperature.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(telemetryJsonSlot),
                )
            } just Runs
            val context = logContext()
            createProcessor { null }.processTyreTemperature(
                input =
                    TyreTemperatureReadoutInput(
                        tyreCarcassTemperature =
                            LmuWindowsTyreCarcassTemperatureData(
                                wheels = mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(100f)),
                            ),
                        raceFlags = raceFlags(PrimaryFlag.GREEN),
                    ),
                events = listOf(SpeechEvent.TyreCold(60)),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.TyreTemperature.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext =
                    LmuWindowsTyreTemperatureLogContext(
                        context.state,
                        context.settings,
                        context.state,
                        context.finalState,
                    ),
            )
            assertContains(telemetryJsonSlot.captured, "\"observedAtMs\":200")
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(SpeechEvent.TyreCold(60, "タイヤを温めて"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.TyreTemperature.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = telemetryJsonSlot.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `タイヤ温度警告はキュー待機中に設定が変わっても判定時の本文を発話してログと一致する`() =
        runTest {
            val key = ReadoutItemKey.LmuWindows.TyreTemperature.Root
            val template = MutableStateFlow("警告{celsius}℃")
            val queuedEvents = mutableListOf<SpeechEvent>()
            val telemetryJsonSlot = slot<String>()
            val events = listOf(SpeechEvent.TyreOverheat(100), SpeechEvent.TyreCold(60))
            val resolvedEvents =
                listOf(SpeechEvent.TyreOverheat(100, "警告100℃"), SpeechEvent.TyreCold(60, "警告60℃"))
            every { observeTyreOverheatReadoutText() } returns template
            every { observeTyreColdReadoutText() } returns template
            coEvery { checkTextToSpeechAvailable() } returns true
            resolvedEvents.forEach { event ->
                every { ttsEngine.speak(event, queue = true) } answers { queuedEvents += event }
            }
            listOf("警告100℃", "警告60℃").forEach { text ->
                coEvery { speakText(text, volume = 40) } just Runs
                coEvery {
                    telemetryLogRepository.saveTelemetryLog(
                        createdAt = 200L,
                        simulator = Simulator.LmuWindows,
                        readoutItemKey = key,
                        narratedText = text,
                        narrationOutcome = NarrationOutcome.QUEUED,
                        telemetryJson = capture(telemetryJsonSlot),
                    )
                } just Runs
            }
            val context = logContext()
            createProcessor(speaker::readoutText).processTyreTemperature(
                input =
                    TyreTemperatureReadoutInput(
                        LmuWindowsTyreCarcassTemperatureData(
                            wheels = mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(100f)),
                        ),
                        raceFlags(PrimaryFlag.GREEN),
                    ),
                events = events,
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.Flag.Root, key),
                queueEnabledStates = mapOf(key to true),
                observedAtMs = 200L,
                logContext =
                    LmuWindowsTyreTemperatureLogContext(
                        context.state,
                        context.settings,
                        context.state,
                        context.finalState,
                    ),
            )
            template.update { "変更後{celsius}℃" }
            assertEquals<List<SpeechEvent>>(resolvedEvents, queuedEvents)
            queuedEvents.forEach { speaker(it, 40) }
            verify(exactly = 1) { observeTyreOverheatReadoutText() }
            verify(exactly = 1) { observeTyreColdReadoutText() }
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            resolvedEvents.forEach { event ->
                verify(exactly = 1) { ttsEngine.speak(event, queue = true) }
            }
            listOf("警告100℃", "警告60℃").forEach { text ->
                coVerify(exactly = 1) { speakText(text, volume = 40) }
                coVerify(exactly = 1) {
                    telemetryLogRepository.saveTelemetryLog(
                        createdAt = 200L,
                        simulator = Simulator.LmuWindows,
                        readoutItemKey = key,
                        narratedText = text,
                        narrationOutcome = NarrationOutcome.QUEUED,
                        telemetryJson = telemetryJsonSlot.captured,
                    )
                }
            }
            coVerify(exactly = 4) { checkTextToSpeechAvailable() }
            confirmVerified(
                ttsEngine,
                telemetryLogRepository,
                observeTyreOverheatReadoutText,
                observeTyreColdReadoutText,
                checkTextToSpeechAvailable,
                speakText,
            )
        }

    @Test
    fun `オーバーヒートの自由文言をログと発話に反映する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(SpeechEvent.Overheating("カスタム"), queue = false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "カスタム",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(telemetryJsons),
                )
            } just Runs
            createProcessor { "カスタム" }.processVehicleDamage(
                vehicleDamage = vehicleDamage(overheating = true),
                events = listOf(SpeechEvent.Overheating()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleDamage.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.Overheating("カスタム"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "カスタム",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = telemetryJsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `オーバーヒートの空白文言をログと発話に反映する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(telemetryJsons),
                )
            } just Runs
            createProcessor { " " }.processVehicleDamage(
                vehicleDamage = vehicleDamage(overheating = true),
                events = listOf(SpeechEvent.Overheating()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleDamage.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(SpeechEvent.Overheating("カスタム"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = telemetryJsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `オーバーヒートのTTS利用不可をログと発話に反映する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(telemetryJsons),
                )
            } just Runs
            createProcessor { null }.processVehicleDamage(
                vehicleDamage = vehicleDamage(overheating = true),
                events = listOf(SpeechEvent.Overheating()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleDamage.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(SpeechEvent.Overheating("カスタム"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = telemetryJsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `自己ベストラップの自由文言をログと発話に反映する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(SpeechEvent.LmuWindowsMyBestLap(83_456L, "カスタム"), queue = false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.MyBestLap.Root,
                    narratedText = "カスタム",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(telemetryJsons),
                )
            } just Runs
            createProcessor { "カスタム" }.processMyBestLap(
                telemetry = fakeTelemetryData(bestLapTimeMs = 83_456L),
                events = listOf(SpeechEvent.LmuWindowsMyBestLap(83_456L)),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.MyBestLap.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.LmuWindowsMyBestLap(83_456L, "カスタム"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.MyBestLap.Root,
                    narratedText = "カスタム",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = telemetryJsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `自己ベストラップの空白文言をログと発話に反映する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.MyBestLap.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(telemetryJsons),
                )
            } just Runs
            createProcessor { " " }.processMyBestLap(
                telemetry = fakeTelemetryData(bestLapTimeMs = 83_456L),
                events = listOf(SpeechEvent.LmuWindowsMyBestLap(83_456L)),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.MyBestLap.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(SpeechEvent.LmuWindowsMyBestLap(83_456L, "カスタム"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.MyBestLap.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = telemetryJsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `自己ベストラップのTTS利用不可をログと発話に反映する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.MyBestLap.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(telemetryJsons),
                )
            } just Runs
            createProcessor { null }.processMyBestLap(
                telemetry = fakeTelemetryData(bestLapTimeMs = 83_456L),
                events = listOf(SpeechEvent.LmuWindowsMyBestLap(83_456L)),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.MyBestLap.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(SpeechEvent.LmuWindowsMyBestLap(83_456L, "カスタム"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.MyBestLap.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = telemetryJsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `部品脱落の自由文言をログと発話に反映する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(SpeechEvent.PartDetached("カスタム"), queue = false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "カスタム",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(telemetryJsons),
                )
            } just Runs
            createProcessor { "カスタム" }.processVehicleDamage(
                vehicleDamage = vehicleDamage(overheating = true),
                events = listOf(SpeechEvent.PartDetached()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleDamage.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.PartDetached("カスタム"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "カスタム",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = telemetryJsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `部品脱落の空白文言をログと発話に反映する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(telemetryJsons),
                )
            } just Runs
            createProcessor { " " }.processVehicleDamage(
                vehicleDamage = vehicleDamage(overheating = true),
                events = listOf(SpeechEvent.PartDetached()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleDamage.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(SpeechEvent.PartDetached("カスタム"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = telemetryJsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `部品脱落のTTS利用不可をログと発話に反映する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(telemetryJsons),
                )
            } just Runs
            createProcessor { null }.processVehicleDamage(
                vehicleDamage = vehicleDamage(overheating = true),
                events = listOf(SpeechEvent.PartDetached()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleDamage.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(SpeechEvent.PartDetached("カスタム"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = telemetryJsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `タイヤ脱落の自由文言をログと発話に反映する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(SpeechEvent.TyreDetached("カスタム"), queue = false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "カスタム",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(telemetryJsons),
                )
            } just Runs
            createProcessor { "カスタム" }.processTyreDetached(
                tyreDetached = tyreDetached(WheelIndex.FRONT_LEFT),
                events = listOf(SpeechEvent.TyreDetached()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleDamage.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.TyreDetached("カスタム"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "カスタム",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = telemetryJsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `タイヤ脱落の空白文言をログと発話に反映する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(telemetryJsons),
                )
            } just Runs
            createProcessor { " " }.processTyreDetached(
                tyreDetached = tyreDetached(WheelIndex.FRONT_LEFT),
                events = listOf(SpeechEvent.TyreDetached()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleDamage.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(SpeechEvent.TyreDetached("カスタム"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = telemetryJsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `タイヤ脱落のTTS利用不可をログと発話に反映する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(telemetryJsons),
                )
            } just Runs
            createProcessor { null }.processTyreDetached(
                tyreDetached = tyreDetached(WheelIndex.FRONT_LEFT),
                events = listOf(SpeechEvent.TyreDetached()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleDamage.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(SpeechEvent.TyreDetached("カスタム"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = telemetryJsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `車両故障はキュー待機中の設定変更でも判定時の本文を発話してログと一致する`() =
        runTest {
            val key = ReadoutItemKey.LmuWindows.VehicleDamage.Root
            val template = MutableStateFlow("判定時の本文")
            val queuedEvents = mutableListOf<SpeechEvent>()
            val telemetryJsons = mutableListOf<String>()
            val events = listOf(SpeechEvent.Overheating(), SpeechEvent.PartDetached(), SpeechEvent.TyreDetached())
            val resolvedEvents =
                listOf(
                    SpeechEvent.Overheating("判定時の本文"),
                    SpeechEvent.PartDetached("判定時の本文"),
                    SpeechEvent.TyreDetached("判定時の本文"),
                )
            every { observeOverheatReadoutText() } returns template
            every { observePartDetachedReadoutText() } returns template
            every { observeTyreDetachedReadoutText() } returns template
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("判定時の本文", volume = 40) } just Runs
            resolvedEvents.forEach { event ->
                every { ttsEngine.speak(event, queue = true) } answers { queuedEvents += event }
            }
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = key,
                    narratedText = "判定時の本文",
                    narrationOutcome = NarrationOutcome.QUEUED,
                    telemetryJson = capture(telemetryJsons),
                )
            } just Runs
            val processor = createProcessor(speaker::readoutText)
            processor.processVehicleDamage(
                vehicleDamage = vehicleDamage(overheating = true),
                events = events.take(2),
                readoutOrder = listOf(key),
                queueEnabledStates = mapOf(key to true),
                observedAtMs = 200L,
                logContext = logContext(),
            )
            processor.processTyreDetached(
                tyreDetached = tyreDetached(WheelIndex.FRONT_LEFT),
                events = events.takeLast(1),
                readoutOrder = listOf(key),
                queueEnabledStates = mapOf(key to true),
                observedAtMs = 200L,
                logContext = logContext(),
            )
            template.update { "変更後の本文" }
            assertEquals<List<SpeechEvent>>(resolvedEvents, queuedEvents)
            queuedEvents.forEach { speaker(it, 40) }
            verify(exactly = 1) { observeOverheatReadoutText() }
            verify(exactly = 1) { observePartDetachedReadoutText() }
            verify(exactly = 1) { observeTyreDetachedReadoutText() }
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            resolvedEvents.forEach { event ->
                verify(exactly = 1) { ttsEngine.speak(event, queue = true) }
            }
            coVerify(exactly = 6) { checkTextToSpeechAvailable() }
            coVerify(exactly = 3) { speakText("判定時の本文", volume = 40) }
            telemetryJsons.distinct().forEach { telemetryJson ->
                coVerify(exactly = telemetryJsons.count { it == telemetryJson }) {
                    telemetryLogRepository.saveTelemetryLog(
                        createdAt = 200L,
                        simulator = Simulator.LmuWindows,
                        readoutItemKey = key,
                        narratedText = "判定時の本文",
                        narrationOutcome = NarrationOutcome.QUEUED,
                        telemetryJson = telemetryJson,
                    )
                }
            }
            confirmVerified(
                ttsEngine,
                telemetryLogRepository,
                observeOverheatReadoutText,
                observePartDetachedReadoutText,
                observeTyreDetachedReadoutText,
                observeMyBestLapReadoutText,
                checkTextToSpeechAvailable,
                speakText,
            )
        }

    @Test
    fun `自己ベストラップはキュー待機中の設定変更でも判定時の本文を発話してログと一致する`() =
        runTest {
            val key = ReadoutItemKey.LmuWindows.MyBestLap.Root
            val template = MutableStateFlow("判定時の本文{laptime}")
            val queuedEvents = mutableListOf<SpeechEvent>()
            val telemetryJsons = mutableListOf<String>()
            val events = listOf(SpeechEvent.LmuWindowsMyBestLap(83_456L))
            val resolvedEvents: List<SpeechEvent> =
                listOf(SpeechEvent.LmuWindowsMyBestLap(83_456L, "判定時の本文1分23秒456"))
            every { observeMyBestLapReadoutText() } returns template
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("判定時の本文1分23秒456", volume = 40) } just Runs
            resolvedEvents.forEach { event ->
                every { ttsEngine.speak(event, queue = true) } answers { queuedEvents += event }
            }
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = key,
                    narratedText = "判定時の本文1分23秒456",
                    narrationOutcome = NarrationOutcome.QUEUED,
                    telemetryJson = capture(telemetryJsons),
                )
            } just Runs
            val processor = createProcessor(speaker::readoutText)
            processor.processMyBestLap(
                telemetry = fakeTelemetryData(bestLapTimeMs = 83_456L),
                events = events,
                readoutOrder = listOf(key),
                queueEnabledStates = mapOf(key to true),
                observedAtMs = 200L,
                logContext = logContext(),
            )
            template.update { "変更後の本文" }
            assertEquals(resolvedEvents, queuedEvents)
            queuedEvents.forEach { speaker(it, 40) }
            verify(exactly = 1) { observeMyBestLapReadoutText() }
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            resolvedEvents.forEach { event ->
                verify(exactly = 1) { ttsEngine.speak(event, queue = true) }
            }
            coVerify(exactly = 2) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("判定時の本文1分23秒456", volume = 40) }
            telemetryJsons.distinct().forEach { telemetryJson ->
                coVerify(exactly = telemetryJsons.count { it == telemetryJson }) {
                    telemetryLogRepository.saveTelemetryLog(
                        createdAt = 200L,
                        simulator = Simulator.LmuWindows,
                        readoutItemKey = key,
                        narratedText = "判定時の本文1分23秒456",
                        narrationOutcome = NarrationOutcome.QUEUED,
                        telemetryJson = telemetryJson,
                    )
                }
            }
            confirmVerified(
                ttsEngine,
                telemetryLogRepository,
                observeMyBestLapReadoutText,
                checkTextToSpeechAvailable,
                speakText,
            )
        }

    @Test
    fun `フラッグはキュー待機中の設定変更でも判定時の本文を発話してログと一致する`() =
        runTest {
            val key = ReadoutItemKey.LmuWindows.Flag.Root
            val template = MutableStateFlow("判定時の本文")
            val queuedEvents = mutableListOf<SpeechEvent>()
            val telemetryJsons = mutableListOf<String>()
            val events =
                listOf<ReadoutTextEvent>(
                    SpeechEvent.BlueFlag(),
                    SpeechEvent.YellowFlag(),
                    SpeechEvent.FullCourseYellow(),
                    SpeechEvent.RedFlag(),
                )
            val resolvedEvents = events.map { it.withResolvedText("判定時の本文") }
            every { observeBlue() } returns template
            every { observeSectorYellow() } returns template
            every { observeFullCourseYellow() } returns template
            every { observeRed() } returns template
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("判定時の本文", volume = 40) } just Runs
            resolvedEvents.forEach { event ->
                every { ttsEngine.speak(event, queue = true) } answers { queuedEvents += event }
            }
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = key,
                    narratedText = "判定時の本文",
                    narrationOutcome = NarrationOutcome.QUEUED,
                    telemetryJson = capture(telemetryJsons),
                )
            } just Runs
            val processor = createProcessor(speaker::readoutText)
            processor.processRaceFlags(
                raceFlags = raceFlags(playerFlag = PrimaryFlag.BLUE),
                events = events,
                readoutOrder = listOf(key),
                queueEnabledStates = mapOf(key to true),
                observedAtMs = 200L,
                logContext = logContext(),
            )
            template.update { "変更後の本文" }
            assertEquals<List<SpeechEvent>>(resolvedEvents, queuedEvents)
            queuedEvents.forEach { speaker(it, 40) }
            verify(exactly = 1) { observeBlue() }
            verify(exactly = 1) { observeSectorYellow() }
            verify(exactly = 1) { observeFullCourseYellow() }
            verify(exactly = 1) { observeRed() }
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            resolvedEvents.forEach { event ->
                verify(exactly = 1) { ttsEngine.speak(event, queue = true) }
            }
            coVerify(exactly = 8) { checkTextToSpeechAvailable() }
            coVerify(exactly = 4) { speakText("判定時の本文", volume = 40) }
            telemetryJsons.distinct().forEach { telemetryJson ->
                coVerify(exactly = telemetryJsons.count { it == telemetryJson }) {
                    telemetryLogRepository.saveTelemetryLog(
                        createdAt = 200L,
                        simulator = Simulator.LmuWindows,
                        readoutItemKey = key,
                        narratedText = "判定時の本文",
                        narrationOutcome = NarrationOutcome.QUEUED,
                        telemetryJson = telemetryJson,
                    )
                }
            }
            confirmVerified(
                ttsEngine,
                telemetryLogRepository,
                observeBlue,
                observeSectorYellow,
                observeFullCourseYellow,
                observeRed,
                checkTextToSpeechAvailable,
                speakText,
            )
        }

    @Test
    fun `車両接近はキュー待機中の設定変更でも判定時の本文を発話してログと一致する`() =
        runTest {
            val key = ReadoutItemKey.LmuWindows.VehicleApproach.Root
            val template = MutableStateFlow("判定時の本文")
            val queuedEvents = mutableListOf<SpeechEvent>()
            val telemetryJsons = mutableListOf<String>()
            val events =
                listOf<ReadoutTextEvent>(
                    SpeechEvent.CarLeft(),
                    SpeechEvent.CarRight(),
                    SpeechEvent.CarLeftSustained(),
                    SpeechEvent.CarRightSustained(),
                )
            val resolvedEvents = events.map { it.withResolvedText("判定時の本文") }
            every { observeLeft() } returns template
            every { observeRight() } returns template
            every { observeSustainedLeft() } returns template
            every { observeSustainedRight() } returns template
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("判定時の本文", volume = 40) } just Runs
            resolvedEvents.forEach { event ->
                every { ttsEngine.speak(event, queue = true) } answers { queuedEvents += event }
            }
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = key,
                    narratedText = "判定時の本文",
                    narrationOutcome = NarrationOutcome.QUEUED,
                    telemetryJson = capture(telemetryJsons),
                )
            } just Runs
            val processor = createProcessor(speaker::readoutText)
            processor.processVehicleApproach(
                vehicleApproach = leftVehicleApproach(),
                events = events,
                readoutOrder = listOf(key),
                queueEnabledStates = mapOf(key to true),
                observedAtMs = 200L,
                logContext = logContext(),
            )
            template.update { "変更後の本文" }
            assertEquals<List<SpeechEvent>>(resolvedEvents, queuedEvents)
            queuedEvents.forEach { speaker(it, 40) }
            verify(exactly = 1) { observeLeft() }
            verify(exactly = 1) { observeRight() }
            verify(exactly = 1) { observeSustainedLeft() }
            verify(exactly = 1) { observeSustainedRight() }
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            resolvedEvents.forEach { event ->
                verify(exactly = 1) { ttsEngine.speak(event, queue = true) }
            }
            coVerify(exactly = 8) { checkTextToSpeechAvailable() }
            coVerify(exactly = 4) { speakText("判定時の本文", volume = 40) }
            telemetryJsons.distinct().forEach { telemetryJson ->
                coVerify(exactly = telemetryJsons.count { it == telemetryJson }) {
                    telemetryLogRepository.saveTelemetryLog(
                        createdAt = 200L,
                        simulator = Simulator.LmuWindows,
                        readoutItemKey = key,
                        narratedText = "判定時の本文",
                        narrationOutcome = NarrationOutcome.QUEUED,
                        telemetryJson = telemetryJson,
                    )
                }
            }
            confirmVerified(
                ttsEngine,
                telemetryLogRepository,
                observeLeft,
                observeRight,
                observeSustainedLeft,
                observeSustainedRight,
                checkTextToSpeechAvailable,
                speakText,
            )
        }

    @Test
    fun `ピットタイミングはキュー待機中の設定変更でも判定時の本文を発話してログと一致する`() =
        runTest {
            val key = ReadoutItemKey.LmuWindows.PitTiming.Root
            val template = MutableStateFlow("判定時の本文")
            val queuedEvents = mutableListOf<SpeechEvent>()
            val telemetryJsons = mutableListOf<String>()
            val events =
                listOf<ReadoutTextEvent>(
                    SpeechEvent.PitTimingWarning(2, PitTimingSource.VirtualEnergy),
                    SpeechEvent.PitTimingWarning(0, PitTimingSource.VirtualEnergy),
                    SpeechEvent.PitTimingWarning(2, PitTimingSource.TyreWear),
                    SpeechEvent.PitTimingWarning(0, PitTimingSource.TyreWear),
                )
            val resolvedEvents = events.map { it.withResolvedText("判定時の本文") }
            every { observePitTimingVirtualEnergyReadoutText() } returns template
            every { observePitTimingVirtualEnergyImminentReadoutText() } returns template
            every { observePitTimingTyreWearReadoutText() } returns template
            every { observePitTimingTyreWearImminentReadoutText() } returns template
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("判定時の本文", volume = 40) } just Runs
            resolvedEvents.forEach { event ->
                every { ttsEngine.speak(event, queue = true) } answers { queuedEvents += event }
            }
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = key,
                    narratedText = "判定時の本文",
                    narrationOutcome = NarrationOutcome.QUEUED,
                    telemetryJson = capture(telemetryJsons),
                )
            } just Runs
            val processor = createProcessor(speaker::readoutText)
            processor.processPitTiming(
                snapshot = pitTimingSnapshot(tyreWear = tyreWear(frontLeft = 0.3)),
                events = events,
                readoutOrder = listOf(key),
                queueEnabledStates = mapOf(key to true),
                observedAtMs = 200L,
                logContext = pitTimingLogContext(),
            )
            template.update { "変更後の本文" }
            assertEquals<List<SpeechEvent>>(resolvedEvents, queuedEvents)
            queuedEvents.forEach { speaker(it, 40) }
            verify(exactly = 1) { observePitTimingVirtualEnergyReadoutText() }
            verify(exactly = 1) { observePitTimingVirtualEnergyImminentReadoutText() }
            verify(exactly = 1) { observePitTimingTyreWearReadoutText() }
            verify(exactly = 1) { observePitTimingTyreWearImminentReadoutText() }
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            resolvedEvents.forEach { event ->
                verify(exactly = 1) { ttsEngine.speak(event, queue = true) }
            }
            coVerify(exactly = 8) { checkTextToSpeechAvailable() }
            coVerify(exactly = 4) { speakText("判定時の本文", volume = 40) }
            telemetryJsons.distinct().forEach { telemetryJson ->
                coVerify(exactly = telemetryJsons.count { it == telemetryJson }) {
                    telemetryLogRepository.saveTelemetryLog(
                        createdAt = 200L,
                        simulator = Simulator.LmuWindows,
                        readoutItemKey = key,
                        narratedText = "判定時の本文",
                        narrationOutcome = NarrationOutcome.QUEUED,
                        telemetryJson = telemetryJson,
                    )
                }
            }
            confirmVerified(
                ttsEngine,
                telemetryLogRepository,
                observePitTimingVirtualEnergyReadoutText,
                observePitTimingVirtualEnergyImminentReadoutText,
                observePitTimingTyreWearReadoutText,
                observePitTimingTyreWearImminentReadoutText,
                checkTextToSpeechAvailable,
                speakText,
            )
        }

    @Test
    fun `空白本文は共通処理で読み上げず空文字とSKIPPEDを保存する`() =
        runTest {
            val json = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = capture(json),
                )
            } just Runs
            createProcessor { " \t\n" }.processRaceFlags(
                raceFlags = raceFlags(playerFlag = PrimaryFlag.BLUE),
                events = listOf(SpeechEvent.BlueFlag()),
                readoutOrder = listOf(ReadoutItemKey.LmuWindows.Flag.Root),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(SpeechEvent.BlueFlag(" \t\n"), queue = false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
                    narratedText = "",
                    narrationOutcome = NarrationOutcome.SKIPPED,
                    telemetryJson = json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `文言解決に失敗しても同じ入力の後続イベントと次回処理を継続する`() =
        runTest {
            val key = ReadoutItemKey.LmuWindows.Flag.Root
            val failedEvent = SpeechEvent.BlueFlag()
            val nextEvent = SpeechEvent.RedFlag()
            val resolvedEvent = SpeechEvent.RedFlag("復旧")
            val skippedJson = slot<String>()
            val spokenJsons = mutableListOf<String>()
            val resolvedEvents = mutableListOf<SpeechEvent>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(resolvedEvent, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    100L,
                    Simulator.LmuWindows,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    capture(skippedJson),
                )
            } just Runs
            listOf(100L, 200L).forEach { time ->
                coEvery {
                    telemetryLogRepository.saveTelemetryLog(
                        time,
                        Simulator.LmuWindows,
                        key,
                        "復旧",
                        NarrationOutcome.SPOKEN,
                        capture(spokenJsons),
                    )
                } just Runs
            }
            val processor =
                createProcessor { event ->
                    resolvedEvents += event
                    if (event == failedEvent) error("preference error")
                    "復旧"
                }

            processor.processRaceFlags(
                raceFlags = raceFlags(PrimaryFlag.BLUE),
                events = listOf(failedEvent, nextEvent),
                readoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 100L,
                logContext = logContext(),
            )
            processor.processRaceFlags(
                raceFlags = raceFlags(PrimaryFlag.GREEN),
                events = listOf(nextEvent),
                readoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )

            assertEquals(listOf<SpeechEvent>(failedEvent, nextEvent, nextEvent), resolvedEvents)
            assertEquals(2, spokenJsons.size)
            assertEquals(
                Json.parseToJsonElement(skippedJson.captured).jsonObject["raceFlags"],
                Json.parseToJsonElement(spokenJsons.last()).jsonObject["previousRaceFlags"],
            )
            verify(exactly = 2) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 2) { ttsEngine.speak(resolvedEvent, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    100L,
                    Simulator.LmuWindows,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    skippedJson.captured,
                )
            }
            listOf(100L, 200L).forEachIndexed { index, time ->
                coVerify(exactly = 1) {
                    telemetryLogRepository.saveTelemetryLog(
                        time,
                        Simulator.LmuWindows,
                        key,
                        "復旧",
                        NarrationOutcome.SPOKEN,
                        spokenJsons[index],
                    )
                }
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `文言解決のキャンセルは再スローしログ保存と後続処理を行わない`() =
        runTest {
            val key = ReadoutItemKey.LmuWindows.Flag.Root
            val event = SpeechEvent.BlueFlag()
            val cancellation = CancellationException("cancelled")
            val resolvedEvents = mutableListOf<SpeechEvent>()
            val processor =
                createProcessor { input ->
                    resolvedEvents += input
                    throw cancellation
                }

            val thrown =
                assertFailsWith<CancellationException> {
                    processor.processRaceFlags(
                        raceFlags = raceFlags(PrimaryFlag.BLUE),
                        events = listOf(event, SpeechEvent.RedFlag()),
                        readoutOrder = listOf(key),
                        queueEnabledStates = emptyMap(),
                        observedAtMs = 100L,
                        logContext = logContext(),
                    )
                }

            assertSame(cancellation, thrown)
            assertEquals(listOf<SpeechEvent>(event), resolvedEvents)
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(event, false) }
            coVerify(exactly = 0) {
                telemetryLogRepository.saveTelemetryLog(
                    100L,
                    Simulator.LmuWindows,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    match { it.isNotEmpty() },
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    private fun createProcessor(readoutText: suspend (SpeechEvent) -> String? = { it.narratedText }) =
        LmuWindowsNarratorEventProcessor(
            ttsEngine = ttsEngine,
            saveTelemetryLog = SaveTelemetryLogUseCase(telemetryLogRepository),
            readoutText = readoutText,
        )
}

private fun logContext() =
    LmuWindowsTelemetryLogContext(
        state = LmuWindowsNarratorState(),
        settings =
            LmuWindowsNarratorReadoutSettings(
                enabledStates = mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.Root to true),
                currentLap = 1,
                skipFirstLap = false,
                vehicleApproachSustainedApproachDurationSeconds = 7,
                tyreTemperatureHighThresholdCelsius = Celsius(95),
                tyreTemperatureLowWarningPhases = emptySet(),
                tyreWearThresholdPercentage = 50,
                brakeTemperatureHighThresholdCelsius = Celsius(700),
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
        lateralDistanceLeftMeters = LateralDistanceMeters(distance),
        lateralDistanceRightMeters = LateralDistanceMeters(Double.MAX_VALUE),
    )

private fun vehicleDamage(overheating: Boolean) =
    LmuWindowsVehicleDamageData(
        overheating = overheating,
        partDetached = false,
        lastImpactMagnitude = 0.0,
    )

private fun tyreDetached(vararg detachedWheels: WheelIndex) =
    LmuWindowsTyreDetachedData(
        wheels = WheelIndex.entries.associateWith { it in detachedWheels },
    )

private fun raceFlags(playerFlag: PrimaryFlag) =
    LmuWindowsRaceFlagsData(
        gamePhase = SessionPhase.GREEN_FLAG,
        yellowFlagState = SessionYellowFlagState.NONE,
        sectorFlags = emptyList(),
        playerFlag = playerFlag,
        playerUnderYellow = false,
    )

private fun tyreWear(frontLeft: Double) =
    LmuWindowsTyreWearData(
        wheels =
            mapOf(
                WheelIndex.FRONT_LEFT to LmuWindowsTyreWearRatio(frontLeft),
                WheelIndex.FRONT_RIGHT to LmuWindowsTyreWearRatio(0.9),
                WheelIndex.REAR_LEFT to LmuWindowsTyreWearRatio(0.9),
                WheelIndex.REAR_RIGHT to LmuWindowsTyreWearRatio(0.9),
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
                currentLap = 1,
                skipFirstLap = false,
                vehicleApproachSustainedApproachDurationSeconds = 7,
                tyreTemperatureHighThresholdCelsius = Celsius(95),
                tyreTemperatureLowWarningPhases = emptySet(),
                tyreWearThresholdPercentage = 50,
                brakeTemperatureHighThresholdCelsius = Celsius(700),
                remainingVirtualEnergyThresholdPercentage = 50,
                pitTimingVirtualEnergyLapsThreshold = 3,
                pitTimingTyreWearLapsThreshold = 3,
            ),
        finalState = LmuWindowsNarratorState(),
    )

private fun pitTimingSnapshot(tyreWear: LmuWindowsTyreWearData) =
    LmuWindowsPitTimingSnapshot(
        telemetry = fakeTelemetryData(),
        virtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = LmuWindowsVirtualEnergyRatio(0.5)),
        tyreWear = tyreWear,
    )

private fun fakeTelemetryData(bestLapTimeMs: Long = 0L) =
    LmuWindowsTelemetryData(
        timestampMs = 0L,
        engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
        inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
        tyres = LmuWindowsTyreData(wheels = emptyMap()),
        fuel = LmuWindowsFuelData(currentLiters = LmuWindowsFuelUnit(0.0), capacityLiters = LmuWindowsFuelUnit(0.0)),
        timing =
            LmuWindowsTimingData(
                currentLapTimeMs = 0L,
                lastLapTimeMs = 0L,
                bestLapTimeMs = bestLapTimeMs,
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

private fun brakeTemperature(frontLeft: Double) =
    LmuWindowsBrakeTemperatureData(
        wheels =
            mapOf(
                WheelIndex.FRONT_LEFT to CelsiusReading(frontLeft.toFloat()),
                WheelIndex.FRONT_RIGHT to CelsiusReading(600f),
                WheelIndex.REAR_LEFT to CelsiusReading(600f),
                WheelIndex.REAR_RIGHT to CelsiusReading(600f),
            ),
    )
