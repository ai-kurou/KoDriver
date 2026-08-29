package kurou.kodriver.feature.acewindowsnarrator

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFlagType
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.FuelPercent
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.AceWindowsNarratorReadoutSettings
import kurou.kodriver.domain.usecase.AceWindowsNarratorState
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("TooManyFunctions")
class AceWindowsNarratorEventProcessorTest {
    @MockK
    private lateinit var telemetryLogRepository: TelemetryLogRepository

    @MockK
    private lateinit var ttsEngine: TextToSpeechEngine

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `直前の燃料データがないイベントはnullとして保存する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val key = ReadoutItemKey.AceWindows.RemainingFuel.Root
            every { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(0L, Simulator.AceWindows, key, capture(telemetryJsons))
            } just Runs

            createProcessor().processRemainingFuel(
                fuel = fuel(20.0),
                events = listOf(SpeechEvent.AceWindowsRemainingFuelWarning),
                readoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
                isOnTrack = true,
            )

            assertEquals(true, telemetryJsons.single().startsWith("{\"state\":{\"raw\":"))
            assertEquals(true, telemetryJsons.single().contains("\"previousFuel\":null"))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(0L, Simulator.AceWindows, key, telemetryJsons.single())
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `読み上げたイベントを直前と現在の燃料データとともに保存する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val processor = createProcessor()
            val key = ReadoutItemKey.AceWindows.RemainingFuel.Root
            every { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(200L, Simulator.AceWindows, key, capture(telemetryJsons))
            } just Runs

            processor.processRemainingFuel(fuel(50.0), emptyList(), emptyList(), emptyMap(), 100L, logContext(), true)
            processor.processRemainingFuel(
                fuel(20.0),
                listOf(SpeechEvent.AceWindowsRemainingFuelWarning),
                listOf(key),
                emptyMap(),
                200L,
                logContext(),
                true,
            )

            assertEquals(1, telemetryJsons.size)
            assertEquals(true, telemetryJsons.single().contains(""""previousFuel":{"remainingPercent":50.0}"""))
            assertEquals(true, telemetryJsons.single().contains(""""fuel":{"remainingPercent":20.0}"""))
            assertEquals(true, telemetryJsons.single().contains(""""observedAtMs":200"""))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(200L, Simulator.AceWindows, key, telemetryJsons.single())
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `優先度の高い項目を再生中なら読み上げも保存もしない`() =
        runTest {
            val currentKey = ReadoutItemKey.AceWindows.RemainingFuel.Root
            val otherKey = ReadoutItemKey.LmuWindows.Flag.Root
            every { ttsEngine.currentReadoutItemKey } returns currentKey
            val processor = createProcessor()

            processor.processRemainingFuel(
                fuel = fuel(20.0),
                events = listOf(SpeechEvent.AceWindowsRemainingFuelWarning),
                readoutOrder = listOf(currentKey, otherKey),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
                isOnTrack = true,
            )

            verify(exactly = 0) { ttsEngine.stop() }
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `優先度で本来無視される項目でもキュー設定が有効ならキュー再生する`() =
        runTest {
            val currentKey = ReadoutItemKey.LmuWindows.Flag.Root
            val key = ReadoutItemKey.AceWindows.RemainingFuel.Root
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, queue = true) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(0L, Simulator.AceWindows, key, capture(telemetryJsons))
            } just Runs
            val processor = createProcessor()

            processor.processRemainingFuel(
                fuel = fuel(20.0),
                events = listOf(SpeechEvent.AceWindowsRemainingFuelWarning),
                readoutOrder = listOf(currentKey, key),
                queueEnabledStates = mapOf(key to true),
                observedAtMs = 0L,
                logContext = logContext(),
                isOnTrack = true,
            )

            verify(exactly = 0) { ttsEngine.stop() }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, queue = true) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(0L, Simulator.AceWindows, key, telemetryJsons.single())
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `優先度の低い項目を再生中なら停止して読み上げる`() =
        runTest {
            val currentKey = ReadoutItemKey.LmuWindows.Flag.Root
            val key = ReadoutItemKey.AceWindows.RemainingFuel.Root
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns currentKey
            every { ttsEngine.stop() } just Runs
            every { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(0L, Simulator.AceWindows, key, capture(telemetryJsons))
            } just Runs
            val processor = createProcessor()

            processor.processRemainingFuel(
                fuel = fuel(20.0),
                events = listOf(SpeechEvent.AceWindowsRemainingFuelWarning),
                readoutOrder = listOf(key, currentKey),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
                isOnTrack = true,
            )

            verify(exactly = 1) { ttsEngine.stop() }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, false) }
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(0L, Simulator.AceWindows, key, telemetryJsons.single())
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `テレメトリログの保存に失敗しても例外を投げない`() =
        runTest {
            every { ttsEngine.currentReadoutItemKey } returns null
            val key = ReadoutItemKey.AceWindows.RemainingFuel.Root
            every { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(0L, Simulator.AceWindows, key, any())
            } throws RuntimeException("db error")

            createProcessor().processRemainingFuel(
                fuel = fuel(20.0),
                events = listOf(SpeechEvent.AceWindowsRemainingFuelWarning),
                readoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
                isOnTrack = true,
            )

            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, false) }
            coVerify(exactly = 1) { telemetryLogRepository.saveTelemetryLog(0L, Simulator.AceWindows, key, any()) }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `燃料残量がNaNでも保存に失敗しない`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val key = ReadoutItemKey.AceWindows.RemainingFuel.Root
            every { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(0L, Simulator.AceWindows, key, capture(telemetryJsons))
            } just Runs

            createProcessor().processRemainingFuel(
                fuel = fuel(Double.NaN),
                events = listOf(SpeechEvent.AceWindowsRemainingFuelWarning),
                readoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
                isOnTrack = true,
            )

            assertEquals(true, telemetryJsons.single().contains(""""fuel":{"remainingPercent":NaN}"""))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(0L, Simulator.AceWindows, key, telemetryJsons.single())
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `isOnTrackがfalseのときは読み上げも保存もしないが直前の燃料データは更新する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val key = ReadoutItemKey.AceWindows.RemainingFuel.Root
            every { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(200L, Simulator.AceWindows, key, capture(telemetryJsons))
            } just Runs
            val processor = createProcessor()

            processor.processRemainingFuel(
                fuel = fuel(20.0),
                events = listOf(SpeechEvent.AceWindowsRemainingFuelWarning),
                readoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 100L,
                logContext = logContext(),
                isOnTrack = false,
            )
            processor.processRemainingFuel(
                fuel = fuel(80.0),
                events = listOf(SpeechEvent.AceWindowsRemainingFuelWarning),
                readoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
                isOnTrack = true,
            )

            assertEquals(true, telemetryJsons.single().contains(""""previousFuel":{"remainingPercent":20.0}"""))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(200L, Simulator.AceWindows, key, telemetryJsons.single())
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `直前の自己ベストラップデータがないイベントはnullとして保存する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val key = ReadoutItemKey.AceWindows.MyBestLap.Root
            every { ttsEngine.speak(SpeechEvent.AceWindowsMyBestLapFormal, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(0L, Simulator.AceWindows, key, capture(telemetryJsons))
            } just Runs

            createProcessor().processMyBestLap(
                bestLapTime = bestLapTime(89_000),
                events = listOf(SpeechEvent.AceWindowsMyBestLapFormal),
                readoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
                isOnTrack = true,
            )

            assertEquals(true, telemetryJsons.single().contains("\"previousBestLapTime\":null"))
            assertEquals(true, telemetryJsons.single().contains(""""bestLapTime":{"bestLapTimeMs":89000}"""))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsMyBestLapFormal, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(0L, Simulator.AceWindows, key, telemetryJsons.single())
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `読み上げた自己ベストラップイベントを直前と現在のベストラップデータとともに保存する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val processor = createProcessor()
            val key = ReadoutItemKey.AceWindows.MyBestLap.Root
            every { ttsEngine.speak(SpeechEvent.AceWindowsMyBestLapFormal, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(200L, Simulator.AceWindows, key, capture(telemetryJsons))
            } just Runs

            processor.processMyBestLap(
                bestLapTime(90_000),
                emptyList(),
                emptyList(),
                emptyMap(),
                100L,
                logContext(),
                true,
            )
            processor.processMyBestLap(
                bestLapTime(89_000),
                listOf(SpeechEvent.AceWindowsMyBestLapFormal),
                listOf(key),
                emptyMap(),
                200L,
                logContext(),
                true,
            )

            assertEquals(1, telemetryJsons.size)
            assertEquals(true, telemetryJsons.single().contains(""""previousBestLapTime":{"bestLapTimeMs":90000}"""))
            assertEquals(true, telemetryJsons.single().contains(""""bestLapTime":{"bestLapTimeMs":89000}"""))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsMyBestLapFormal, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(200L, Simulator.AceWindows, key, telemetryJsons.single())
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `直前のフラグデータがないイベントはnullとして保存する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val key = ReadoutItemKey.AceWindows.Flag.Root
            every { ttsEngine.speak(SpeechEvent.AceWindowsBlueFlag, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(0L, Simulator.AceWindows, key, capture(telemetryJsons))
            } just Runs

            createProcessor().processFlag(
                flag = flag(AceWindowsFlagType.BLUE_FLAG),
                events = listOf(SpeechEvent.AceWindowsBlueFlag),
                readoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
                isOnTrack = true,
            )

            assertEquals(true, telemetryJsons.single().contains("\"previousFlag\":null"))
            assertEquals(true, telemetryJsons.single().contains(""""flag":{"flag":"BLUE_FLAG"}"""))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsBlueFlag, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(0L, Simulator.AceWindows, key, telemetryJsons.single())
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `読み上げたフラグイベントを直前と現在のフラグデータとともに保存する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val processor = createProcessor()
            val key = ReadoutItemKey.AceWindows.Flag.Root
            every { ttsEngine.speak(SpeechEvent.AceWindowsBlueFlag, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(200L, Simulator.AceWindows, key, capture(telemetryJsons))
            } just Runs

            processor.processFlag(
                flag(AceWindowsFlagType.NO_FLAG),
                emptyList(),
                emptyList(),
                emptyMap(),
                100L,
                logContext(),
                true,
            )
            processor.processFlag(
                flag(AceWindowsFlagType.BLUE_FLAG),
                listOf(SpeechEvent.AceWindowsBlueFlag),
                listOf(key),
                emptyMap(),
                200L,
                logContext(),
                true,
            )

            assertEquals(1, telemetryJsons.size)
            assertEquals(true, telemetryJsons.single().contains(""""previousFlag":{"flag":"NO_FLAG"}"""))
            assertEquals(true, telemetryJsons.single().contains(""""flag":{"flag":"BLUE_FLAG"}"""))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsBlueFlag, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(200L, Simulator.AceWindows, key, telemetryJsons.single())
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `直前のタイヤカーカス温度データがないイベントはnullとして保存する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val key = ReadoutItemKey.AceWindows.TyreTemperature.Root
            every { ttsEngine.speak(SpeechEvent.AceWindowsTyreOverheat, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(0L, Simulator.AceWindows, key, capture(telemetryJsons))
            } just Runs

            createProcessor().processTyreTemperature(
                tyreCarcassTemperature = tyreCarcassTemperature(110.0f),
                events = listOf(SpeechEvent.AceWindowsTyreOverheat),
                readoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
                isOnTrack = true,
            )

            assertEquals(true, telemetryJsons.single().contains("\"previousTyreCarcassTemperature\":null"))
            assertEquals(
                true,
                telemetryJsons.single().contains(""""tyreCarcassTemperature":{"wheels":{"FRONT_LEFT":110.0}}"""),
            )
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsTyreOverheat, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(0L, Simulator.AceWindows, key, telemetryJsons.single())
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `読み上げたタイヤカーカス温度イベントを直前と現在のタイヤカーカス温度データとともに保存する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val processor = createProcessor()
            val key = ReadoutItemKey.AceWindows.TyreTemperature.Root
            every { ttsEngine.speak(SpeechEvent.AceWindowsTyreOverheat, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(200L, Simulator.AceWindows, key, capture(telemetryJsons))
            } just Runs

            processor.processTyreTemperature(
                tyreCarcassTemperature(90.0f),
                emptyList(),
                emptyList(),
                emptyMap(),
                100L,
                logContext(),
                true,
            )
            processor.processTyreTemperature(
                tyreCarcassTemperature(110.0f),
                listOf(SpeechEvent.AceWindowsTyreOverheat),
                listOf(key),
                emptyMap(),
                200L,
                logContext(),
                true,
            )

            assertEquals(1, telemetryJsons.size)
            assertEquals(
                true,
                telemetryJsons.single().contains(""""previousTyreCarcassTemperature":{"wheels":{"FRONT_LEFT":90.0}}"""),
            )
            assertEquals(
                true,
                telemetryJsons.single().contains(""""tyreCarcassTemperature":{"wheels":{"FRONT_LEFT":110.0}}"""),
            )
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsTyreOverheat, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(200L, Simulator.AceWindows, key, telemetryJsons.single())
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    private fun tyreCarcassTemperature(frontLeftCelsius: Float) =
        AceWindowsTyreCarcassTemperatureData(wheels = mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(frontLeftCelsius)))

    private fun flag(flagType: AceWindowsFlagType) = AceWindowsFlagData(flag = flagType)

    private fun bestLapTime(bestLapTimeMs: Int) = AceWindowsBestLapTimeData(bestLapTimeMs = bestLapTimeMs)

    private fun createProcessor() =
        AceWindowsNarratorEventProcessor(
            ttsEngine = ttsEngine,
            saveTelemetryLog = SaveTelemetryLogUseCase(telemetryLogRepository),
        )

    private fun fuel(remainingPercent: Double) = AceWindowsFuelData(remainingPercent = FuelPercent(remainingPercent))

    private fun logContext() =
        AceWindowsTelemetryLogContext(
            state = AceWindowsNarratorState(),
            settings =
                AceWindowsNarratorReadoutSettings(
                    enabledStates = emptyMap(),
                    remainingFuelThresholdPercentage = 0,
                ),
            finalState = AceWindowsNarratorState(),
        )
}
