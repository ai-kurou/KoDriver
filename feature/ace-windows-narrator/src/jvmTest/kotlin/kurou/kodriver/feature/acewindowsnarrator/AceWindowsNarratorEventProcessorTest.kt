@file:Suppress("TooManyFunctions")

package kurou.kodriver.feature.acewindowsnarrator

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
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFlagType
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsNearbyVehicleData
import kurou.kodriver.domain.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.FuelPercent
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.AceWindowsNarratorReadoutSettings
import kurou.kodriver.domain.usecase.AceWindowsNarratorState
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

@Suppress("TooManyFunctions")
class AceWindowsNarratorEventProcessorTest {
    private val telemetryLogRepository: TelemetryLogRepository = mockk()

    private val ttsEngine: TextToSpeechEngine = mockk()

    @Test
    fun `直前の燃料データがないイベントはnullとして保存する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val key = ReadoutItemKey.AceWindows.RemainingFuel.Root
            every { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "残り燃料警告",
                    NarrationOutcome.SPOKEN,
                    capture(telemetryJsons),
                )
            } just Runs

            createProcessor().processRemainingFuel(
                fuel = fuel(20.0),
                events = listOf(SpeechEvent.AceWindowsRemainingFuelWarning),
                readoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )

            assertEquals(true, telemetryJsons.single().startsWith("{\"state\":{\"raw\":"))
            assertEquals(true, telemetryJsons.single().contains("\"previousFuel\":null"))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "残り燃料警告",
                    NarrationOutcome.SPOKEN,
                    telemetryJsons.single(),
                )
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
                telemetryLogRepository.saveTelemetryLog(
                    200L,
                    Simulator.AceWindows,
                    key,
                    "残り燃料警告",
                    NarrationOutcome.SPOKEN,
                    capture(telemetryJsons),
                )
            } just Runs

            processor.processRemainingFuel(fuel(50.0), emptyList(), emptyList(), emptyMap(), 100L, logContext())
            processor.processRemainingFuel(
                fuel(20.0),
                listOf(SpeechEvent.AceWindowsRemainingFuelWarning),
                listOf(key),
                emptyMap(),
                200L,
                logContext(),
            )

            assertEquals(1, telemetryJsons.size)
            assertEquals(true, telemetryJsons.single().contains(""""previousFuel":{"remainingPercent":50.0}"""))
            assertEquals(true, telemetryJsons.single().contains(""""fuel":{"remainingPercent":20.0}"""))
            assertEquals(true, telemetryJsons.single().contains(""""observedAtMs":200"""))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    200L,
                    Simulator.AceWindows,
                    key,
                    "残り燃料警告",
                    NarrationOutcome.SPOKEN,
                    telemetryJsons.single(),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `優先度の高い項目を再生中なら読み上げずSKIPPEDとして保存する`() =
        runTest {
            val currentKey = ReadoutItemKey.AceWindows.RemainingFuel.Root
            val otherKey = ReadoutItemKey.LmuWindows.Flag.Root
            every { ttsEngine.currentReadoutItemKey } returns currentKey
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    currentKey,
                    "残り燃料警告",
                    NarrationOutcome.SKIPPED,
                    any(),
                )
            } just Runs
            val processor = createProcessor()

            processor.processRemainingFuel(
                fuel = fuel(20.0),
                events = listOf(SpeechEvent.AceWindowsRemainingFuelWarning),
                readoutOrder = listOf(currentKey, otherKey),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )

            verify(exactly = 0) { ttsEngine.stop() }
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    currentKey,
                    "残り燃料警告",
                    NarrationOutcome.SKIPPED,
                    any(),
                )
            }
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
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "残り燃料警告",
                    NarrationOutcome.QUEUED,
                    capture(telemetryJsons),
                )
            } just Runs
            val processor = createProcessor()

            processor.processRemainingFuel(
                fuel = fuel(20.0),
                events = listOf(SpeechEvent.AceWindowsRemainingFuelWarning),
                readoutOrder = listOf(currentKey, key),
                queueEnabledStates = mapOf(key to true),
                observedAtMs = 0L,
                logContext = logContext(),
            )

            verify(exactly = 0) { ttsEngine.stop() }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, queue = true) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "残り燃料警告",
                    NarrationOutcome.QUEUED,
                    telemetryJsons.single(),
                )
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
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "残り燃料警告",
                    NarrationOutcome.INTERRUPTED,
                    capture(telemetryJsons),
                )
            } just Runs
            val processor = createProcessor()

            processor.processRemainingFuel(
                fuel = fuel(20.0),
                events = listOf(SpeechEvent.AceWindowsRemainingFuelWarning),
                readoutOrder = listOf(key, currentKey),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )

            verify(exactly = 1) { ttsEngine.stop() }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, false) }
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "残り燃料警告",
                    NarrationOutcome.INTERRUPTED,
                    telemetryJsons.single(),
                )
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
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "残り燃料警告",
                    NarrationOutcome.SPOKEN,
                    any(),
                )
            } throws RuntimeException("db error")

            createProcessor().processRemainingFuel(
                fuel = fuel(20.0),
                events = listOf(SpeechEvent.AceWindowsRemainingFuelWarning),
                readoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )

            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, false) }
            coVerify(
                exactly = 1,
            ) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "残り燃料警告",
                    NarrationOutcome.SPOKEN,
                    any(),
                )
            }
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
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "残り燃料警告",
                    NarrationOutcome.SPOKEN,
                    capture(telemetryJsons),
                )
            } just Runs

            createProcessor().processRemainingFuel(
                fuel = fuel(Double.NaN),
                events = listOf(SpeechEvent.AceWindowsRemainingFuelWarning),
                readoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )

            assertEquals(true, telemetryJsons.single().contains(""""fuel":{"remainingPercent":NaN}"""))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "残り燃料警告",
                    NarrationOutcome.SPOKEN,
                    telemetryJsons.single(),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `イベントがないときは読み上げも保存もしないが直前の燃料データは更新する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val key = ReadoutItemKey.AceWindows.RemainingFuel.Root
            every { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    200L,
                    Simulator.AceWindows,
                    key,
                    "残り燃料警告",
                    NarrationOutcome.SPOKEN,
                    capture(telemetryJsons),
                )
            } just Runs
            val processor = createProcessor()

            processor.processRemainingFuel(
                fuel = fuel(20.0),
                events = emptyList(),
                readoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 100L,
                logContext = logContext(),
            )
            processor.processRemainingFuel(
                fuel = fuel(80.0),
                events = listOf(SpeechEvent.AceWindowsRemainingFuelWarning),
                readoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )

            assertEquals(true, telemetryJsons.single().contains(""""previousFuel":{"remainingPercent":20.0}"""))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelWarning, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    200L,
                    Simulator.AceWindows,
                    key,
                    "残り燃料警告",
                    NarrationOutcome.SPOKEN,
                    telemetryJsons.single(),
                )
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
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "自己ベストラップ更新",
                    NarrationOutcome.SPOKEN,
                    capture(telemetryJsons),
                )
            } just Runs

            createProcessor().processMyBestLap(
                bestLapTime = bestLapTime(89_000),
                events = listOf(SpeechEvent.AceWindowsMyBestLapFormal),
                readoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )

            assertEquals(true, telemetryJsons.single().contains("\"previousBestLapTime\":null"))
            assertEquals(true, telemetryJsons.single().contains(""""bestLapTime":{"bestLapTimeMs":89000}"""))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsMyBestLapFormal, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "自己ベストラップ更新",
                    NarrationOutcome.SPOKEN,
                    telemetryJsons.single(),
                )
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
                telemetryLogRepository.saveTelemetryLog(
                    200L,
                    Simulator.AceWindows,
                    key,
                    "自己ベストラップ更新",
                    NarrationOutcome.SPOKEN,
                    capture(telemetryJsons),
                )
            } just Runs

            processor.processMyBestLap(
                bestLapTime(90_000),
                emptyList(),
                emptyList(),
                emptyMap(),
                100L,
                logContext(),
            )
            processor.processMyBestLap(
                bestLapTime(89_000),
                listOf(SpeechEvent.AceWindowsMyBestLapFormal),
                listOf(key),
                emptyMap(),
                200L,
                logContext(),
            )

            assertEquals(1, telemetryJsons.size)
            assertEquals(true, telemetryJsons.single().contains(""""previousBestLapTime":{"bestLapTimeMs":90000}"""))
            assertEquals(true, telemetryJsons.single().contains(""""bestLapTime":{"bestLapTimeMs":89000}"""))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsMyBestLapFormal, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    200L,
                    Simulator.AceWindows,
                    key,
                    "自己ベストラップ更新",
                    NarrationOutcome.SPOKEN,
                    telemetryJsons.single(),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `直前の燃料残り周回数データがないイベントはnullとして保存する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val key = ReadoutItemKey.AceWindows.RemainingFuelLaps.Root
            every { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelLapsWarning(2), false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "燃料は残り約2周",
                    NarrationOutcome.SPOKEN,
                    capture(telemetryJsons),
                )
            } just Runs

            createProcessor().processRemainingFuelLaps(
                remainingFuelLaps = remainingFuelLaps(2.5f),
                events = listOf(SpeechEvent.AceWindowsRemainingFuelLapsWarning(2)),
                readoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )

            assertEquals(true, telemetryJsons.single().contains("\"previousRemainingFuelLaps\":null"))
            assertEquals(true, telemetryJsons.single().contains(""""remainingFuelLaps":{"remainingLaps":2.5}"""))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelLapsWarning(2), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "燃料は残り約2周",
                    NarrationOutcome.SPOKEN,
                    telemetryJsons.single(),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `読み上げた燃料残り周回数イベントを直前と現在の燃料残り周回数データとともに保存する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val processor = createProcessor()
            val key = ReadoutItemKey.AceWindows.RemainingFuelLaps.Root
            every { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelLapsWarning(2), false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    200L,
                    Simulator.AceWindows,
                    key,
                    "燃料は残り約2周",
                    NarrationOutcome.SPOKEN,
                    capture(telemetryJsons),
                )
            } just Runs

            processor.processRemainingFuelLaps(
                remainingFuelLaps(3.5f),
                emptyList(),
                emptyList(),
                emptyMap(),
                100L,
                logContext(),
            )
            processor.processRemainingFuelLaps(
                remainingFuelLaps(2.5f),
                listOf(SpeechEvent.AceWindowsRemainingFuelLapsWarning(2)),
                listOf(key),
                emptyMap(),
                200L,
                logContext(),
            )

            assertEquals(1, telemetryJsons.size)
            assertEquals(
                true,
                telemetryJsons.single().contains(""""previousRemainingFuelLaps":{"remainingLaps":3.5}"""),
            )
            assertEquals(true, telemetryJsons.single().contains(""""remainingFuelLaps":{"remainingLaps":2.5}"""))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsRemainingFuelLapsWarning(2), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    200L,
                    Simulator.AceWindows,
                    key,
                    "燃料は残り約2周",
                    NarrationOutcome.SPOKEN,
                    telemetryJsons.single(),
                )
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
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "ブルーフラッグ",
                    NarrationOutcome.SPOKEN,
                    capture(telemetryJsons),
                )
            } just Runs

            createProcessor().processFlag(
                flag = flag(AceWindowsFlagType.BLUE_FLAG),
                events = listOf(SpeechEvent.AceWindowsBlueFlag),
                readoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )

            assertEquals(true, telemetryJsons.single().contains("\"previousFlag\":null"))
            assertEquals(true, telemetryJsons.single().contains(""""flag":{"flag":"BLUE_FLAG"}"""))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsBlueFlag, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "ブルーフラッグ",
                    NarrationOutcome.SPOKEN,
                    telemetryJsons.single(),
                )
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
                telemetryLogRepository.saveTelemetryLog(
                    200L,
                    Simulator.AceWindows,
                    key,
                    "ブルーフラッグ",
                    NarrationOutcome.SPOKEN,
                    capture(telemetryJsons),
                )
            } just Runs

            processor.processFlag(
                flag(AceWindowsFlagType.NO_FLAG),
                emptyList(),
                emptyList(),
                emptyMap(),
                100L,
                logContext(),
            )
            processor.processFlag(
                flag(AceWindowsFlagType.BLUE_FLAG),
                listOf(SpeechEvent.AceWindowsBlueFlag),
                listOf(key),
                emptyMap(),
                200L,
                logContext(),
            )

            assertEquals(1, telemetryJsons.size)
            assertEquals(true, telemetryJsons.single().contains(""""previousFlag":{"flag":"NO_FLAG"}"""))
            assertEquals(true, telemetryJsons.single().contains(""""flag":{"flag":"BLUE_FLAG"}"""))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsBlueFlag, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    200L,
                    Simulator.AceWindows,
                    key,
                    "ブルーフラッグ",
                    NarrationOutcome.SPOKEN,
                    telemetryJsons.single(),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `直前のタイヤカーカス温度データがないイベントはnullとして保存する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val key = ReadoutItemKey.AceWindows.TyreTemperature.Root
            every { ttsEngine.speak(SpeechEvent.AceWindowsTyreOverheat(110, "タイヤ過熱 110度"), false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "タイヤ過熱 110度",
                    NarrationOutcome.SPOKEN,
                    capture(telemetryJsons),
                )
            } just Runs

            createProcessor().processTyreTemperature(
                tyreCarcassTemperature = tyreCarcassTemperature(110.0f),
                events = listOf(SpeechEvent.AceWindowsTyreOverheat(110)),
                readoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext(),
            )

            assertEquals(true, telemetryJsons.single().contains("\"previousTyreCarcassTemperature\":null"))
            assertEquals(
                true,
                telemetryJsons.single().contains(""""tyreCarcassTemperature":{"wheels":{"FRONT_LEFT":110.0}}"""),
            )
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsTyreOverheat(110, "タイヤ過熱 110度"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "タイヤ過熱 110度",
                    NarrationOutcome.SPOKEN,
                    telemetryJsons.single(),
                )
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
            every { ttsEngine.speak(SpeechEvent.AceWindowsTyreOverheat(110, "タイヤ過熱 110度"), false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    200L,
                    Simulator.AceWindows,
                    key,
                    "タイヤ過熱 110度",
                    NarrationOutcome.SPOKEN,
                    capture(telemetryJsons),
                )
            } just Runs

            processor.processTyreTemperature(
                tyreCarcassTemperature(90.0f),
                emptyList(),
                emptyList(),
                emptyMap(),
                100L,
                logContext(),
            )
            processor.processTyreTemperature(
                tyreCarcassTemperature(110.0f),
                listOf(SpeechEvent.AceWindowsTyreOverheat(110)),
                listOf(key),
                emptyMap(),
                200L,
                logContext(),
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
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.AceWindowsTyreOverheat(110, "タイヤ過熱 110度"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    200L,
                    Simulator.AceWindows,
                    key,
                    "タイヤ過熱 110度",
                    NarrationOutcome.SPOKEN,
                    telemetryJsons.single(),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `自由文言10種の保存文言を読み上げログに記録する`() =
        runTest {
            val events =
                listOf(
                    SpeechEvent.AceWindowsCheckeredFlag,
                    SpeechEvent.AceWindowsWhiteFlag,
                    SpeechEvent.AceWindowsGreenFlag,
                    SpeechEvent.AceWindowsRedFlag,
                    SpeechEvent.AceWindowsBlueFlag,
                    SpeechEvent.AceWindowsYellowFlag,
                    SpeechEvent.AceWindowsBlackFlag,
                    SpeechEvent.AceWindowsBlackWhiteFlag,
                    SpeechEvent.AceWindowsOrangeCircleFlag,
                    SpeechEvent.AceWindowsRedYellowStripesFlag,
                )
            val readouts = mutableListOf<SpeechEvent>()
            val processor =
                AceWindowsNarratorEventProcessor(ttsEngine, SaveTelemetryLogUseCase(telemetryLogRepository)) { event ->
                    readouts += event
                    "完走"
                }
            every { ttsEngine.currentReadoutItemKey } returns null
            events.forEach { event ->
                val jsons = mutableListOf<String>()
                val text: String? = "完走"
                every { ttsEngine.speak(event, false) } just Runs
                coEvery {
                    telemetryLogRepository.saveTelemetryLog(
                        0L,
                        Simulator.AceWindows,
                        event.readoutItemKey,
                        text.orEmpty(),
                        NarrationOutcome.SPOKEN,
                        capture(jsons),
                    )
                } just Runs
                processor.processFlag(
                    flag(
                        when (event) {
                            SpeechEvent.AceWindowsCheckeredFlag -> AceWindowsFlagType.CHECKERED_FLAG
                            SpeechEvent.AceWindowsWhiteFlag -> AceWindowsFlagType.WHITE_FLAG
                            SpeechEvent.AceWindowsGreenFlag -> AceWindowsFlagType.GREEN_FLAG
                            SpeechEvent.AceWindowsRedFlag -> AceWindowsFlagType.RED_FLAG
                            SpeechEvent.AceWindowsBlueFlag -> AceWindowsFlagType.BLUE_FLAG
                            SpeechEvent.AceWindowsYellowFlag -> AceWindowsFlagType.YELLOW_FLAG
                            SpeechEvent.AceWindowsBlackFlag -> AceWindowsFlagType.BLACK_FLAG
                            SpeechEvent.AceWindowsBlackWhiteFlag -> AceWindowsFlagType.BLACK_WHITE_FLAG
                            SpeechEvent.AceWindowsOrangeCircleFlag -> AceWindowsFlagType.ORANGE_CIRCLE_FLAG
                            SpeechEvent.AceWindowsRedYellowStripesFlag -> AceWindowsFlagType.RED_YELLOW_STRIPES_FLAG
                            else -> error("Unexpected flag event")
                        },
                    ),
                    listOf(event),
                    listOf(event.readoutItemKey),
                    emptyMap(),
                    0L,
                    logContext(),
                )
                verify(exactly = 1) { ttsEngine.speak(event, false) }
                coVerify(exactly = 1) {
                    telemetryLogRepository.saveTelemetryLog(
                        0L,
                        Simulator.AceWindows,
                        event.readoutItemKey,
                        text.orEmpty(),
                        NarrationOutcome.SPOKEN,
                        jsons.single(),
                    )
                }
            }
            assertEquals(events, readouts)
            verify(exactly = events.size) { ttsEngine.currentReadoutItemKey }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `自由文言10種の文言がnullなら空文字とSKIPPEDを記録する`() =
        runTest {
            val events =
                listOf(
                    SpeechEvent.AceWindowsCheckeredFlag,
                    SpeechEvent.AceWindowsWhiteFlag,
                    SpeechEvent.AceWindowsGreenFlag,
                    SpeechEvent.AceWindowsRedFlag,
                    SpeechEvent.AceWindowsBlueFlag,
                    SpeechEvent.AceWindowsYellowFlag,
                    SpeechEvent.AceWindowsBlackFlag,
                    SpeechEvent.AceWindowsBlackWhiteFlag,
                    SpeechEvent.AceWindowsOrangeCircleFlag,
                    SpeechEvent.AceWindowsRedYellowStripesFlag,
                )
            val readouts = mutableListOf<SpeechEvent>()
            val processor =
                AceWindowsNarratorEventProcessor(ttsEngine, SaveTelemetryLogUseCase(telemetryLogRepository)) { event ->
                    readouts += event
                    null
                }

            events.forEach { event ->
                val jsons = mutableListOf<String>()
                val text: String? = null

                coEvery {
                    telemetryLogRepository.saveTelemetryLog(
                        0L,
                        Simulator.AceWindows,
                        event.readoutItemKey,
                        text.orEmpty(),
                        NarrationOutcome.SKIPPED,
                        capture(jsons),
                    )
                } just Runs
                processor.processFlag(
                    flag(
                        when (event) {
                            SpeechEvent.AceWindowsCheckeredFlag -> AceWindowsFlagType.CHECKERED_FLAG
                            SpeechEvent.AceWindowsWhiteFlag -> AceWindowsFlagType.WHITE_FLAG
                            SpeechEvent.AceWindowsGreenFlag -> AceWindowsFlagType.GREEN_FLAG
                            SpeechEvent.AceWindowsRedFlag -> AceWindowsFlagType.RED_FLAG
                            SpeechEvent.AceWindowsBlueFlag -> AceWindowsFlagType.BLUE_FLAG
                            SpeechEvent.AceWindowsYellowFlag -> AceWindowsFlagType.YELLOW_FLAG
                            SpeechEvent.AceWindowsBlackFlag -> AceWindowsFlagType.BLACK_FLAG
                            SpeechEvent.AceWindowsBlackWhiteFlag -> AceWindowsFlagType.BLACK_WHITE_FLAG
                            SpeechEvent.AceWindowsOrangeCircleFlag -> AceWindowsFlagType.ORANGE_CIRCLE_FLAG
                            SpeechEvent.AceWindowsRedYellowStripesFlag -> AceWindowsFlagType.RED_YELLOW_STRIPES_FLAG
                            else -> error("Unexpected flag event")
                        },
                    ),
                    listOf(event),
                    listOf(event.readoutItemKey),
                    emptyMap(),
                    0L,
                    logContext(),
                )
                verify(exactly = 0) { ttsEngine.speak(event, false) }
                coVerify(exactly = 1) {
                    telemetryLogRepository.saveTelemetryLog(
                        0L,
                        Simulator.AceWindows,
                        event.readoutItemKey,
                        text.orEmpty(),
                        NarrationOutcome.SKIPPED,
                        jsons.single(),
                    )
                }
            }
            assertEquals(events, readouts)
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `自由文言の文言が空白なら読み上げず空文字とSKIPPEDを記録する`() =
        runTest {
            val event = SpeechEvent.AceWindowsGreenFlag
            val jsons = mutableListOf<String>()
            val processor =
                AceWindowsNarratorEventProcessor(ttsEngine, SaveTelemetryLogUseCase(telemetryLogRepository)) { "  " }
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    event.readoutItemKey,
                    "",
                    NarrationOutcome.SKIPPED,
                    capture(jsons),
                )
            } just Runs

            processor.processFlag(
                flag(AceWindowsFlagType.GREEN_FLAG),
                listOf(event),
                listOf(event.readoutItemKey),
                emptyMap(),
                0L,
                logContext(),
            )

            verify(exactly = 0) { ttsEngine.speak(event, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    event.readoutItemKey,
                    "",
                    NarrationOutcome.SKIPPED,
                    jsons.single(),
                )
            }
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    private fun tyreCarcassTemperature(frontLeftCelsius: Float) =
        AceWindowsTyreCarcassTemperatureData(wheels = mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(frontLeftCelsius)))

    private fun flag(flagType: AceWindowsFlagType) = AceWindowsFlagData(flag = flagType)

    private fun bestLapTime(bestLapTimeMs: Int) = AceWindowsBestLapTimeData(bestLapTimeMs = bestLapTimeMs)

    @Test
    fun `文言解決に失敗しても同じ入力の後続イベントと次回処理を継続する`() =
        runTest {
            val key = ReadoutItemKey.AceWindows.Flag.Root
            val failedEvent = SpeechEvent.AceWindowsBlueFlag
            val nextEvent = SpeechEvent.AceWindowsRedFlag
            val resolvedEvent = SpeechEvent.AceWindowsRedFlag
            val skippedJson = slot<String>()
            val spokenJsons = mutableListOf<String>()
            val resolvedEvents = mutableListOf<SpeechEvent>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(resolvedEvent, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    100L,
                    Simulator.AceWindows,
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
                        Simulator.AceWindows,
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

            processor.processFlag(
                flag = flag(AceWindowsFlagType.BLUE_FLAG),
                events = listOf(failedEvent, nextEvent),
                readoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 100L,
                logContext = logContext(),
            )
            processor.processFlag(
                flag = flag(AceWindowsFlagType.RED_FLAG),
                events = listOf(nextEvent),
                readoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext(),
            )

            assertEquals(listOf<SpeechEvent>(failedEvent, nextEvent, nextEvent), resolvedEvents)
            assertEquals(2, spokenJsons.size)
            assertEquals(
                Json.parseToJsonElement(skippedJson.captured).jsonObject["flag"],
                Json.parseToJsonElement(spokenJsons.last()).jsonObject["previousFlag"],
            )
            verify(exactly = 2) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 2) { ttsEngine.speak(resolvedEvent, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    100L,
                    Simulator.AceWindows,
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
                        Simulator.AceWindows,
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
            val key = ReadoutItemKey.AceWindows.Flag.Root
            val event = SpeechEvent.AceWindowsBlueFlag
            val cancellation = CancellationException("cancelled")
            val resolvedEvents = mutableListOf<SpeechEvent>()
            val processor =
                createProcessor { input ->
                    resolvedEvents += input
                    throw cancellation
                }

            val thrown =
                assertFailsWith<CancellationException> {
                    processor.processFlag(
                        flag = flag(AceWindowsFlagType.BLUE_FLAG),
                        events = listOf(event, SpeechEvent.AceWindowsRedFlag),
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
                    Simulator.AceWindows,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    match { it.isNotEmpty() },
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `車両接近は保存文言をRootのログに記録する`() =
        runTest {
            val event = SpeechEvent.AceWindowsVehicleApproach
            val key = ReadoutItemKey.AceWindows.VehicleApproach.Root
            val json = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(event, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "周囲に注意",
                    NarrationOutcome.SPOKEN,
                    capture(json),
                )
            } just Runs
            val resolvedEvents = mutableListOf<SpeechEvent>()
            val processor =
                createProcessor {
                    resolvedEvents += it
                    "周囲に注意"
                }
            processor.processVehicleApproach(
                AceWindowsVehicleApproachData(nearbyVehicles = listOf(AceWindowsNearbyVehicleData(5.0))),
                listOf(event),
                listOf(key),
                emptyMap(),
                0L,
                logContext(),
            )
            assertEquals(listOf<SpeechEvent>(event), resolvedEvents)
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(event, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "周囲に注意",
                    NarrationOutcome.SPOKEN,
                    json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `車両接近は空白またはTTS不可の解決結果なら開始音を要求せず空文字でSKIPPEDを記録する`() =
        runTest {
            val event = SpeechEvent.AceWindowsVehicleApproach
            val key = ReadoutItemKey.AceWindows.VehicleApproach.Root
            val json = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    capture(json),
                )
            } just Runs
            val resolvedEvents = mutableListOf<SpeechEvent>()
            val processor =
                createProcessor {
                    resolvedEvents += it
                    null
                }
            processor.processVehicleApproach(
                AceWindowsVehicleApproachData(nearbyVehicles = listOf(AceWindowsNearbyVehicleData(5.0))),
                listOf(event),
                listOf(key),
                emptyMap(),
                0L,
                logContext(),
            )
            assertEquals(listOf<SpeechEvent>(event), resolvedEvents)
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(event, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `車両接近は文言解決に失敗しても空文字でSKIPPEDを記録する`() =
        runTest {
            val event = SpeechEvent.AceWindowsVehicleApproach
            val key = ReadoutItemKey.AceWindows.VehicleApproach.Root
            val json = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    capture(json),
                )
            } just Runs
            val resolvedEvents = mutableListOf<SpeechEvent>()
            val processor =
                createProcessor {
                    resolvedEvents += it
                    error("preference error")
                }
            processor.processVehicleApproach(
                AceWindowsVehicleApproachData(nearbyVehicles = listOf(AceWindowsNearbyVehicleData(5.0))),
                listOf(event),
                listOf(key),
                emptyMap(),
                0L,
                logContext(),
            )
            assertEquals(listOf<SpeechEvent>(event), resolvedEvents)
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(event, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `車両接近の文言解決キャンセルは読み上げとログを要求せず伝播する`() =
        runTest {
            val processor = createProcessor { throw CancellationException("cancelled") }
            assertFailsWith<CancellationException> {
                processor.processVehicleApproach(
                    AceWindowsVehicleApproachData(nearbyVehicles = emptyList()),
                    listOf(SpeechEvent.AceWindowsVehicleApproach),
                    listOf(ReadoutItemKey.AceWindows.VehicleApproach.Root),
                    emptyMap(),
                    0L,
                    logContext(),
                )
            }
            verify(exactly = 0) { ttsEngine.speak(SpeechEvent.AceWindowsVehicleApproach, false) }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `タイヤ過熱は保存文言をRootのログに記録する`() =
        runTest {
            val event = SpeechEvent.AceWindowsTyreOverheat(110)
            val key = ReadoutItemKey.AceWindows.TyreTemperature.Root
            val json = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(event.withResolvedText("過熱 110度"), false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "過熱 110度",
                    NarrationOutcome.SPOKEN,
                    capture(json),
                )
            } just Runs
            val resolvedEvents = mutableListOf<SpeechEvent>()
            val processor =
                createProcessor {
                    resolvedEvents += it
                    "過熱 110度"
                }
            processor.processTyreTemperature(
                tyreCarcassTemperature(110.0f),
                listOf(event),
                listOf(key),
                emptyMap(),
                0L,
                logContext(),
            )
            assertEquals(listOf<SpeechEvent>(event), resolvedEvents)
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(event.withResolvedText("過熱 110度"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "過熱 110度",
                    NarrationOutcome.SPOKEN,
                    json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `タイヤ過熱はTTS不可の解決結果なら開始音を要求せず空文字でSKIPPEDを記録する`() =
        runTest {
            val event = SpeechEvent.AceWindowsTyreOverheat(110)
            val key = ReadoutItemKey.AceWindows.TyreTemperature.Root
            val json = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    capture(json),
                )
            } just Runs
            val resolvedEvents = mutableListOf<SpeechEvent>()
            val processor =
                createProcessor {
                    resolvedEvents += it
                    null
                }
            processor.processTyreTemperature(
                tyreCarcassTemperature(110.0f),
                listOf(event),
                listOf(key),
                emptyMap(),
                0L,
                logContext(),
            )
            assertEquals(listOf<SpeechEvent>(event), resolvedEvents)
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(event.withResolvedText("過熱 110度"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `タイヤ過熱は空白の解決結果なら開始音を要求せず空文字でSKIPPEDを記録する`() =
        runTest {
            val event = SpeechEvent.AceWindowsTyreOverheat(110)
            val key = ReadoutItemKey.AceWindows.TyreTemperature.Root
            val json = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    capture(json),
                )
            } just Runs
            val resolvedEvents = mutableListOf<SpeechEvent>()
            val processor =
                createProcessor {
                    resolvedEvents += it
                    " "
                }
            processor.processTyreTemperature(
                tyreCarcassTemperature(110.0f),
                listOf(event),
                listOf(key),
                emptyMap(),
                0L,
                logContext(),
            )
            assertEquals(listOf<SpeechEvent>(event), resolvedEvents)
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(event.withResolvedText("過熱 110度"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `タイヤ過熱は文言解決に失敗しても空文字でSKIPPEDを記録する`() =
        runTest {
            val event = SpeechEvent.AceWindowsTyreOverheat(110)
            val key = ReadoutItemKey.AceWindows.TyreTemperature.Root
            val json = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    capture(json),
                )
            } just Runs
            val resolvedEvents = mutableListOf<SpeechEvent>()
            val processor =
                createProcessor {
                    resolvedEvents += it
                    error("preference error")
                }
            processor.processTyreTemperature(
                tyreCarcassTemperature(110.0f),
                listOf(event),
                listOf(key),
                emptyMap(),
                0L,
                logContext(),
            )
            assertEquals(listOf<SpeechEvent>(event), resolvedEvents)
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(event.withResolvedText("過熱 110度"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.AceWindows,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `タイヤ過熱の文言解決キャンセルは読み上げとログを要求せず伝播する`() =
        runTest {
            val processor = createProcessor { throw CancellationException("cancelled") }
            assertFailsWith<CancellationException> {
                processor.processTyreTemperature(
                    tyreCarcassTemperature(110.0f),
                    listOf(SpeechEvent.AceWindowsTyreOverheat(110)),
                    listOf(ReadoutItemKey.AceWindows.TyreTemperature.Root),
                    emptyMap(),
                    0L,
                    logContext(),
                )
            }
            verify(exactly = 0) { ttsEngine.speak(SpeechEvent.AceWindowsTyreOverheat(110), false) }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    private fun createProcessor(readoutText: suspend (SpeechEvent) -> String? = { it.narratedText }) =
        AceWindowsNarratorEventProcessor(
            ttsEngine = ttsEngine,
            saveTelemetryLog = SaveTelemetryLogUseCase(telemetryLogRepository),
            readoutText = readoutText,
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

    private fun remainingFuelLaps(remainingLaps: Float) = AceWindowsRemainingFuelLapsData(remainingLaps = remainingLaps)
}
