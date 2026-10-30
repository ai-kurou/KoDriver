package kurou.kodriver.feature.gt7ps5narrator

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
import kurou.kodriver.domain.engine.DebugReadoutText
import kurou.kodriver.domain.engine.Gt7Ps5ManualReadoutText
import kurou.kodriver.domain.engine.Gt7Ps5MyBestLap
import kurou.kodriver.domain.engine.Gt7Ps5RemainingFuelLapsWarning
import kurou.kodriver.domain.engine.Gt7Ps5RemainingFuelWarning
import kurou.kodriver.domain.engine.Gt7Ps5TyreOverheat
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5FuelUnit
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.gt7ps5.usecase.Gt7Ps5NarratorReadoutSettings
import kurou.kodriver.domain.gt7ps5.usecase.Gt7Ps5NarratorState
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.DebugReadoutRequest
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.NarrationSource
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

@Suppress("TooManyFunctions")
class Gt7Ps5NarratorEventProcessorTest {
    private val telemetryLogRepository: TelemetryLogRepository = mockk()

    private val ttsEngine: TextToSpeechEngine = mockk()

    @Test
    fun `手動イベントをキューに追加しテレメトリなしのログを保存する`() = assertQueuedManualReadout("読み上げる項目がありません")

    @Test
    fun `手動燃料の編集済み文言をキューとログで一致させる`() = assertQueuedManualReadout("残り42%")

    private fun assertQueuedManualReadout(text: String) =
        runTest {
            val event = Gt7Ps5ManualReadoutText(Gt7Ps5ReadoutItemKey.RemainingFuel.Root, text)
            val resolved = event.withResolvedText(event.text)
            val json = slot<String>()
            every { ttsEngine.speak(resolved, true) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    100L,
                    Simulator.Gt7Ps5,
                    event.readoutItemKey,
                    event.text,
                    NarrationOutcome.QUEUED,
                    NarrationSource.MANUAL,
                    capture(json),
                )
            } just Runs

            createProcessor().processManualReadout(
                listOf(event),
                emptyList(),
                mapOf(event.readoutItemKey to true),
                100L,
                logContext,
            )

            val logged = Json.parseToJsonElement(json.captured).jsonObject
            assertEquals(setOf("state", "settings", "observedAtMs"), logged.keys)
            assertEquals("100", logged.getValue("observedAtMs").toString())
            assertEquals(true, logged.getValue("state").jsonObject.containsKey("raw"))
            assertEquals(true, logged.getValue("settings").jsonObject.containsKey("raw"))
            verify(exactly = 1) { ttsEngine.speak(resolved, true) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    100L,
                    Simulator.Gt7Ps5,
                    event.readoutItemKey,
                    event.text,
                    NarrationOutcome.QUEUED,
                    NarrationSource.MANUAL,
                    json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `手動文言が空白なら読み上げずログにスキップを保存する`() =
        runTest {
            val event = Gt7Ps5ManualReadoutText(Gt7Ps5ReadoutItemKey.RemainingFuel.Root, " ")
            val json = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    event.readoutItemKey,
                    "",
                    NarrationOutcome.SKIPPED,
                    NarrationSource.MANUAL,
                    capture(json),
                )
            } just Runs
            createProcessor().processManualReadout(listOf(event), emptyList(), emptyMap(), 0L, logContext)
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    event.readoutItemKey,
                    "",
                    NarrationOutcome.SKIPPED,
                    NarrationSource.MANUAL,
                    json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `手動文言が解決できなければ読み上げずログにスキップを保存する`() =
        runTest {
            val event = Gt7Ps5ManualReadoutText(Gt7Ps5ReadoutItemKey.RemainingFuel.Root, "読み上げる項目がありません")
            val json = slot<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    event.readoutItemKey,
                    "",
                    NarrationOutcome.SKIPPED,
                    NarrationSource.MANUAL,
                    capture(json),
                )
            } just Runs
            createProcessor { null }.processManualReadout(listOf(event), emptyList(), emptyMap(), 0L, logContext)
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    event.readoutItemKey,
                    "",
                    NarrationOutcome.SKIPPED,
                    NarrationSource.MANUAL,
                    json.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `手動イベントがなければ発話もログ保存もしない`() =
        runTest {
            createProcessor().processManualReadout(emptyList(), emptyList(), emptyMap(), 0L, logContext)
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `直前のテレメトリがないイベントはnullとして保存する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val sourceKey = Gt7Ps5ReadoutItemKey.MyBestLap.Root
            every { ttsEngine.speak(Gt7Ps5MyBestLap(60_000, "自己ベストラップ更新 1分0秒000"), false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    sourceKey,
                    "自己ベストラップ更新 1分0秒000",
                    NarrationOutcome.SPOKEN,
                    NarrationSource.AUTO,
                    capture(telemetryJsons),
                )
            } just Runs

            createProcessor().process(
                sourceKey = sourceKey,
                telemetry = telemetry(),
                events = listOf(Gt7Ps5MyBestLap(60_000, "自己ベストラップ更新 1分0秒000")),
                autoReadoutOrder = listOf(sourceKey),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext,
            )

            assertEquals(true, telemetryJsons.single().startsWith("{\"state\":{\"raw\":"))
            assertEquals(true, telemetryJsons.single().contains("\"previousTelemetry\":null"))
            assertEquals(true, telemetryJsons.single().contains("remainingFuelLapsThreshold=3"))
            assertEquals(true, telemetryJsons.single().contains("remainingFuelThresholdPercentage=20"))
            assertEquals(true, telemetryJsons.single().contains("previousBestLapTimeMs=59000"))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(Gt7Ps5MyBestLap(60_000, "自己ベストラップ更新 1分0秒000"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    sourceKey,
                    "自己ベストラップ更新 1分0秒000",
                    NarrationOutcome.SPOKEN,
                    NarrationSource.AUTO,
                    telemetryJsons.single(),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `読み上げたイベントを直前と現在のテレメトリとともに保存する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val processor = createProcessor()
            val sourceKey = Gt7Ps5ReadoutItemKey.MyBestLap.Root
            every { ttsEngine.speak(Gt7Ps5MyBestLap(60_000, "自己ベストラップ更新 1分0秒000"), false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    200L,
                    Simulator.Gt7Ps5,
                    sourceKey,
                    "自己ベストラップ更新 1分0秒000",
                    NarrationOutcome.SPOKEN,
                    NarrationSource.AUTO,
                    capture(telemetryJsons),
                )
            } just Runs

            processor.process(
                sourceKey,
                telemetry(bestLapTimeMs = 60_000),
                emptyList(),
                emptyList(),
                emptyMap(),
                100L,
                logContext,
            )
            processor.process(
                sourceKey,
                telemetry(bestLapTimeMs = 59_000),
                listOf(Gt7Ps5MyBestLap(60_000, "自己ベストラップ更新 1分0秒000")),
                listOf(sourceKey),
                emptyMap(),
                200L,
                logContext = logContext,
            )

            assertEquals(1, telemetryJsons.size)
            assertEquals(
                true,
                telemetryJsons.single().contains(
                    """"previousTelemetry":{"lapCount":0,"lapsInRace":5,"bestLapTimeMs":60000,""" +
                        """"gasLevel":20.0,"gasCapacity":100.0,"carCategory":"",""" +
                        """"tyreTemperature":{"frontLeftCelsius":0.0,"frontRightCelsius":0.0,""" +
                        """"rearLeftCelsius":0.0,"rearRightCelsius":0.0}}""",
                ),
            )
            assertEquals(
                true,
                telemetryJsons.single().contains(
                    """"telemetry":{"lapCount":0,"lapsInRace":5,"bestLapTimeMs":59000,""" +
                        """"gasLevel":20.0,"gasCapacity":100.0,"carCategory":"",""" +
                        """"tyreTemperature":{"frontLeftCelsius":0.0,"frontRightCelsius":0.0,""" +
                        """"rearLeftCelsius":0.0,"rearRightCelsius":0.0}},"settings":""",
                ),
            )
            assertEquals(true, telemetryJsons.single().contains(""""observedAtMs":200"""))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(Gt7Ps5MyBestLap(60_000, "自己ベストラップ更新 1分0秒000"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    200L,
                    Simulator.Gt7Ps5,
                    sourceKey,
                    "自己ベストラップ更新 1分0秒000",
                    NarrationOutcome.SPOKEN,
                    NarrationSource.AUTO,
                    telemetryJsons.single(),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `テレメトリログの保存に失敗しても例外を投げない`() =
        runTest {
            val observedAtMs = 0L
            val sourceKey = Gt7Ps5ReadoutItemKey.MyBestLap.Root
            val narratedText = "自己ベストラップ更新 1分0秒000"
            val telemetryJsonSlot = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(Gt7Ps5MyBestLap(60_000, "自己ベストラップ更新 1分0秒000"), false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    observedAtMs,
                    Simulator.Gt7Ps5,
                    sourceKey,
                    narratedText,
                    NarrationOutcome.SPOKEN,
                    NarrationSource.AUTO,
                    capture(telemetryJsonSlot),
                )
            } throws RuntimeException("db error")

            createProcessor().process(
                sourceKey = sourceKey,
                telemetry = telemetry(),
                events = listOf(Gt7Ps5MyBestLap(60_000, "自己ベストラップ更新 1分0秒000")),
                autoReadoutOrder = listOf(sourceKey),
                queueEnabledStates = emptyMap(),
                observedAtMs = observedAtMs,
                logContext = logContext,
            )

            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(Gt7Ps5MyBestLap(60_000, "自己ベストラップ更新 1分0秒000"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    observedAtMs,
                    Simulator.Gt7Ps5,
                    sourceKey,
                    narratedText,
                    NarrationOutcome.SPOKEN,
                    NarrationSource.AUTO,
                    telemetryJsonSlot.captured,
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `テレメトリがNaNでも保存に失敗しない`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val sourceKey = Gt7Ps5ReadoutItemKey.MyBestLap.Root
            every { ttsEngine.speak(Gt7Ps5MyBestLap(60_000, "自己ベストラップ更新 1分0秒000"), false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    sourceKey,
                    "自己ベストラップ更新 1分0秒000",
                    NarrationOutcome.SPOKEN,
                    NarrationSource.AUTO,
                    capture(telemetryJsons),
                )
            } just Runs

            createProcessor().process(
                sourceKey = sourceKey,
                telemetry = telemetry(gasLevel = Float.NaN),
                events = listOf(Gt7Ps5MyBestLap(60_000, "自己ベストラップ更新 1分0秒000")),
                autoReadoutOrder = listOf(sourceKey),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext,
            )

            assertEquals(true, telemetryJsons.single().contains("\"gasLevel\":NaN"))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(Gt7Ps5MyBestLap(60_000, "自己ベストラップ更新 1分0秒000"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    sourceKey,
                    "自己ベストラップ更新 1分0秒000",
                    NarrationOutcome.SPOKEN,
                    NarrationSource.AUTO,
                    telemetryJsons.single(),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `機能ごとに直前のテレメトリを保持する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val processor = createProcessor()
            val myBestLapKey = Gt7Ps5ReadoutItemKey.MyBestLap.Root
            val fuelKey = Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root
            every { ttsEngine.speak(Gt7Ps5MyBestLap(60_000, "自己ベストラップ更新 1分0秒000"), false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    myBestLapKey,
                    "自己ベストラップ更新 1分0秒000",
                    NarrationOutcome.SPOKEN,
                    NarrationSource.AUTO,
                    capture(telemetryJsons),
                )
            } just Runs

            processor.process(
                myBestLapKey,
                telemetry(bestLapTimeMs = 60_000),
                emptyList(),
                emptyList(),
                emptyMap(),
                0L,
                logContext,
            )
            processor.process(
                fuelKey,
                telemetry(bestLapTimeMs = 50_000),
                emptyList(),
                emptyList(),
                emptyMap(),
                0L,
                logContext,
            )
            processor.process(
                myBestLapKey,
                telemetry(bestLapTimeMs = 59_000),
                listOf(Gt7Ps5MyBestLap(60_000, "自己ベストラップ更新 1分0秒000")),
                emptyList(),
                emptyMap(),
                0L,
                logContext = logContext,
            )

            assertEquals(true, telemetryJsons.single().contains("\"bestLapTimeMs\":60000"))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(Gt7Ps5MyBestLap(60_000, "自己ベストラップ更新 1分0秒000"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    myBestLapKey,
                    "自己ベストラップ更新 1分0秒000",
                    NarrationOutcome.SPOKEN,
                    NarrationSource.AUTO,
                    telemetryJsons.single(),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `優先度の高い項目を再生中なら読み上げずSKIPPEDとして保存する`() =
        runTest {
            val json = slot<String>()
            val currentKey = Gt7Ps5ReadoutItemKey.MyBestLap.Root
            val newEvent = Gt7Ps5RemainingFuelLapsWarning(2)
            every { ttsEngine.currentReadoutItemKey } returns currentKey
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    newEvent.readoutItemKey,
                    newEvent.narratedText,
                    NarrationOutcome.SKIPPED,
                    NarrationSource.AUTO,
                    capture(json),
                )
            } just Runs
            val processor = createProcessor()

            processor.process(
                sourceKey = Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
                telemetry = telemetry(),
                events = listOf(newEvent),
                autoReadoutOrder = listOf(currentKey, newEvent.readoutItemKey),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext,
            )

            verify(exactly = 0) { ttsEngine.stop() }
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    newEvent.readoutItemKey,
                    newEvent.narratedText,
                    NarrationOutcome.SKIPPED,
                    NarrationSource.AUTO,
                    json.captured,
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `優先度で本来無視される項目でもキュー設定が有効ならキュー再生する`() =
        runTest {
            val currentKey = Gt7Ps5ReadoutItemKey.MyBestLap.Root
            val newEvent = Gt7Ps5RemainingFuelLapsWarning(2)
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.speak(newEvent.copy(resolvedText = newEvent.narratedText), queue = true) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
                    "燃料は残り約2周",
                    NarrationOutcome.QUEUED,
                    NarrationSource.AUTO,
                    capture(telemetryJsons),
                )
            } just Runs
            val processor = createProcessor()

            processor.process(
                sourceKey = Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
                telemetry = telemetry(),
                events = listOf(newEvent),
                autoReadoutOrder = listOf(currentKey, newEvent.readoutItemKey),
                queueEnabledStates = mapOf(newEvent.readoutItemKey to true),
                observedAtMs = 0L,
                logContext = logContext,
            )

            verify(exactly = 0) { ttsEngine.stop() }
            verify(exactly = 1) { ttsEngine.speak(newEvent.copy(resolvedText = newEvent.narratedText), queue = true) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
                    "燃料は残り約2周",
                    NarrationOutcome.QUEUED,
                    NarrationSource.AUTO,
                    telemetryJsons.single(),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `優先度の低い項目を再生中なら停止して読み上げる`() =
        runTest {
            val currentKey = Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root
            val newEvent = Gt7Ps5MyBestLap(60_000, "自己ベストラップ更新 1分0秒000")
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns currentKey
            every { ttsEngine.stop() } just Runs
            every { ttsEngine.speak(newEvent, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    Gt7Ps5ReadoutItemKey.MyBestLap.Root,
                    "自己ベストラップ更新 1分0秒000",
                    NarrationOutcome.INTERRUPTED,
                    NarrationSource.AUTO,
                    capture(telemetryJsons),
                )
            } just Runs
            val processor = createProcessor()

            processor.process(
                sourceKey = Gt7Ps5ReadoutItemKey.MyBestLap.Root,
                telemetry = telemetry(),
                events = listOf(newEvent),
                autoReadoutOrder = listOf(newEvent.readoutItemKey, currentKey),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
                logContext = logContext,
            )

            verify(exactly = 1) { ttsEngine.stop() }
            verify(exactly = 1) { ttsEngine.speak(newEvent, false) }
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    Gt7Ps5ReadoutItemKey.MyBestLap.Root,
                    "自己ベストラップ更新 1分0秒000",
                    NarrationOutcome.INTERRUPTED,
                    NarrationSource.AUTO,
                    telemetryJsons.single(),
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
            val sourceKey = Gt7Ps5ReadoutItemKey.MyBestLap.Root
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 100L,
                    simulator = Simulator.Gt7Ps5,
                    readoutItemKey = sourceKey,
                    narratedText = "自己ベストラップ更新 1分0秒000",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    narrationSource = NarrationSource.AUTO,
                    telemetryJson = capture(slot<String>()),
                )
            } answers {
                saveCount += 1
                error("failed")
            }
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.Gt7Ps5,
                    readoutItemKey = sourceKey,
                    narratedText = "自己ベストラップ更新 1分0秒000",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    narrationSource = NarrationSource.AUTO,
                    telemetryJson = capture(slot<String>()),
                )
            } answers { saveCount += 1 }
            val processor = createProcessor()

            processor.process(
                sourceKey = sourceKey,
                telemetry = telemetry(bestLapTimeMs = 60_000),
                events = listOf(Gt7Ps5MyBestLap(60_000, "自己ベストラップ更新 1分0秒000")),
                autoReadoutOrder = listOf(sourceKey),
                queueEnabledStates = emptyMap(),
                observedAtMs = 100L,
                logContext = logContext,
            )
            processor.process(
                sourceKey = sourceKey,
                telemetry = telemetry(bestLapTimeMs = 59_000),
                events = listOf(Gt7Ps5MyBestLap(60_000, "自己ベストラップ更新 1分0秒000")),
                autoReadoutOrder = listOf(sourceKey),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext,
            )

            assertEquals(
                listOf<SpeechEvent>(
                    Gt7Ps5MyBestLap(60_000, "自己ベストラップ更新 1分0秒000"),
                    Gt7Ps5MyBestLap(60_000, "自己ベストラップ更新 1分0秒000"),
                ),
                spokenEvents,
            )
            assertEquals(2, saveCount)
            verify(exactly = 2) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 2) { ttsEngine.speak(Gt7Ps5MyBestLap(60_000, "自己ベストラップ更新 1分0秒000"), false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 100L,
                    simulator = Simulator.Gt7Ps5,
                    readoutItemKey = sourceKey,
                    narratedText = "自己ベストラップ更新 1分0秒000",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    narrationSource = NarrationSource.AUTO,
                    telemetryJson = capture(slot<String>()),
                )
            }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.Gt7Ps5,
                    readoutItemKey = sourceKey,
                    narratedText = "自己ベストラップ更新 1分0秒000",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    narrationSource = NarrationSource.AUTO,
                    telemetryJson = capture(slot<String>()),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `燃料残り周回数は解決文言をイベントとログに保持してキューへ渡す`() =
        runTest {
            val jsons = mutableListOf<String>()
            val key = Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root
            val event = Gt7Ps5RemainingFuelLapsWarning(2)
            val resolved = event.copy(resolvedText = "あと2周")
            every { ttsEngine.speak(resolved, true) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    10L,
                    Simulator.Gt7Ps5,
                    key,
                    "あと2周",
                    NarrationOutcome.QUEUED,
                    NarrationSource.AUTO,
                    capture(jsons),
                )
            } just Runs
            val resolvedEvents = mutableListOf<SpeechEvent>()
            val processor =
                Gt7Ps5NarratorEventProcessor(ttsEngine, SaveTelemetryLogUseCase(telemetryLogRepository)) {
                    resolvedEvents += it
                    "あと2周"
                }

            processor.process(
                sourceKey = key,
                telemetry = telemetry(),
                events = listOf(event),
                autoReadoutOrder = listOf(Gt7Ps5ReadoutItemKey.MyBestLap.Root, key),
                queueEnabledStates = mapOf(key to true),
                observedAtMs = 10L,
                logContext = logContext,
            )

            assertEquals(listOf<SpeechEvent>(event), resolvedEvents)
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(resolved, true) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    10L,
                    Simulator.Gt7Ps5,
                    key,
                    "あと2周",
                    NarrationOutcome.QUEUED,
                    NarrationSource.AUTO,
                    jsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `文言がnullの燃料残り周回数は読み上げず空文言とSKIPPEDを保存する`() =
        runTest {
            val jsons = mutableListOf<String>()
            val key = Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root
            val event = Gt7Ps5RemainingFuelLapsWarning(0)
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    10L,
                    Simulator.Gt7Ps5,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    NarrationSource.AUTO,
                    capture(jsons),
                )
            } just Runs
            val processor =
                Gt7Ps5NarratorEventProcessor(ttsEngine, SaveTelemetryLogUseCase(telemetryLogRepository)) { null }

            processor.process(key, telemetry(), listOf(event), listOf(key), emptyMap(), 10L, logContext)

            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(event, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    10L,
                    Simulator.Gt7Ps5,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    NarrationSource.AUTO,
                    jsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `燃料なし文言も解決して通常再生とログに渡す`() =
        runTest {
            val jsons = mutableListOf<String>()
            val key = Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root
            val event = Gt7Ps5RemainingFuelLapsWarning(0)
            val resolved = event.copy(resolvedText = "燃料切れです")
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(resolved, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    10L,
                    Simulator.Gt7Ps5,
                    key,
                    "燃料切れです",
                    NarrationOutcome.SPOKEN,
                    NarrationSource.AUTO,
                    capture(jsons),
                )
            } just Runs
            val processor =
                Gt7Ps5NarratorEventProcessor(ttsEngine, SaveTelemetryLogUseCase(telemetryLogRepository)) {
                    assertEquals(event, it)
                    "燃料切れです"
                }

            processor.process(key, telemetry(), listOf(event), listOf(key), emptyMap(), 10L, logContext)

            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(resolved, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    10L,
                    Simulator.Gt7Ps5,
                    key,
                    "燃料切れです",
                    NarrationOutcome.SPOKEN,
                    NarrationSource.AUTO,
                    jsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `燃料残量は解決文言をイベントとログに保持してキューへ渡す`() =
        runTest {
            val jsons = mutableListOf<String>()
            val key = Gt7Ps5ReadoutItemKey.RemainingFuel.Root
            val event = Gt7Ps5RemainingFuelWarning(30)
            val resolved = event.copy(resolvedText = "残り30%")
            every { ttsEngine.speak(resolved, true) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    10L,
                    Simulator.Gt7Ps5,
                    key,
                    "残り30%",
                    NarrationOutcome.QUEUED,
                    NarrationSource.AUTO,
                    capture(jsons),
                )
            } just Runs
            val resolvedEvents = mutableListOf<SpeechEvent>()
            val processor =
                Gt7Ps5NarratorEventProcessor(ttsEngine, SaveTelemetryLogUseCase(telemetryLogRepository)) {
                    resolvedEvents += it
                    "残り30%"
                }

            processor.process(
                sourceKey = key,
                telemetry = telemetry(),
                events = listOf(event),
                autoReadoutOrder = listOf(Gt7Ps5ReadoutItemKey.MyBestLap.Root, key),
                queueEnabledStates = mapOf(key to true),
                observedAtMs = 10L,
                logContext = logContext,
            )

            assertEquals(listOf<SpeechEvent>(event), resolvedEvents)
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(resolved, true) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    10L,
                    Simulator.Gt7Ps5,
                    key,
                    "残り30%",
                    NarrationOutcome.QUEUED,
                    NarrationSource.AUTO,
                    jsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `タイヤ過熱は解決文言をイベントとログに保持してキューへ渡す`() =
        runTest {
            val jsons = mutableListOf<String>()
            val key = Gt7Ps5ReadoutItemKey.TyreTemperature.Root
            val event = Gt7Ps5TyreOverheat(107)
            val resolved = event.copy(resolvedText = "タイヤ107度")
            every { ttsEngine.speak(resolved, true) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    10L,
                    Simulator.Gt7Ps5,
                    key,
                    "タイヤ107度",
                    NarrationOutcome.QUEUED,
                    NarrationSource.AUTO,
                    capture(jsons),
                )
            } just Runs
            val resolvedEvents = mutableListOf<SpeechEvent>()
            val processor =
                Gt7Ps5NarratorEventProcessor(ttsEngine, SaveTelemetryLogUseCase(telemetryLogRepository)) {
                    resolvedEvents += it
                    "タイヤ107度"
                }

            processor.process(
                sourceKey = key,
                telemetry = telemetry(),
                events = listOf(event),
                autoReadoutOrder = listOf(Gt7Ps5ReadoutItemKey.MyBestLap.Root, key),
                queueEnabledStates = mapOf(key to true),
                observedAtMs = 10L,
                logContext = logContext,
            )

            assertEquals(listOf<SpeechEvent>(event), resolvedEvents)
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(resolved, true) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    10L,
                    Simulator.Gt7Ps5,
                    key,
                    "タイヤ107度",
                    NarrationOutcome.QUEUED,
                    NarrationSource.AUTO,
                    jsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `自己ベストラップ更新は解決文言をイベントとログに保持してキューへ渡す`() =
        runTest {
            val jsons = mutableListOf<String>()
            val key = Gt7Ps5ReadoutItemKey.MyBestLap.Root
            val event = Gt7Ps5MyBestLap(83_456)
            val resolved = event.copy(resolvedText = "更新1分23秒456")
            every { ttsEngine.speak(resolved, true) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    10L,
                    Simulator.Gt7Ps5,
                    key,
                    "更新1分23秒456",
                    NarrationOutcome.QUEUED,
                    NarrationSource.AUTO,
                    capture(jsons),
                )
            } just Runs
            val resolvedEvents = mutableListOf<SpeechEvent>()
            val processor =
                Gt7Ps5NarratorEventProcessor(ttsEngine, SaveTelemetryLogUseCase(telemetryLogRepository)) {
                    resolvedEvents += it
                    "更新1分23秒456"
                }

            processor.process(
                sourceKey = key,
                telemetry = telemetry(),
                events = listOf(event),
                autoReadoutOrder = listOf(key),
                queueEnabledStates = mapOf(key to true),
                observedAtMs = 10L,
                logContext = logContext,
            )

            assertEquals(listOf<SpeechEvent>(event), resolvedEvents)
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(resolved, true) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    10L,
                    Simulator.Gt7Ps5,
                    key,
                    "更新1分23秒456",
                    NarrationOutcome.QUEUED,
                    NarrationSource.AUTO,
                    jsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    private val logContext =
        Gt7Ps5TelemetryLogContext(
            state = Gt7Ps5NarratorState(),
            settings =
                Gt7Ps5NarratorReadoutSettings(
                    enabledStates = mapOf(Gt7Ps5ReadoutItemKey.MyBestLap.Root to true),
                    remainingFuelLapsThreshold = 3,
                    remainingFuelThresholdPercentage = 20,
                    tyreTemperatureHighThresholdCelsius = Celsius(120),
                ),
            finalState = Gt7Ps5NarratorState(previousBestLapTimeMs = 59_000),
        )

    @Test
    fun `全GT7自由文言イベントの空文字と空白は開始音を要求せずSKIPPEDを保存する`() =
        runTest {
            val events =
                listOf(
                    Gt7Ps5RemainingFuelLapsWarning(3),
                    Gt7Ps5RemainingFuelLapsWarning(0),
                    Gt7Ps5RemainingFuelWarning(20),
                    Gt7Ps5MyBestLap(83_456),
                    Gt7Ps5TyreOverheat(120),
                )
            var observedAtMs = 0L
            events.forEach { event ->
                listOf("", " \t\n　").forEach { text ->
                    observedAtMs++
                    val jsons = mutableListOf<String>()
                    coEvery {
                        telemetryLogRepository.saveTelemetryLog(
                            observedAtMs,
                            Simulator.Gt7Ps5,
                            event.readoutItemKey,
                            "",
                            NarrationOutcome.SKIPPED,
                            NarrationSource.AUTO,
                            capture(jsons),
                        )
                    } just Runs
                    val processor =
                        Gt7Ps5NarratorEventProcessor(
                            ttsEngine,
                            SaveTelemetryLogUseCase(telemetryLogRepository),
                        ) { text }

                    processor.process(
                        event.readoutItemKey,
                        telemetry(),
                        listOf(event),
                        listOf(event.readoutItemKey),
                        emptyMap(),
                        observedAtMs,
                        logContext,
                    )

                    coVerify(exactly = 1) {
                        telemetryLogRepository.saveTelemetryLog(
                            observedAtMs,
                            Simulator.Gt7Ps5,
                            event.readoutItemKey,
                            "",
                            NarrationOutcome.SKIPPED,
                            NarrationSource.AUTO,
                            jsons.single(),
                        )
                    }
                }
            }
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            events.forEach { event -> verify(exactly = 0) { ttsEngine.speak(event, false) } }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `文言解決に失敗しても同じ入力の後続イベントと次回処理を継続する`() =
        runTest {
            val key = Gt7Ps5ReadoutItemKey.RemainingFuel.Root
            val failedEvent = Gt7Ps5RemainingFuelWarning(20)
            val nextEvent = Gt7Ps5RemainingFuelWarning(10)
            val resolvedEvent = Gt7Ps5RemainingFuelWarning(10, "復旧")
            val skippedJson = slot<String>()
            val spokenJsons = mutableListOf<String>()
            val resolvedEvents = mutableListOf<SpeechEvent>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(resolvedEvent, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    100L,
                    Simulator.Gt7Ps5,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    NarrationSource.AUTO,
                    capture(skippedJson),
                )
            } just Runs
            listOf(100L, 200L).forEach { time ->
                coEvery {
                    telemetryLogRepository.saveTelemetryLog(
                        time,
                        Simulator.Gt7Ps5,
                        key,
                        "復旧",
                        NarrationOutcome.SPOKEN,
                        NarrationSource.AUTO,
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

            processor.process(
                sourceKey = key,
                telemetry = telemetry(gasLevel = 20f),
                events = listOf(failedEvent, nextEvent),
                autoReadoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 100L,
                logContext = logContext,
            )
            processor.process(
                sourceKey = key,
                telemetry = telemetry(gasLevel = 10f),
                events = listOf(nextEvent),
                autoReadoutOrder = listOf(key),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
                logContext = logContext,
            )

            assertEquals(listOf<SpeechEvent>(failedEvent, nextEvent, nextEvent), resolvedEvents)
            assertEquals(2, spokenJsons.size)
            assertEquals(
                Json.parseToJsonElement(skippedJson.captured).jsonObject["telemetry"],
                Json.parseToJsonElement(spokenJsons.last()).jsonObject["previousTelemetry"],
            )
            verify(exactly = 2) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 2) { ttsEngine.speak(resolvedEvent, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    100L,
                    Simulator.Gt7Ps5,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    NarrationSource.AUTO,
                    skippedJson.captured,
                )
            }
            listOf(100L, 200L).forEachIndexed { index, time ->
                coVerify(exactly = 1) {
                    telemetryLogRepository.saveTelemetryLog(
                        time,
                        Simulator.Gt7Ps5,
                        key,
                        "復旧",
                        NarrationOutcome.SPOKEN,
                        NarrationSource.AUTO,
                        spokenJsons[index],
                    )
                }
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `文言解決のキャンセルは再スローしログ保存と後続処理を行わない`() =
        runTest {
            val key = Gt7Ps5ReadoutItemKey.RemainingFuel.Root
            val event = Gt7Ps5RemainingFuelWarning(20)
            val cancellation = CancellationException("cancelled")
            val resolvedEvents = mutableListOf<SpeechEvent>()
            val processor =
                createProcessor { input ->
                    resolvedEvents += input
                    throw cancellation
                }

            val thrown =
                assertFailsWith<CancellationException> {
                    processor.process(
                        sourceKey = key,
                        telemetry = telemetry(gasLevel = 20f),
                        events = listOf(event, Gt7Ps5RemainingFuelWarning(10)),
                        autoReadoutOrder = listOf(key),
                        queueEnabledStates = emptyMap(),
                        observedAtMs = 100L,
                        logContext = logContext,
                    )
                }

            assertSame(cancellation, thrown)
            assertEquals(listOf<SpeechEvent>(event), resolvedEvents)
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(event, false) }
            coVerify(exactly = 0) {
                telemetryLogRepository.saveTelemetryLog(
                    100L,
                    Simulator.Gt7Ps5,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    NarrationSource.AUTO,
                    match { it.isNotEmpty() },
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `デバッグ要求はキュー設定を使い入力本文と状態設定をログに保存する`() =
        runTest {
            val key = Gt7Ps5ReadoutItemKey.MyBestLap.Root
            val request = DebugReadoutRequest(Simulator.Gt7Ps5, key, " 入力 {laps} ")
            val event = DebugReadoutText(key, request.text)
            val telemetryJson = slot<String>()
            every { ttsEngine.speak(event, true) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    100L,
                    Simulator.Gt7Ps5,
                    key,
                    request.text,
                    NarrationOutcome.QUEUED,
                    NarrationSource.AUTO,
                    capture(telemetryJson),
                )
            } just Runs
            val context = logContext
            createProcessor().processDebugReadout(
                request = request,
                autoReadoutOrder = emptyList(),
                queueEnabledStates = mapOf(key to true),
                observedAtMs = 100L,
                logContext = context,
            )
            val json = Json.parseToJsonElement(telemetryJson.captured).jsonObject
            assertEquals(setOf("state", "settings", "observedAtMs", "debugReadout"), json.keys)
            assertEquals("true", json.getValue("debugReadout").toString())
            assertEquals("100", json.getValue("observedAtMs").toString())
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(event, true) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    100L,
                    Simulator.Gt7Ps5,
                    key,
                    request.text,
                    NarrationOutcome.QUEUED,
                    NarrationSource.AUTO,
                    telemetryJson.captured,
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    private fun createProcessor(readoutText: suspend (SpeechEvent) -> String? = { it.narratedText }) =
        Gt7Ps5NarratorEventProcessor(
            ttsEngine = ttsEngine,
            saveTelemetryLog = SaveTelemetryLogUseCase(telemetryLogRepository),
            readoutText = readoutText,
        )

    private fun telemetry(
        bestLapTimeMs: Int = 60_000,
        gasLevel: Float = 20f,
    ) = Gt7Ps5TelemetryData(
        lapCount = 0,
        lapsInRace = 5,
        bestLapTimeMs = bestLapTimeMs,
        gasLevel = Gt7Ps5FuelUnit(gasLevel),
        gasCapacity = Gt7Ps5FuelUnit(100f),
    )
}
